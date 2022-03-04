package com.kirin.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.domain.OrderInfo;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.result.CodeMsg;
import com.kirin.miaosha.result.Result;
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
	@RequestMapping(value="/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> miaosha(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) { //若无法获取用户，则返回登录页
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//根据商品id查询，10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) { //已经秒杀到商品，不能再秒杀
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
    }
}
