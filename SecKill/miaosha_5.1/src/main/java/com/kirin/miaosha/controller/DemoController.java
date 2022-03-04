package com.kirin.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.classmate.util.ResolvedTypeCache.Key;
import com.kirin.miaosha.domain.User;
import com.kirin.miaosha.rabbitmq.MQSender;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.redis.UserKey;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.result.Result;
import com.kirin.miaosha.service.UserService;

@Controller
public class DemoController {
	@Autowired
    MQSender sender;
	
	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}
	
	//Controller中的方法：
	//1.rest api json输出 
	//【例】com.kirin.miaosha.result包下Result类
	@RequestMapping("/hello")
	@ResponseBody
	public Result <String> hello() {
		return Result.success("helo,kirin");
//		return new Result(0,"success","hello,kirin");
	} 
	
	@RequestMapping("/helloError")
	@ResponseBody
	public Result <String> helloError() {
		return Result.error(CodeMsg.SERVER_ERROR);
//		return new Result(500100,"session失效");
	}
	
	//U1-2 集成Thymeleaf（页面模板=JSP），Result结果封装
	@RequestMapping("/thymeleaf")
	public String Thymeleaf(Model model) { //返回页面模板
		model.addAttribute("name","kirin"); //定义一个对象
		return "hello";
		//在配置文件中配置hello的页面模板：src/main/resources/templates/hello.html
	}
	
	//U1-4 数据库测试1：获取id
	@Autowired
	UserService userService;
	
	@RequestMapping("/db/get")
	@ResponseBody
	public Result<User> dbGet() {
		User user = userService.getById(1);
		return Result.success(user);
	}
	
	//U1-4 数据库测试2：事务（插入数据）
	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean> dbTx() {
		userService.tx();
		return Result.success(true);
	}
	
	//U1-6 Redis测试：设置和获取数据
	@Autowired
	RedisService redisService;
	
	@RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
    	User user = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }
    
    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
    	User user = new User();
    	user.setId(1);
    	user.setName("1111");
    	redisService.set(UserKey.getById, ""+1, user); //UserKey:id1
        return Result.success(true);
    }
    
    /*
    //U6-3 RabbitMQ测试发送接收数据
	//U6-3 1.Direct模式
    @RequestMapping("/mq")
	@ResponseBody
	public Result <String> mq() {
    	sender.send("hello,kirin");
    	return Result.success("Helo,Kirin");
	}
    
	//U6-3 2.Topic模式
    @RequestMapping("/mq/topic")
	@ResponseBody
	public Result<String> topic() {
		sender.sendTopic("hello,kirin");
		return Result.success("Helo,Kirin");
	}
    
	//U6-3 3.Fanout模式
	@RequestMapping("/mq/fanout")
	@ResponseBody
	public Result<String> fanout() {
		sender.sendFanout("hello,kirin");
		return Result.success("Helo,Kirin");
	}
	
	//U6-3 4.Header模式
	@RequestMapping("/mq/header")
	@ResponseBody
	public Result<String> header() {
		sender.sendHeader("hello,kirin");
		return Result.success("Helo,Kirin");
	}
	*/
}
