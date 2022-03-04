package com.kirin.miaosha.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.kirin.miaosha.access.UserContext;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.service.MiaoshaUserService;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
//实现HandlerMethodArgumentResolver接口，必须重写其中的两个方法，supportsParameter()和resolveArgument()

	@Autowired
	MiaoshaUserService userService;
	
	//判断参数类型是否支持
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz==MiaoshaUser.class;
	}

	//实现对参数的处理逻辑（2种情况）：
	//1.从request中获取token值
	//2.从cookie中拿取token值，根据token值来获取到对应的user
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return UserContext.getUser();
	}
}
