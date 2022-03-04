package com.kirin.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.service.GoodsService;
import com.kirin.miaosha.service.MiaoshaService;
import com.kirin.miaosha.service.MiaoshaUserService;
import com.kirin.miaosha.service.OrderService;
import com.kirin.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	/*
	 * 2.miaosha.jmx
	 * QPS=146.9/s
	 * 并发10000 = 1000个线程 * 10次循环
	 */
	//秒杀表单提交
    @RequestMapping("/do_miaosha")
    public String list(Model model,MiaoshaUser user,@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) { //若无法获取用户，则返回登录页
    		return "login";
    	}
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId); //根据商品id查询
    	int stock = goods.getStockCount();
    	if(stock <= 0) { //没有库存
    		model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
    		return "miaosha_fail";
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) { //已经秒杀到商品，不能再秒杀
    		model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
    		return "miaosha_fail";
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
    	model.addAttribute("orderInfo", orderInfo);
    	model.addAttribute("goods", goods);
        return "order_detail";
    }
}
