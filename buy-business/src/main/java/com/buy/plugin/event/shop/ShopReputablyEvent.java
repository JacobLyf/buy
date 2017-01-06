package com.buy.plugin.event.shop;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 店铺好评率评分
 */
public class ShopReputablyEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public ShopReputablyEvent(Object source) {
		super(source);
	}

}
