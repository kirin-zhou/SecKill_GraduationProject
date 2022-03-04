package com.kirin.miaosha.redis;

public class GoodsKey extends BasePrefix{

	private GoodsKey(int expireSeconds, String prefix) { //expireSeconds：设置有效期
		super(expireSeconds, prefix);
	}
	public static GoodsKey getGoodsList = new GoodsKey(60, "gl"); //有效期60s
	public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
}
