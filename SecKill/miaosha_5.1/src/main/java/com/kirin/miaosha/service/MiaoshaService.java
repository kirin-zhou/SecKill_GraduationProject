package com.kirin.miaosha.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.redis.MiaoshaKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.vo.GoodsVo;
import com.kirin.miaosha.util.MD5Util;
import com.kirin.miaosha.util.UUIDUtil;

@SuppressWarnings("restriction")
@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		boolean success = goodsService.reduceStock(goods); //减少库存
		if(success) { //若减库存成功，则生成订单
			//order_info表和maiosha_order表
			return orderService.createOrder(user, goods);
		}else {
			setGoodsOver(goods.getId()); //标记商品已被秒杀完
			return null;
		}
	}

	//通过用户和订单查询是否生成订单
	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		if(order != null) { //秒杀成功
			return order.getOrderId();
		}else {
			boolean isOver = getGoodsOver(goodsId); //通过之前标记的已被秒杀完的商品，判断该商品是否卖完
			if(isOver) {
				return -1; //秒杀失败
			}else {
				return 0; //排队中，继续轮询
			}
		}
	}
	
	//标记商品已被秒杀完
	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}
	
	//通过之前标记的已被秒杀完的商品，判断该商品是否卖完
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
	}
	
	//还原库存
	public void reset(List<GoodsVo> goodsList) {
		goodsService.resetStock(goodsList); //还原库存
		orderService.deleteOrders(); //删除生成的订单
	}

	
	//验证秒杀商品地址
	public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
		if(user == null || path == null) {
			return false;
		}
		//之前存储的path
		String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getId() + "_"+ goodsId, String.class);
		return path.equals(pathOld); //判断是否相等
	}

	//生成秒杀商品地址
	public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
    	redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId() + "_"+ goodsId, str);
		return str;
	}

	//生成图片验证码
	public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <=0) {
			return null;
		}
		int width = 80;
		int height = 32;
		//生成图片
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(new Color(0xDCDCDC)); //设置背景颜色
		g.fillRect(0, 0, width, height); //背景颜色填充
		g.setColor(Color.black); //画笔设置为黑色
		g.drawRect(0, 0, width - 1, height - 1); //画一个矩形框
		Random rdm = new Random(); //生成随机数
		//在图片上生成50个干扰点
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		//生成验证码
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0)); //颜色
		g.setFont(new Font("Candara", Font.BOLD, 24)); //字体
		g.drawString(verifyCode, 8, 24); //把验证码写在图片上
		g.dispose(); //销毁画笔
		//计算验证码中表达式的结果
		int rnd = calc(verifyCode);
		//把验证码存到redis中
		redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
		//输出图片	
		return image;
	}

	//验证验证码
	public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
		if(user == null || goodsId <=0) {
			return false;
		}
		Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
		if(codeOld == null || codeOld - verifyCode != 0 ) { //若验证码值空
			return false;
		}
		redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId); //在redis缓存中删掉
		return true;
	}
	
//	//测试类
//	public static void main(String[] args) {
//		System.out.println(calc("1+3-8"));
//	}
	
	//计算验证码中表达式的结果
	private static int calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer)engine.eval(exp);
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 生成验证码：随机生成3个数字，做+ - *
	 **/
	private static char[] ops = new char[] {'+', '-', '*'};
	
	private String generateVerifyCode(Random rdm) {
		int num1 = rdm.nextInt(10);
	    int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = ""+ num1 + op1 + num2 + op2 + num3;
		return exp;
	}
	
}

