package com.kirin.miaosha.exception;

import com.kirin.miaosha.result.CodeMsg;

//全局异常就比较简单了，它继承了RuntimeException类，其中包含我们需要返回的信息CodeMsg的字段
public class GlobalException extends RuntimeException { 

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}

	public CodeMsg getCm() {
		return cm;
	}
}
