package com.buy.plugin.event.cart;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 购物车数量事件
 */
public class CartCountEvent extends ApplicationEvent { 

	private static final long serialVersionUID = 6994987952247306131L;
	
	public CartCountEvent(Object source) {
		super(source);
		
	}
 
}