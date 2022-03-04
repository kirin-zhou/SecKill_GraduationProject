package com.kirin.miaosha.redis;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

@Service
public class RedisService {
	
	@Autowired
	JedisPool jedisPool;
	
	//获取当前对象
	public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//生成真正的key
			String realKey = prefix.getPrefix() + key;
			String str = jedis.get(realKey);
			T t = stringToBean(str, clazz); //将获取的值，从String类型转换成bean类型
			return t;
		}finally {
			returnToPool(jedis);
		}
	}
	
	//设置对象
	public <T> boolean set(KeyPrefix prefix, String key,  T value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String str = beanToString(value); //将设置的值，从bean类型转换成String类型
			if(str == null || str.length() <= 0) {
				return false;
			}
			//生成真正的key
			String realKey = prefix.getPrefix() + key;
			int seconds = prefix.expireSeconds(); //过期时间
			if(seconds <= 0) {
				jedis.set(realKey, str);
			}else {
				jedis.setex(realKey, seconds, str);
			}
			return true;
		}finally {
			returnToPool(jedis);
		}
	}
	
	//判断key是否存在
	public <T> boolean exists(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//生成真正的key
			String realKey = prefix.getPrefix() + key;
			return jedis.exists(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
	
	//增加值：+1
	public <T> Long incr(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//生成真正的key
			String realKey = prefix.getPrefix() + key;
			return jedis.incr(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
	
	//减少值：-1
	public <T> Long decr(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//生成真正的key
			String realKey = prefix.getPrefix() + key;
			return jedis.decr(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
	
	//删除（com.kirin.miaosha.service / MiaoshaUserService.java / updatePassword()修改密码：删除之前的redis）
	public boolean delete(KeyPrefix prefix, String key) {
		 Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			//生成真正的key
			String realKey  = prefix.getPrefix() + key;
			long ret =  jedis.del(realKey);
			return ret > 0;
		 }finally {
			  returnToPool(jedis);
		 }
	}
	
	public boolean delete(KeyPrefix prefix) {
		if(prefix == null) {
			return false;
		}
		List<String> keys = scanKeys(prefix.getPrefix());
		if(keys==null || keys.size() <= 0) {
			return true;
		}
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(keys.toArray(new String[0]));
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(jedis != null) {
				jedis.close();
			}
		}
	}
	
	public List<String> scanKeys(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			List<String> keys = new ArrayList<String>();
			String cursor = "0";
			ScanParams sp = new ScanParams();
			sp.match("*"+key+"*");
			sp.count(100);
			do{
				ScanResult<String> ret = jedis.scan(cursor, sp);
				List<String> result = ret.getResult();
				if(result!=null && result.size() > 0){
					keys.addAll(result);
				}
				//再处理cursor
				cursor = ret.getStringCursor();
			}while(!cursor.equals("0"));
			return keys;
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	
	//将设置的值，从bean类型转换成String类型
	public static <T> String beanToString(T value) {
		if(value == null) { //若空，直接返回
			return null;
		}
		Class<?> clazz = value.getClass();
		if(clazz == int.class || clazz == Integer.class) { //若是int类型，直接写入
			return ""+value;
		}else if(clazz == String.class) { //若是String类型，直接写入
			return (String)value;
		}else if(clazz == long.class || clazz == Long.class) { //若是long类型，直接写入
			return ""+value;
		}else {
			return JSON.toJSONString(value);
		}
	}
	
	//将获取的值，从String类型转换成bean类型
	@SuppressWarnings("unchecked")
	public static <T> T stringToBean(String str, Class<T> clazz) {
		if(str == null || str.length() <= 0 || clazz == null) { //若空，直接返回
			return null;
		}
		if(clazz == int.class || clazz == Integer.class) { //若是int类型，强转为int类型
			return (T)Integer.valueOf(str);
		}else if(clazz == String.class) { //若是String类型，直接输出
			return (T)str;
		}else if(clazz == long.class || clazz == Long.class) { //若是long类型，强转为long类型
			return (T)Long.valueOf(str);
		}else {
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
	}
	
	private void returnToPool(Jedis jedis) {
		 if(jedis != null) {
			 jedis.close();
		 }
	}
	
}
