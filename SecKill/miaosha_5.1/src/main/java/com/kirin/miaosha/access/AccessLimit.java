package com.kirin.miaosha.access;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {
	int seconds(); //限定访问几秒
	int maxCount(); //最多访问次数
	boolean needLogin() default true; //需不需要登录
}