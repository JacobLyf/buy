package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品更新事件.
 * 
 * @author Chengyb
 */
public class ProductUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -7574563247221398896L;

	public ProductUpdateEvent(Object source) {
		super(source);
    }

}