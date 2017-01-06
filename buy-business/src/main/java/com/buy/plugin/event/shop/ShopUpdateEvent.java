package com.buy.plugin.event.shop;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 店铺更新事件.
 * 
 * @author Chengyb
 */
public class ShopUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -7574563247221398896L;

	public ShopUpdateEvent(Object source) {
		super(source);
    }

}