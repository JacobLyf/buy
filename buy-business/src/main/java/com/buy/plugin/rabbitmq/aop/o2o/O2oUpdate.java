package com.buy.plugin.rabbitmq.aop.o2o;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.buy.model.MQSendError;
import com.buy.plugin.rabbitmq.Message;
import com.buy.plugin.rabbitmq.RabbitMQ;
import com.buy.plugin.rabbitmq.RabbitMQConstants;
import com.buy.plugin.rabbitmq.model.IndexMessage;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import com.buy.plugin.rabbitmq.wrapper.ModelType;
import com.jfinal.plugin.activerecord.Record;
import com.rabbitmq.client.Channel;

public class O2oUpdate {

	public static void send(Integer o2oApplyId, ExchangeType type) {
		Channel channel = null;
		try {
			channel = RabbitMQ.conn.createChannel();
			Map<String, Object>  args = new HashMap<String, Object>();
			//延迟1小时
			args.put("x-message-ttl", 60*60*1000);
		    args.put("x-dead-letter-exchange", RabbitMQConstants.O2O_EXCHANGE);
		    args.put("x-dead-letter-routing-key", RabbitMQConstants.O2O_ROUTINGKEY);
		    Record r = new Record();
		    r.set("id", o2oApplyId);
		    r.set("ModelType", ModelType.O2O);
		    r.set("exchangeType", type);
			new Message().queue(RabbitMQConstants.O2O_DELAY_QUEUE).exchange(RabbitMQConstants.O2O_DELAY_EXCHANGE)
					.routingKey(RabbitMQConstants.O2O_DELAY_ROUTINGKEY)
					.body(JSONObject.toJSONString(r)).publish(channel,args);
		} catch (Exception e) {
			e.printStackTrace();

			MQSendError mqSendError = new MQSendError();
			mqSendError.set("model", ModelType.O2O.toString());
			mqSendError.set("id", o2oApplyId);
			mqSendError.set("operate", type.toString());
			mqSendError.set("mq_message", JSONObject.toJSONString(new IndexMessage(ModelType.O2O, o2oApplyId, type)));
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