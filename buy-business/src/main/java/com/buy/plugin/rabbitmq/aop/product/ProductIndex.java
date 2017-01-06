package com.buy.plugin.rabbitmq.aop.product;

import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.buy.model.MQSendError;
import com.buy.plugin.rabbitmq.Message;
import com.buy.plugin.rabbitmq.RabbitMQ;
import com.buy.plugin.rabbitmq.RabbitMQConstants;
import com.buy.plugin.rabbitmq.model.IndexMessage;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import com.buy.plugin.rabbitmq.wrapper.ModelType;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

public class ProductIndex {

	private static final Logger L = Logger.getLogger(ProductIndex.class);

	public static void send(ExchangeType type, Integer priority) {
		Channel channel = null;
		try {
			channel = RabbitMQ.conn.createChannel();
			new Message(new BasicProperties().builder().deliveryMode(Message.DELIVERY_MODE_PERSISTENT)
					.priority(priority).build()).queue(RabbitMQConstants.PRODUCT_QUEUE)
							.exchange(RabbitMQConstants.PRODUCT_EXCHANGE)
							.routingKey(RabbitMQConstants.PRODUCT_ROUTINGKEY)
							.body(JSONObject.toJSONString(new IndexMessage(ModelType.PRODUCT, type)))
							.publish(channel,null);
		} catch (Exception e) {
			e.printStackTrace();
			L.error(e.getMessage());

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.PRODUCT.toString());
			mqSendError.set("operate", type.toString());
			mqSendError.set("mq_message", JSONObject.toJSONString(new IndexMessage(ModelType.PRODUCT, type)));
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
	
	public static void send(Object id, ExchangeType type, Integer priority) {
		Channel channel = null;
		try {
			channel = RabbitMQ.conn.createChannel();
			new Message(new BasicProperties().builder().deliveryMode(Message.DELIVERY_MODE_PERSISTENT)
					.priority(priority).build()).queue(RabbitMQConstants.PRODUCT_QUEUE)
							.exchange(RabbitMQConstants.PRODUCT_EXCHANGE)
							.routingKey(RabbitMQConstants.PRODUCT_ROUTINGKEY)
							.body(JSONObject.toJSONString(new IndexMessage(ModelType.PRODUCT, id, type)))
							.publish(channel, null);
		} catch (Throwable t) {
			t.printStackTrace();
			L.error("",t);

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.PRODUCT.toString());
			mqSendError.set("id", id.toString());
			mqSendError.set("operate", type.toString());
			mqSendError.set("mq_message", JSONObject.toJSONString(new IndexMessage(ModelType.PRODUCT, id, type)));
			mqSendError.set("error_message", t.getMessage());
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

	public static void send(List<Integer> idList, ExchangeType type) {
		Channel channel = null;
		try {
			channel = RabbitMQ.conn.createChannel();
			new Message().queue(RabbitMQConstants.PRODUCT_QUEUE).exchange(RabbitMQConstants.PRODUCT_EXCHANGE)
					.routingKey(RabbitMQConstants.PRODUCT_ROUTINGKEY)
					.body(JSONObject.toJSONString(new IndexMessage(ModelType.PRODUCT, idList, type))).publish(channel, null);
		} catch (Exception e) {
			L.error(e.getMessage());

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.PRODUCT.toString());
			mqSendError.set("id", idList.toString());
			mqSendError.set("operate", type);
			mqSendError.set("message", e.getMessage());
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