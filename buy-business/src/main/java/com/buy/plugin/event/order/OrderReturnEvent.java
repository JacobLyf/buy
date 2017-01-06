package com.buy.plugin.event.order;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 后台订单发货触发点
 * @author huangzq
 *
 */
public class OrderReturnEvent extends ApplicationEvent { 

	private static final long serialVersionUID = 6994987952247306131L;
	
	public OrderReturnEvent(Object source) {
		super(source);
	}
 
}