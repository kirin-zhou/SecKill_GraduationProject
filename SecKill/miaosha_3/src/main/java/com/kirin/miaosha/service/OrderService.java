package com.kirin.miaosha.service;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kirin.miaosha.dao.OrderDao;
import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.redis.OrderKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.vo.GoodsVo;

@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;
	
	@Autowired
	RedisService redisService;
	
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
		//在判断有没有秒杀到商品时，不用查数据库，可以直接查缓存
		//return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		return redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+userId+"_"+goodsId, MiaoshaOrder.class); //查缓存
	}
	
	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}

	//生成订单
	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		//将信息保存进商品订单表
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1); //商品数量
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
		orderInfo.setOrderChannel(1); //秒杀渠道
		orderInfo.setStatus(0); //商品状态：0未支付
		orderInfo.setUserId(user.getId());
		long orderId = orderDao.insert(orderInfo);
		//将信息保存进秒杀商品订单表
		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goods.getId());
		miaoshaOrder.setOrderId(orderId);
		miaoshaOrder.setUserId(user.getId());
		orderDao.insertMiaoshaOrder(miaoshaOrder);
		
		//下单成功后，把miaoshaOrder写进Redis缓存
		redisService.set(OrderKey.getMiaoshaOrderByUidGid, ""+user.getId()+"_"+goods.getId(), miaoshaOrder);
		
		return orderInfo;
	}
}
