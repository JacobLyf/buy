package com.buy.plugin.event.sms.shop;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 开店审核
 */
public class ApplyUpdateEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	public ApplyUpdateEvent(Object source) {
		super(source);
	}

}
