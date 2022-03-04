package com.kirin.miaosha.exception;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.result.Result;

//全局异常处理器：处理登录异常没有显示在页面上的问题
@ControllerAdvice //它是增强的Controller，能够实现全局异常处理和全局数据绑定
@ResponseBody
public class GlobalExceptionHandler {
	//配合@ExceptionHandler(value = Exception.class)，它能够实现对所有异常的接受，而在方法中，对不同的异常进行处理
	@ExceptionHandler(value=Exception.class) //拦截所有的异常
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		e.printStackTrace();
		if(e instanceof GlobalException) {
			GlobalException ex = (GlobalException)e;
			return Result.error(ex.getCm());
		}else if(e instanceof BindException) {
			BindException ex = (BindException)e; //获取错误列表，拿取其中的第一个
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String msg = error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
		}else {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}

