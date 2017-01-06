package com.buy.plugin.rabbitmq;

import net.dreamlu.event.EventKit;

import com.buy.plugin.event.o2o.O2oUpdateReceiveEvent;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQ {

	public volatile static ConnectionFactory factory;

	public volatile static Connection conn;

	public static void receive() {
		// o2o商品修改接收监听事件
		EventKit.postEvent(new O2oUpdateReceiveEvent(new Object()));

	}

}