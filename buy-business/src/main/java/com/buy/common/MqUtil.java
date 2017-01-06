package com.buy.common;

import com.buy.plugin.event.mq.MqSendEvent;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.EventKit;

/**
 * 发送对接
 * @author huangzq
 *
 */
public class MqUtil {
	/**
	 * 发送消息
	 * @param queueName 队列名称
	 * @param content 消息内容
	 */
	public static void send(String queueName,String content){
		Record r = new Record();
		r.set("routingKey", queueName);
		r.set("content", content);
		EventKit.postEvent(new MqSendEvent(r));
	}
	

}
