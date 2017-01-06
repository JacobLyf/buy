package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品销量更新事件.
 * 
 * @author Chengyb
 */
public class ProductSalesCountEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6022372516062531938L;

	public ProductSalesCountEvent(Object source) {
		super(source);
    }

}