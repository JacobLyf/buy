package com.buy.plugin.rabbitmq;

import com.jfinal.plugin.IPlugin;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQPlugin implements IPlugin {

	private String host;
	private Integer port;
	private String username;
	private String password;
	
	private ConnectionFactory factory;
	
	public RabbitMQPlugin(String host, Integer port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	@Override
	public boolean start() {
		try {
		    synchronized (this) {
		    	if(null == factory) {
		    		ConnectionFactory connectionFactory = new SingleConnectionFactory();
					connectionFactory.setHost(host);
					connectionFactory.setPort(port);
					connectionFactory.setUsername(username);
					connectionFactory.setPassword(password);
					
					RabbitMQ.factory = connectionFactory;
		    	}
		    	if(null == RabbitMQ.conn) {
		    		RabbitMQ.conn = RabbitMQ.factory.newConnection();
		    	}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean stop() {
		return false;
	}

	public ConnectionFactory getFactory() {
		return factory;
	}

}
