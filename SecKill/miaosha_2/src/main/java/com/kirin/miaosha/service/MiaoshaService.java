package com.kirin.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		goodsService.reduceStock(goods); //减少库存
		//order_info表和maiosha_order表
		return orderService.createOrder(user, goods);
	}
}

