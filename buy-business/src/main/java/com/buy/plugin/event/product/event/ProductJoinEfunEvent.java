package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品加入幸运一折购事件.
 * 
 * @author Chengyb
 */
public class ProductJoinEfunEvent extends ApplicationEvent {

	private static final long serialVersionUID = -4011069547445156558L;

	public ProductJoinEfunEvent(Object source) {
		super(source);
    }

}