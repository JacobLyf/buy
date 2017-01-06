package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品上架事件.
 * 
 * @author Chengyb
 */
public class ProductShelvesEvent extends ApplicationEvent {

	private static final long serialVersionUID = 2665798763209059082L;

	public ProductShelvesEvent(Object source) {
		super(source);
    }

}