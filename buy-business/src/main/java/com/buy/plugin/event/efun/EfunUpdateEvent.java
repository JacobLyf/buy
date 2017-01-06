package com.buy.plugin.event.efun;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 幸运一折购更新事件.
 * 
 * @author Chengyb
 */
public class EfunUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5786780488327985124L;

	public EfunUpdateEvent(Object source) {
		super(source);
    }

}