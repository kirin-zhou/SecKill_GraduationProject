package com.kirin.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kirin.miaosha.redis.RedisService;

//消息发送器
@Service
public class MQSender {

	private static Logger log = LoggerFactory.getLogger(MQSender.class); //设置控制台输出
	
	@Autowired
	AmqpTemplate amqpTemplate ;
	
	//秒杀入队（Direct模式）：向队列中插数据
	public void sendMiaoshaMessage(MiaoshaMessage mm) {
		String msg = RedisService.beanToString(mm);
		log.info("send message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg); //队列名称，信息
	}
	
	/*
	//1.Direct模式：向队列中插（发送）数据
	public void send(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
	}
	
	//2.Topic模式
	public void sendTopic(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send topic message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg+"1"); //可以匹配topic.key1（topicQueue1()）和topic.#（topicQueue2()）
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg+"2"); //只能匹配topic.#（topicQueue2()）
	}

	//3.Fanout模式
	public void sendFanout(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send fanout message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg); //2个队列都绑定了交换机，所以2个队列（topicQueue1()、topicQueue2()）都能收到
	}

	//4.Header模式
	public void sendHeader(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send fanout message:"+msg);
		MessageProperties properties = new MessageProperties();
		//其中携带的条件，与交换机绑定的队列条件匹配
		properties.setHeader("header1", "value1");
		properties.setHeader("header2", "value2");
		Message obj = new Message(msg.getBytes(), properties);
		amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
	}
	*/
	
}

