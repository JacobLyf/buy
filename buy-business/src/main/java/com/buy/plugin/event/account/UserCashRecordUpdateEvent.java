package com.buy.plugin.event.account;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 用户现金对账单更新消息
 */
public class UserCashRecordUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public UserCashRecordUpdateEvent(Object source) {
		super(source);
	} 

}
