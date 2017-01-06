package com.buy.plugin.event.sms.merchant;

import net.dreamlu.event.core.ApplicationEvent;

public class DeliverSmsEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;
	
	public DeliverSmsEvent(Object source)
	{
		super(source);
	}

}
