package com.buy.plugin.event.user.sign;

import net.dreamlu.event.core.ApplicationEvent;

public class UserSignEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;
	
	public UserSignEvent(Object source)
	{
		super(source);
	}

}
