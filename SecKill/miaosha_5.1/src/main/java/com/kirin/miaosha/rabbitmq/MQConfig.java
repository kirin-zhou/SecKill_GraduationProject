package com.kirin.miaosha.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//配置类
@Configuration
public class MQConfig {
	
	public static final String MIAOSHA_QUEUE = "miaosha.queue";
	public static final String QUEUE = "queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1"; //队列1
	public static final String TOPIC_QUEUE2 = "topic.queue2"; //队列2
	public static final String HEADER_QUEUE = "header.queue"; //队列3
	public static final String TOPIC_EXCHANGE = "topicExchage"; //Topic交换器
	public static final String FANOUT_EXCHANGE = "fanoutxchage"; //Fanout交换器
	public static final String HEADERS_EXCHANGE = "headersExchage"; //Header交换器
	
	/**
	 * 秒杀队列（Direct模式）
	 * */
	@Bean
	public Queue miaosha_queue() { //创建队列
		return new Queue(MIAOSHA_QUEUE, true); //队列bean实例
	}
	
	/**
	 * 1.Direct模式 交换机Exchange：任何发送到Direct Exchange的消息都会被转发到RouteKey中指定的Queue
	 * */
	@Bean //@Bean注解是要告诉方法，产生一个Bean对象，并将这个Bean由Spring容器管理。产生这个Bean对象的方法Spring只会调用一次，随后这个Bean将放在IOC容器中。
	public Queue queue() { //创建队列
		return new Queue(QUEUE, true); //队列bean实例
	}
	
	/**
	 * 2.Topic模式 交换机Exchange：任何发送到Topic Exchange的消息都会被转发到与routingKey匹配的队列上
	 * */
	@Bean
	public Queue topicQueue1() { //创建队列1
		return new Queue(TOPIC_QUEUE1, true); //队列bean实例
	}
	@Bean
	public Queue topicQueue2() { //创建队列2
		return new Queue(TOPIC_QUEUE2, true); //队列bean实例
	}
	@Bean
	public TopicExchange topicExchage(){ //Topic交换器：先把消息放到交换机中，在把消息放到队列中
		return new TopicExchange(TOPIC_EXCHANGE);
	}
	//将队列和交换机用key绑定，只有带有特定的key才能进入特定的队列
	@Bean
	public Binding topicBinding1() { //当routingKey=topic.key1，绑定队列1和交换机
		return BindingBuilder.bind(topicQueue1()).to(topicExchage()).with("topic.key1");
	}
	@Bean
	public Binding topicBinding2() { //当routingKey=topic.#，绑定队列2和交换机
		return BindingBuilder.bind(topicQueue2()).to(topicExchage()).with("topic.#");
	}
	
	/**
	 * 3.Fanout广播模式 交换机Exchange
	 * */
	@Bean
	public FanoutExchange fanoutExchage(){ //Fanout交换器
		return new FanoutExchange(FANOUT_EXCHANGE);
	}
	@Bean
	public Binding FanoutBinding1() { //绑定队列1和交换机
		return BindingBuilder.bind(topicQueue1()).to(fanoutExchage());
	}
	@Bean
	public Binding FanoutBinding2() { //绑定队列2和交换机
		return BindingBuilder.bind(topicQueue2()).to(fanoutExchage());
	}
	
	/**
	 * 4.Header模式 交换机Exchange
	 * */
	@Bean
	public HeadersExchange headersExchage(){ //Header交换器
		return new HeadersExchange(HEADERS_EXCHANGE);
	}
	@Bean
	public Queue headerQueue1() {
		return new Queue(HEADER_QUEUE, true); //队列bean实例
	}
	@Bean
	public Binding headerBinding() { //绑定队列和交换机
		Map<String, Object> map = new HashMap<String, Object>();
		//设定匹配条件
		map.put("header1", "value1");
		map.put("header2", "value2");
		//whereAll：发送到交换机的消息中所携带的条件，必须与设定的条件全部匹配
		return BindingBuilder.bind(headerQueue1()).to(headersExchage()).whereAll(map).match();
	}
	
}
