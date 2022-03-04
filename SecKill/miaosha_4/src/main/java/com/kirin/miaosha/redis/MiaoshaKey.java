package com.kirin.miaosha.redis;

//标记商品已被秒杀完
public class MiaoshaKey extends BasePrefix{

	private MiaoshaKey(String prefix) {
		super(prefix);
	}
	public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
}
