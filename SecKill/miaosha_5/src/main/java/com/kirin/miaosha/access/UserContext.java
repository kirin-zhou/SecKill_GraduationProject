package com.kirin.miaosha.access;

import com.kirin.miaosha.domain.MiaoshaUser;

public class UserContext {
	
	//用ThreadLocal来装user信息，调用它的set和get方法，向其中存储值
    //ThreadLocal是为当前线程存储值，所以在多线程下，各个线程的user并不冲突
	private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();
	
	public static void setUser(MiaoshaUser user) {
		userHolder.set(user);
	}
	
	public static MiaoshaUser getUser() {
		return userHolder.get();
	}

}