package com.buy.plugin.event.pos.product;

import net.dreamlu.event.core.ApplicationEvent;

public class PushPosProductEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public PushPosProductEvent(Object source) {
		super(source);
	}

}
