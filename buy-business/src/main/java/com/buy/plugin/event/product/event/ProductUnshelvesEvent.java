package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品下架事件.
 * 
 * @author Chengyb
 */
public class ProductUnshelvesEvent extends ApplicationEvent {

	private static final long serialVersionUID = -6460553498402664859L;

	public ProductUnshelvesEvent(Object source) {
		super(source);
    }

}