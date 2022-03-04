package com.kirin.miaosha.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

//用于判断用户登录时输入的格式
public class ValidatorUtil {
	
	private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}"); //以1开头，后跟10个数字
	
	public static boolean isMobile(String src) {
		if(StringUtils.isEmpty(src)) {
			return false;
		}
		Matcher m = mobile_pattern.matcher(src);
		return m.matches();
	}
	
//	//测试类
//	public static void main(String[] args) {
//		System.out.println(isMobile("18912341234")); //true
//		System.out.println(isMobile("1891234123")); //false
//	}

}
