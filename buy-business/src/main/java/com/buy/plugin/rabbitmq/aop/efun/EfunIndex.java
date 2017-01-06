package com.buy.plugin.rabbitmq.aop.efun;

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

public class EfunIndex {

	public static void send(String skuCode, ExchangeType type) {
		Channel channel = null;
		try {
			if (null != skuCode) {
				channel = RabbitMQ.conn.createChannel();

				new Message().queue(RabbitMQConstants.EFUN_QUEUE).exchange(RabbitMQConstants.EFUN_EXCHANGE)
						.routingKey(RabbitMQConstants.EFUN_ROUTINGKEY)
						.body(JSONObject.toJSONString(new IndexMessage(ModelType.EFUN, skuCode, type)))
						.publish(channel,null);
			}
		} catch (Exception e) {
			e.printStackTrace();

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.EFUN.toString());
			mqSendError.set("id", skuCode);
			mqSendError.set("operate", type.toString());
			mqSendError.set("mq_message", JSONObject.toJSONString(new IndexMessage(ModelType.EFUN, skuCode, type)));
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