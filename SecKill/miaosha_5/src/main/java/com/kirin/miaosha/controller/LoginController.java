package com.kirin.miaosha.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.classmate.util.ResolvedTypeCache.Key;
import com.kirin.miaosha.domain.User;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.redis.UserKey;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.result.Result;
import com.kirin.miaosha.service.MiaoshaUserService;
import com.kirin.miaosha.service.UserService;
import com.kirin.miaosha.util.ValidatorUtil;
import com.kirin.miaosha.vo.LoginVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/login")
public class LoginController {

	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
    @RequestMapping("/to_login")
    public String toLogin() {
        return "login"; //返回页面
    }
    
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) { //2.JSR303参数检验
    	log.info(loginVo.toString()); //在LoginVo.java中生成toString()
    	//1.自己写的参数校验
//    	String passInput = loginVo.getPassword();
//    	String mobile = loginVo.getMobile();
//    	if(StringUtils.isEmpty(passInput)) {
//    		return Result.error(CodeMsg.PASSWORD_EMPTY);
//    	}
//    	if(StringUtils.isEmpty(mobile)) {
//    		return Result.error(CodeMsg.MOBILE_EMPTY);
//    	}
//    	if(!ValidatorUtil.isMobile(mobile)) { //验证mobile的输入的格式，是否是以1开头，后跟10个数字
//    		return Result.error(CodeMsg.MOBILE_ERROR);
//    	}
    	
    	//登录
    	String token = userService.login(response,loginVo);
    	return Result.success(token);
//    	if(cm.getCode() == 0) {
//    		return Result.success(true);
//    	}else {
//			return Result.error(cm);
//		}
    }
}
