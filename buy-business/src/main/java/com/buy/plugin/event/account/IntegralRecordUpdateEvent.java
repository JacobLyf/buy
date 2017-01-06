package com.buy.plugin.event.account;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 用户积分对账单更新消息
 */
public class IntegralRecordUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public IntegralRecordUpdateEvent(Object source) {
		super(source);
	} 

}
