package com.kirin.miaosha.result;

public class Result<T> { //<T>：泛型（广泛的类型）
	private int code;
	private String msg;
	private T data;  //不知道data的类型，则定义为T，在类后声明<T>，表示T为泛型
	
	
	//com.kirin.miaosha.controller包下DemoController.java
	//成功时调用
	public static <T> Result<T> success(T data){
		return new Result<T>(data); //写完后报错：生成一个构造函数private Result(T data)
		
	}
	
	//失败时调用
	public static <T> Result<T> error(CodeMsg cm){ //报错：自动生成CodeMsg.java类
		return new Result<T>(cm); //写完后报错：生成一个构造函数private Result(CodeMsg cm)
	}
	
	//public static <T> Result<T> success(T data)报错后，自动生成的构造函数
	private Result(T data) {
		this.code = 0; //成功
		this.msg = "success";
		this.data = data;
	}
	
	//public static <T> Result<T> error(CodeMsg cm)报错后，自动生成的构造函数
	private Result(CodeMsg cm) {
		if(cm == null) {
			return;
		}
		this.code = cm.getCode();
		this.msg = cm.getMsg();  
	}

	
	//生成get、set方法（后来发现用不到set方法，删除）
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
	public T getData() {
		return data;
	}
}
