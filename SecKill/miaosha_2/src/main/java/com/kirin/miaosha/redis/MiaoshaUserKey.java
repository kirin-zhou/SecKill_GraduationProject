package com.kirin.miaosha.redis;

//模板：接口KeyPrefix <—— 抽象类BasePrefix <—— 实现类MiaoshaUserKey

//3.实现类
public class MiaoshaUserKey extends BasePrefix{
	public static final int TOKEN_EXPIRE = 3600*24*2; //设置token有效期2天
	private MiaoshaUserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");
}

