package com.kirin.miaosha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.redis.MiaoshaKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.vo.GoodsVo;

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
	
}

