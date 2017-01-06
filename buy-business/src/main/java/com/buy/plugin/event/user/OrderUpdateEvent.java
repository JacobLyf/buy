package com.buy.plugin.event.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 会员订单消息
 */
public class OrderUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public OrderUpdateEvent(Object source) {
		super(source);
	} 

}
