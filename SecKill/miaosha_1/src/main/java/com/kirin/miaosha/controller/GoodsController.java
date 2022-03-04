package com.kirin.miaosha.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.service.GoodsService;
import com.kirin.miaosha.service.MiaoshaUserService;
import com.kirin.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	//1.用户登录后，跳转到商品列表页
	@RequestMapping("/to_list")
    public String list(Model model,MiaoshaUser user) {
    	model.addAttribute("user", user);
    	//查询商品列表
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
    	model.addAttribute("goodsList", goodsList); //往前台传数据
        return "goods_list";
    }
    
	//2.查看商品详情
	//@RequestMapping指定的映射URL，其中有用{}括起来的参数，在方法的形参处，用@PathVariable注解对其进行获取
    @RequestMapping("/to_detail/{goodsId}") //根据id获取商品
    public String detail(Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	model.addAttribute("goods", goods);
    	
    	long startAt = goods.getStartDate().getTime(); //秒杀开始时间
    	long endAt = goods.getEndDate().getTime(); //秒杀结束时间
    	long now = System.currentTimeMillis(); //现在的时间
    	
    	int miaoshaStatus = 0; //设置秒杀状态
    	int remainSeconds = 0; //剩余时间（秒）
    	if(now < startAt ) { //秒杀还没开始，倒计时
    		miaoshaStatus = 0; //miaoshaStatus=0：秒杀还没开始
    		remainSeconds = (int)((startAt - now )/1000);
    	}else if(now > endAt){ //秒杀已经结束
    		miaoshaStatus = 2; //miaoshaStatus=2：秒杀已经结束
    		remainSeconds = -1;
    	}else { //秒杀进行中
    		miaoshaStatus = 1; //miaoshaStatus=1：秒杀进行中
    		remainSeconds = 0;
    	}
    	model.addAttribute("miaoshaStatus", miaoshaStatus); //往前台传数据
    	model.addAttribute("remainSeconds", remainSeconds);
        return "goods_detail";
    }
}
