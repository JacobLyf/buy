package com.buy.plugin.event.shop;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 店铺创建事件.
 * 
 * @author Chengyb
 */
public class ShopCreateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3305777656274035611L;

	public ShopCreateEvent(Object source) {
		super(source);
    }

}