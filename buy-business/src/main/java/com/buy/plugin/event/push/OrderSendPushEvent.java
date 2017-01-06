package com.buy.plugin.event.push;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 推送
 */
public class OrderSendPushEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	public OrderSendPushEvent(Object source)
	{
		super(source);
	}

}
