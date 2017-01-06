package com.buy.plugin.event.account;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 店铺现金对账单更新消息
 */
public class ShopCashRecordUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public ShopCashRecordUpdateEvent(Object source) {
		super(source);
	} 

}
