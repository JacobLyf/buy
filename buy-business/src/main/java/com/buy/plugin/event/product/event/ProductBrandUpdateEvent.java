package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品品牌更新事件.
 * 
 * @author Chengyb
 */
public class ProductBrandUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -363810040605217367L;

	public ProductBrandUpdateEvent(Object source) {
		super(source);
    }

}