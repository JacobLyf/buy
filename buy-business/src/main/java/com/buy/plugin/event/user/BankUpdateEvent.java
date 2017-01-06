package com.buy.plugin.event.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 会员银行卡修改消息
 */
public class BankUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public BankUpdateEvent(Object source) {
		super(source);
	} 

}
