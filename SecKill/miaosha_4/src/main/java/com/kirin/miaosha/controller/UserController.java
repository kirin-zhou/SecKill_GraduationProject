package com.kirin.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.Result;
import com.kirin.miaosha.service.MiaoshaUserService;

//压测：获取用户
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model,MiaoshaUser user) {
        return Result.success(user);
    }   
}