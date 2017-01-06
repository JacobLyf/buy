package com.buy.plugin.event.shop;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 店铺删除事件.
 * 
 * @author Chengyb
 */
public class ShopDeleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 5825378757893216675L;

	public ShopDeleteEvent(Object source) {
		super(source);
    }

}