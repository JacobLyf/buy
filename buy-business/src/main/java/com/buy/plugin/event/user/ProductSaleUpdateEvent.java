package com.buy.plugin.event.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 商品降价消息
 */
public class ProductSaleUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public ProductSaleUpdateEvent(Object source) {
		super(source);
	} 

}
