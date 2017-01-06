package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品删除事件.
 * 
 * @author Chengyb
 */
public class ProductDeleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3157861187543971761L;

	public ProductDeleteEvent(Object source) {
		super(source);
    }

}