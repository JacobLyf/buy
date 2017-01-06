package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品幸运一折购价格更新事件.
 * 
 * @author Chengyb
 */
public class ProductEfunPriceUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5953985424108250930L;

	public ProductEfunPriceUpdateEvent(Object source) {
		super(source);
    }

}