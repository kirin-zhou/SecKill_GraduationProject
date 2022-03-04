package com.kirin.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.kirin.miaosha.domain.MiaoshaOrder;
import com.kirin.miaosha.domain.MiaoshaUser;
import com.kirin.miaosha.redis.RedisService;
import com.kirin.miaosha.service.GoodsService;
import com.kirin.miaosha.service.MiaoshaService;
import com.kirin.miaosha.service.OrderService;
import com.kirin.miaosha.vo.GoodsVo;

//消息接受器
@Service
public class MQReceiver {

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class); //设置控制台输出
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE) //@RabbitListener：其中queues属性通过识别队列的名字来接受消息进行消费
	public void receive(String message) {
		log.info("receive message:"+message);
		MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class); //解析接收的信息
		MiaoshaUser user = mm.getUser(); //获取用户信息
		long goodsId = mm.getGoodsId(); //获取商品id
		
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId); //根据商品id查询
    	int stock = goods.getStockCount();
    	if(stock <= 0) { //若没有库存，则无法秒杀
    		return;
    	}
    	//若有库存，判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return;
    	}
    	//减库存 下订单 写入秒杀订单
    	miaoshaService.miaosha(user, goods);
	}

	
	//1.Direct模式：接受消息
//	@RabbitListener(queues=MQConfig.QUEUE)
//	public void receive(String message) {
//		log.info("receive message:"+message);
//	}
	
	//2.Topic模式
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info(" topic  queue1 message:"+message);
	}
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info(" topic  queue2 message:"+message);
	}
	
	//4.Header模式
	@RabbitListener(queues=MQConfig.HEADER_QUEUE)
	public void receiveHeaderQueue(byte[] message) {
		log.info(" header  queue message:"+new String(message));
	}
	
}
