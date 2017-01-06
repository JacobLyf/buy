package com.buy.plugin.event.pos.order;

import net.dreamlu.event.core.ApplicationEvent;

public class PushPosOrderReturnEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public PushPosOrderReturnEvent(Object source) {
		super(source);
	}

}
