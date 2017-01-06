package com.buy.plugin.event.order;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 订单扣库存跟自动分配发货仓库事件
 */
public class OrderStoreEvent extends ApplicationEvent { 

	private static final long serialVersionUID = 6994987952247306131L;
	
	public OrderStoreEvent(Object source) {
		super(source);
	}
 
}