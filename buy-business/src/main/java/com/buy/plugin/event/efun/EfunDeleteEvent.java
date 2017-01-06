package com.buy.plugin.event.efun;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 幸运一折购删除事件.
 * 
 * @author Chengyb
 */
public class EfunDeleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 9190345771196128962L;

	public EfunDeleteEvent(Object source) {
		super(source);
    }

}