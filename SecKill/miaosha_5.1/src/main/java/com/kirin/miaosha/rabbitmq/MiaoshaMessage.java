package com.kirin.miaosha.rabbitmq;

import com.kirin.miaosha.domain.MiaoshaUser;

//秒杀入队：用户信息+商品id
public class MiaoshaMessage {
	private MiaoshaUser user;
	private long goodsId;
	public MiaoshaUser getUser() {
		return user;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
}
