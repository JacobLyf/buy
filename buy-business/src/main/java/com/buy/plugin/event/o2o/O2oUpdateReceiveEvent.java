package com.buy.plugin.event.o2o;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * O2O改价事件
 * @author allon
 *
 */
public class O2oUpdateReceiveEvent extends ApplicationEvent {



	private static final long serialVersionUID = -6460553498402664859L;

	public O2oUpdateReceiveEvent(Object source) {
		super(source);
    }

}