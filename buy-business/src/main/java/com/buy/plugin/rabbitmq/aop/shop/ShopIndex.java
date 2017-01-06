package com.buy.plugin.rabbitmq.aop.shop;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.buy.model.MQSendError;
import com.buy.plugin.rabbitmq.Message;
import com.buy.plugin.rabbitmq.RabbitMQ;
import com.buy.plugin.rabbitmq.RabbitMQConstants;
import com.buy.plugin.rabbitmq.model.IndexMessage;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import com.buy.plugin.rabbitmq.wrapper.ModelType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.BasicProperties;

public class ShopIndex {
	
	public static void send(Integer appAdId, ExchangeType type, Integer priority) {
		Channel channel = null;
		try {
			channel = RabbitMQ.conn.createChannel();
			new Message(new BasicProperties().builder().deliveryMode(Message.DELIVERY_MODE_PERSISTENT)
					.priority(priority).build()).queue(RabbitMQConstants.SHOP_QUEUE)
							.exchange(RabbitMQConstants.SHOP_EXCHANGE).routingKey(RabbitMQConstants.SHOP_ROUTINGKEY)
							.body(JSONObject.toJSONString(new IndexMessage(ModelType.SHOP, appAdId, type))).publish(channel, null);
		} catch (Exception e) {
			e.printStackTrace();

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.SHOP.toString());
			mqSendError.set("operate", type.toString());
			mqSendError.set("mq_message", JSONObject.toJSONString(new IndexMessage(ModelType.SHOP, type)));
			mqSendError.set("error_message", e.getMessage());
			mqSendError.set("create_t", new Date());
			mqSendError.save();
		} finally {
			if(null != channel) {
				try {
					channel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void send(String shopId, ExchangeType type) {
		Channel channel = null;
		try {
			channel = RabbitMQ.conn.createChannel();
			new Message().queue(RabbitMQConstants.SHOP_QUEUE).exchange(RabbitMQConstants.SHOP_EXCHANGE)
					.routingKey(RabbitMQConstants.SHOP_ROUTINGKEY)
					.body(JSONObject.toJSONString(new IndexMessage(ModelType.SHOP, shopId, type))).publish(channel,null);
		} catch (Exception e) {
			e.printStackTrace();

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.SHOP.toString());
			mqSendError.set("id", shopId);
			mqSendError.set("operate", type.toString());
			mqSendError.set("mq_message", JSONObject.toJSONString(new IndexMessage(ModelType.SHOP, shopId, type)));
			mqSendError.set("error_message", e.getMessage());
			mqSendError.set("create_t", new Date());
			mqSendError.save();
		} finally {
			if(null != channel) {
				try {
					channel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}