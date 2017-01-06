package com.buy.plugin.event.efun;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 幸运一折购创建事件.
 * 
 * @author Chengyb
 */
public class EfunCreateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 8809377120308539924L;

	public EfunCreateEvent(Object source) {
		super(source);
    }

}