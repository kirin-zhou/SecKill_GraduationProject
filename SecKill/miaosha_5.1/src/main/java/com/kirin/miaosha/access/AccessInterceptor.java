package com.kirin.miaosha.access;

import java.io.OutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.alibaba.fastjson.JSON;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.redis.AccessKey;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.result.Result;
import com.kirin.miaosha.service.MiaoshaUserService;

//拦截器
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter{ //继承HandlerInterceptorAdapter，重写preHandle方法
	
	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if(handler instanceof HandlerMethod) {
			MiaoshaUser user = getUser(request, response); //取用户
			UserContext.setUser(user); //保存用户
			HandlerMethod hm = (HandlerMethod)handler;
			//处理方法的对象，获取的是方法的注解
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if(accessLimit == null) {
				return true; //这里应该写成true，没有被该注解标记的全部放行才对
			}
			int seconds = accessLimit.seconds(); //限定访问几秒
			int maxCount = accessLimit.maxCount(); //最多访问次数
			boolean needLogin = accessLimit.needLogin(); //需不需要登录
			String key = request.getRequestURI(); //获取请求的地址
			if(needLogin) {
				if(user == null) { //若user为空，递交错误信息
					render(response, CodeMsg.SESSION_ERROR); //提示信息
					return false;
				}
				key += "_" + user.getId();
			}else {
				//do nothing
			}
			AccessKey ak = AccessKey.withExpire(seconds);
			Integer count = redisService.get(ak, key, Integer.class);
	    	if(count == null) { //若规定时间内，再有访问，则value+1
	    		//第一次访问的时候，向redis中存储值，key为目标地址和用户id，value为访问次数，并有过期时间，每次访问都将该值与访问的限制最大值进行比对，超过规定的次数返回错误信息
	    		redisService.set(ak, key, 1);
	    	}else if(count < maxCount) { //若规定时间内，没有超过数值，且到了下一个时间，则value值归0，重新计数
	    		redisService.incr(ak, key);
	    	}else { //若规定时间内，value值超过限定数值，则返回访问太频繁
	    		render(response, CodeMsg.ACCESS_LIMIT_REACHED);
	    		return false;
	    	}
		}
		return true;
	}
	
	//提示信息
	private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		OutputStream out = response.getOutputStream();
		String str  = JSON.toJSONString(Result.error(cm));
		out.write(str.getBytes("UTF-8"));
		out.flush();
		out.close();
	}

	//同步：取用户
	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
		String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
		String cookieToken = getCookieValue(request, MiaoshaUserService.COOKI_NAME_TOKEN);
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}
	
	//同步
	private String getCookieValue(HttpServletRequest request, String cookiName) {
		Cookie[]  cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookiName)) {
				return cookie.getValue();
			}
		}
		return null;
	}
	
}