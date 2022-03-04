package com.kirin.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	public static String md5(String src) {
		return DigestUtils.md5Hex(src); //调用DigestUtils，实现md5处理
	}
	
	private static final String salt = "1a2b3c4d"; //静态的salt，用于第一次MD5，在输入的密码后进行拼接
	
	//1.做第一次MD5：把用户输入的明文密码转换成Form表单格式
	public static String inputPassToFormPass(String inputPass) {
		//取salt中第0个字符+第2个字符+输入的密码+第5个字符+第4个字符
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
		System.out.println(str);
		return md5(str);
	}
	
	//2.做第二次MD5：把Form表单格式的密码转换成DB格式的密码
	public static String formPassToDBPass(String formPass, String salt) { //动态的salt，取随机值
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	//3.合并第一和第二次转换（MD5）：把用户输入的明文密码转换成DB格式的密码
	public static String inputPassToDbPass(String inputPass, String saltDB) {
		String formPass = inputPassToFormPass(inputPass); //1.做第一次MD5：把用户输入的明文密码转换成Form表单格式
		String dbPass = formPassToDBPass(formPass, saltDB); //2.做第二次MD5：把Form表单格式的密码转换成DB格式的密码
		return dbPass;
	}
	
	//编写主类进行测试
	public static void main(String[] args) {
		System.out.println(inputPassToDbPass("123456", "1a2b3c4d")); //b7797cce01b4b131b433b6acf4add449
	}
}
