package com.kirin.miaosha.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.redis.GoodsKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.Result;
import com.kirin.miaosha.service.GoodsService;
import com.kirin.miaosha.service.MiaoshaUserService;
import com.kirin.miaosha.vo.GoodsDetailVo;
import com.kirin.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	
	@Autowired
	ApplicationContext applicationContext;
	
	/*
	 * 1.goods_list.jmx
	 * 并发10000 = 1000个线程 * 10次循环
	 * 压测1：QPS=296.9/s
	 * 压测2（页面缓存）：QPS=1558.1/s
	 */
	//1.用户登录后，跳转到商品列表页（缓存）
	@RequestMapping(value="/to_list", produces="text/html") //返回HTML页面
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user) {
    	model.addAttribute("user", user);
    	//（1）取缓存
    	String html = redisService.get(GoodsKey.getGoodsList, "", String.class); //从缓存中取页面
    	if(!StringUtils.isEmpty(html)) { //若有HTML页面，直接返回给客户端
    		return html;
    	}
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
    	model.addAttribute("goodsList", goodsList);
//   	 return "goods_list";
    	//（2）若无，手动渲染
    	SpringWebContext ctx = new SpringWebContext(request,response,
    			request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
    	//手动渲染：ThymeleafViewResolver.getTemplateEngine().process(页面名称, 包含业务的内容)
    	html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
    	//（3）手动渲染完成后，保存到缓存中，以便下次使用
    	if(!StringUtils.isEmpty(html)) {
    		redisService.set(GoodsKey.getGoodsList, "", html);
    	}
    	return html;
    }
	
	//2.3 查看商品详情（页面静态化：页面存为HTML，动态数据通过接口从服务端获取）
	//@RequestMapping指定的映射URL，其中有用{}括起来的参数，在方法的形参处，用@PathVariable注解对其进行获取
	@RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,
    		@PathVariable("goodsId")long goodsId) {
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	
    	long startAt = goods.getStartDate().getTime(); //秒杀开始时间
    	long endAt = goods.getEndDate().getTime(); //秒杀结束时间
    	long now = System.currentTimeMillis(); //现在的时间
    	int miaoshaStatus = 0; //设置秒杀状态
    	int remainSeconds = 0; //剩余时间（秒）
    	if(now < startAt ) { //秒杀还没开始，倒计时
    		miaoshaStatus = 0; //miaoshaStatus=0：秒杀还没开始
    		remainSeconds = (int)((startAt - now )/1000);
    	}else if(now > endAt){ //秒杀已经结束
    		miaoshaStatus = 2; //miaoshaStatus=2：秒杀已经结束
    		remainSeconds = -1;
    	}else { //秒杀进行中
    		miaoshaStatus = 1; //miaoshaStatus=1：秒杀进行中
    		remainSeconds = 0;
    	}
    	
    	GoodsDetailVo vo = new GoodsDetailVo();
    	vo.setGoods(goods);
    	vo.setUser(user);
    	vo.setRemainSeconds(remainSeconds);
    	vo.setMiaoshaStatus(miaoshaStatus);
    	return Result.success(vo);
    }
    
    
	//2.2 查看商品详情（缓存）
	//@RequestMapping指定的映射URL，其中有用{}括起来的参数，在方法的形参处，用@PathVariable注解对其进行获取
	@RequestMapping(value="/to_detail2/{goodsId}",produces="text/html") //根据id获取商品
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,
    		@PathVariable("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	
    	//（1）取缓存
    	String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class); //""+goodsId：加参数goodsId
    	if(!StringUtils.isEmpty(html)) {
    		return html;
    	}
    	//（2）手动渲染
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	model.addAttribute("goods", goods);
    	
    	long startAt = goods.getStartDate().getTime(); //秒杀开始时间
    	long endAt = goods.getEndDate().getTime(); //秒杀结束时间
    	long now = System.currentTimeMillis(); //现在的时间
    	
    	int miaoshaStatus = 0; //设置秒杀状态
    	int remainSeconds = 0; //剩余时间（秒）
    	if(now < startAt ) { //秒杀还没开始，倒计时
    		miaoshaStatus = 0; //miaoshaStatus=0：秒杀还没开始
    		remainSeconds = (int)((startAt - now )/1000);
    	}else if(now > endAt){ //秒杀已经结束
    		miaoshaStatus = 2; //miaoshaStatus=2：秒杀已经结束
    		remainSeconds = -1;
    	}else { //秒杀进行中
    		miaoshaStatus = 1; //miaoshaStatus=1：秒杀进行中
    		remainSeconds = 0;
    	}
    	
    	model.addAttribute("miaoshaStatus", miaoshaStatus); //往前台传数据
    	model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";
    	
    	SpringWebContext ctx = new SpringWebContext(request,response,
    			request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
    	html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
    	//（3）手动渲染完成后，保存到缓存中，以便下次使用
    	if(!StringUtils.isEmpty(html)) {
    		redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html); //""+goodsId：加参数goodsId
    	}
    	return html;
    }
    
//	//2.1 查看商品详情（原无缓存）
//	//@RequestMapping指定的映射URL，其中有用{}括起来的参数，在方法的形参处，用@PathVariable注解对其进行获取
//    @RequestMapping("/to_detail/{goodsId}") //根据id获取商品
//    public String detail(Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId) {
//    	model.addAttribute("user", user);
//    	
//    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//    	model.addAttribute("goods", goods);
//    	
//    	long startAt = goods.getStartDate().getTime(); //秒杀开始时间
//    	long endAt = goods.getEndDate().getTime(); //秒杀结束时间
//    	long now = System.currentTimeMillis(); //现在的时间
//    	
//    	int miaoshaStatus = 0; //设置秒杀状态
//    	int remainSeconds = 0; //剩余时间（秒）
//    	if(now < startAt ) { //秒杀还没开始，倒计时
//    		miaoshaStatus = 0; //miaoshaStatus=0：秒杀还没开始
//    		remainSeconds = (int)((startAt - now )/1000);
//    	}else if(now > endAt){ //秒杀已经结束
//    		miaoshaStatus = 2; //miaoshaStatus=2：秒杀已经结束
//    		remainSeconds = -1;
//    	}else { //秒杀进行中
//    		miaoshaStatus = 1; //miaoshaStatus=1：秒杀进行中
//    		remainSeconds = 0;
//    	}
//    	model.addAttribute("miaoshaStatus", miaoshaStatus); //往前台传数据
//    	model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";
//    }
}
