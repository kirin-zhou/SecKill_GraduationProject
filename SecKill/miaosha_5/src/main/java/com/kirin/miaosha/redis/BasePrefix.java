package com.kirin.miaosha.redis;

//模板：接口KeyPrefix <—— 抽象类BasePrefix <—— 实现类MiaoshaUserKey

//2.抽象类
public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds;
	
	private String prefix;
	
	public BasePrefix(String prefix) { //0代表永不过期
		this(0, prefix);
	}
	
	public BasePrefix( int expireSeconds, String prefix) {
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	public int expireSeconds() { //默认0代表永不过期
		return expireSeconds;
	}

	public String getPrefix() { //获取前缀
		String className = getClass().getSimpleName();
		return className+":" + prefix;
	}

}

