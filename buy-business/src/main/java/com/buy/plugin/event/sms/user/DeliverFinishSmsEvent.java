package com.buy.plugin.event.sms.user;

import net.dreamlu.event.core.ApplicationEvent;

public class DeliverFinishSmsEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;
	
	public DeliverFinishSmsEvent(Object source)
	{
		super(source);
	}

}
