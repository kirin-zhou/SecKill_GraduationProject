package com.kirin.miaosha.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kirin.miaosha.access.AccessLimit;
import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.rabbitmq.MQSender;
import com.kirin.miaosha.rabbitmq.MiaoshaMessage;
import com.kirin.miaosha.redis.AccessKey;
import com.kirin.miaosha.redis.GoodsKey;
import com.kirin.miaosha.redis.MiaoshaKey;
import com.kirin.miaosha.redis.OrderKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.result.Result;
import com.kirin.miaosha.service.GoodsService;
import com.kirin.miaosha.service.MiaoshaService;
import com.kirin.miaosha.service.MiaoshaUserService;
import com.kirin.miaosha.service.OrderService;
import com.kirin.miaosha.util.MD5Util;
import com.kirin.miaosha.util.UUIDUtil;
import com.kirin.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean { //实现InitialzingBean接口，重写afterProperties方法

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	MQSender sender;
	
	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>(); //【内存标记】服务器记录是否结束标记
	
	//2.【Redis缓存】系统初始化：将秒杀商品库存加载到Redis中
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo(); //查询出所有的商品，加载到库存中
		if(goodsList == null) {
			return;
		}
		//从数据库中将秒杀商品的信息读取出来，再一个一个加载到缓存中
		for(GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount()); //放入缓存
			localOverMap.put(goods.getId(), false); //【内存标记】标记商品id没有结束
		}
	}
	
	/*
	 * 2.miaosha.jmx
	 * QPS=146.9/s
	 * 并发10000 = 1000个线程 * 10次循环
	 */
	//秒杀表单提交
	@RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,
    		@PathVariable("path") String path) {
    	model.addAttribute("user", user);
    	if(user == null) { //若无法获取用户，则返回登录页
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	
    	//验证path
    	boolean check = miaoshaService.checkPath(user, goodsId, path);
    	if(!check){
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	
    	//3.【内存标记】减少Redis访问
    	boolean over = localOverMap.get(goodsId); //判断标记商品id是否结束
    	if(over) { //若结束，则没必要访问Redis，直接返回“已秒杀完”
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	
    	//2.Redis缓存（优化）
    	//收到秒杀请求后，在缓存中预减库存
    	long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId); //10
    	if(stock < 0) { //无库存，秒杀失败
    		localOverMap.put(goodsId, true); //【内存标记】将标记商品id的值改成true，表示标记商品id已结束
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//入队：用户信息+商品id
    	MiaoshaMessage mm = new MiaoshaMessage();
    	mm.setUser(user);
    	mm.setGoodsId(goodsId);
    	sender.sendMiaoshaMessage(mm);
    	return Result.success(0); //0：排队中
    	
    	
    	/*
    	//1.数据库操作（未优化）
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//根据商品id查询，10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) { //已经秒杀到商品，不能再秒杀
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//        return Result.success(orderInfo);
        */
    	
    }
	
	
	/**
	 * 前端轮询服务端（static/goods_detail.htm）
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     **/
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId); //通过用户和订单查询是否生成订单
    	return Result.success(result);
    }
    
    //还原库存
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.listGoodsVo(); //查找商品
		for(GoodsVo goods : goodsList) {
			goods.setStockCount(10); //把数据库中商品库存还原成10个
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10); //把Redis中商品库存还原成10个
			localOverMap.put(goods.getId(), false); //【内存标记】标记商品id没有结束
		}
		//删除所有秒杀结束的标记
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}
    
    
    /**
	 * U7-1 秒杀接口地址隐藏（static/goods_detail.htm）
     **/
	//【接口防刷2-（通用方法）拦截器】查询访问次数：从用户第一次访问开始计时，5秒访问5次
    @AccessLimit(seconds=5, maxCount=5, needLogin=true) //限定访问几秒，最多访问次数，需不需要登录
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,
    		@RequestParam(value="verifyCode", defaultValue="0")int verifyCode
    		) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	
    	/*
    	//【接口防刷1-简单方法，不实用】查询访问次数：从用户第一次访问开始计时，5秒访问5次
    	String uri = request.getRequestURI();
    	String key = uri + "_" + user.getId();
    	Integer count = redisService.get(AccessKey.access, key, Integer.class);
    	if(count == null) { //若规定时间内，再有访问，则value+1
    		redisService.set(AccessKey.access, key, 1);
    	}else if(count < 5){ //若规定时间内，没有超过数值，且到了下一个时间，则value值归0，重新计数
    		redisService.incr(AccessKey.access, key);
    	}else { //若规定时间内，value值超过限定数值，则返回访问太频繁
			return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
		}
		*/
    	
    	//计算验证码
    	boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
    	if(!check) {
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	
    	String path = miaoshaService.createMiaoshaPath(user, goodsId);
    	return Result.success(path);
    }
    
    
    /**
	 * U7-2 生成图片验证码（static/goods_detail.htm）
     **/
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	try {
    		BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream(); //输出图片
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.MIAOSHA_FAIL);
    	}
    }
}
