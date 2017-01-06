package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品创建事件.
 * 
 * @author Chengyb
 */
public class ProductCreateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3045430397248651931L;

	public ProductCreateEvent(Object source) {
		super(source);
    }

}