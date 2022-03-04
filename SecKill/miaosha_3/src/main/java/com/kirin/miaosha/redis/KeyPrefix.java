package com.kirin.miaosha.redis;

//模板：接口KeyPrefix <—— 抽象类BasePrefix <—— 实现类MiaoshaUserKey

//1.接口
public interface KeyPrefix {
	
	public int expireSeconds(); //有效期
	
	public String getPrefix(); //前缀
	
}
