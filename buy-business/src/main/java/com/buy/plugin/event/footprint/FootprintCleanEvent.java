package com.buy.plugin.event.footprint;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 我的足迹清理事件.
 * 
 * @author Chengyb
 */
public class FootprintCleanEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1232651115422437682L;

	public FootprintCleanEvent(Object source) {
		super(source);
    }

}