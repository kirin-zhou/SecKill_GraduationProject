package com.kirin.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	
	//做一次MD5
	public static String md5(String src) {
		return DigestUtils.md5Hex(src); //调用DigestUtils，实现md5处理
	}
	
	//1.做第一次MD5
	//静态的salt，用于第一次MD5，在输入的密码后进行拼接
	private static final String salt = "1a2b3c4d";
	
	//1.做第一次MD5：把用户输入的明文密码转换成Form表单格式
	public static String inputPassToFormPass(String inputPass) {
		//取salt中第0个字符+第2个字符+输入的密码+第5个字符+第4个字符
		//拼接字符时没有添加""，出现了登录验证失败的问题
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4); 
		System.out.println(str);
		return md5(str); //做一次MD5
	}
	
	//2.做第二次MD5：把Form表单格式的密码转换成DB格式的密码
	public static String formPassToDBPass(String formPass, String salt) { //动态的salt，取随机值
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str); //做一次MD5
	}
	
	//3.合并第一和第二次转换（MD5）：把用户输入的明文密码转换成DB格式的密码
	public static String inputPassToDbPass(String inputPass, String saltDB) {
		//1.做第一次MD5：把用户输入的明文密码转换成Form表单格式
		String formPass = inputPassToFormPass(inputPass);
		//2.做第二次MD5：把Form表单格式的密码转换成DB格式的密码
		String dbPass = formPassToDBPass(formPass, saltDB);
		return dbPass;
	}
	
	//编写主类进行测试
	public static void main(String[] args) {
		//1.做第一次MD5
		//System.out.println(inputPassToFormPass("123456")); //输出：d3b1294a61a07da9b49b6e22b2cbd7f9 ——> 破解后12123456c3
		//2.做第二次MD5
		//System.out.println(formPassToDBPass(inputPassToFormPass("123456"), "1a2b3c4d")); //设定动态salt的值为1a2b3c4d
		System.out.println(inputPassToDbPass("123456", "1a2b3c4d")); //b7797cce01b4b131b433b6acf4add449
	}

}
