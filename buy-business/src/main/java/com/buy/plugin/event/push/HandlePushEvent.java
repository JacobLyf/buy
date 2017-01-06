package com.buy.plugin.event.push;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 手动推送 
 */
public class HandlePushEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	public HandlePushEvent(Object source)
	{
		super(source);
	}

}
