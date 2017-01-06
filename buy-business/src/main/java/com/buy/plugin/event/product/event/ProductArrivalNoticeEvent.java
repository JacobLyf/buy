package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品到货通知事件.
 * 
 * @author Chengyb
 */
public class ProductArrivalNoticeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 7560263929337497217L;

	public ProductArrivalNoticeEvent(Object source) {
		super(source);
	}

}