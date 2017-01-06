package com.buy.plugin.event.sms.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 幸运一折购中奖
 */
public class EfunUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public EfunUpdateEvent(Object source) {
		super(source);
	}

}
