package com.buy.plugin.event.activity;

import net.dreamlu.event.core.ApplicationEvent;

public class OlympicPrizeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6461788554448343452L;
	
	/**
	 * 里约奥运会奖品事件
	 * @param source
	 */
	public OlympicPrizeEvent(Object source) {
		super(source);
	}

}
