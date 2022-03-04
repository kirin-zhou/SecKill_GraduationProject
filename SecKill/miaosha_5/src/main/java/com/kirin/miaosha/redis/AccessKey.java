package com.kirin.miaosha.redis;

//接口防刷
public class AccessKey extends BasePrefix{

	private AccessKey( int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	//【接口防刷1-简单方法，不实用】查询访问次数：从用户第一次访问开始计时，5秒访问5次
//	public static AccessKey access = new AccessKey(5,"access"); //限制5秒内访问5次
	
	//【接口防刷2-（通用方法）拦截器】查询访问次数：从用户第一次访问开始计时，5秒访问5次
	public static AccessKey withExpire(int expireSeconds) {
		return new AccessKey(expireSeconds, "access");
	}
}
