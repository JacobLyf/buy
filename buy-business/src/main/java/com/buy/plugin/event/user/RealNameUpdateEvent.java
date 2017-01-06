package com.buy.plugin.event.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 会员实名认证消息
 */
public class RealNameUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public RealNameUpdateEvent(Object source) {
		super(source);
	} 

}
