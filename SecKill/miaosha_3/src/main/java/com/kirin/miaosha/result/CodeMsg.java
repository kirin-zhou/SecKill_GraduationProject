package com.kirin.miaosha.result;

public class CodeMsg {
	private int code;
	private String msg;
	
	//定义通用异常
	public static CodeMsg SUCCESS = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务端异常");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s"); //带参数
    
	//登录模块 5002XX
  	public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");
  	public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "登录密码不能为空");
  	public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
  	public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
  	public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
  	public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");

	//商品模块5003XX

	//订单模块5004XX
  	public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500400, "订单不存在");

	//秒杀模块5005XX
  	public static CodeMsg MIAO_SHA_OVER = new CodeMsg(500500, "商品已经秒杀完毕");
	public static CodeMsg REPEATE_MIAOSHA = new CodeMsg(500501, "不能重复秒杀");

  	private CodeMsg( ) {
	}
			
	private CodeMsg( int code,String msg ) {
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	//定义带参数的CodeMsg
	//其中String.format()能够根据传入的字符串格式，比如"参数校验异常：%s"，其中%s，能被第二个传入的参数进行替换，从而形成动态的字符串
	public CodeMsg fillArgs(Object... args) {
		int code = this.code;
		String message = String.format(this.msg, args); //把原始的message拼接上参数
		return new CodeMsg(code, message);
	}

	@Override
	public String toString() {
		return "CodeMsg [code=" + code + ", msg=" + msg + "]";
	}
}
