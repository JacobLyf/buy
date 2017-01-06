package com.buy.plugin.event.supplier;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 分类更新事件.
 * 
 * @author Chengyb
 */
public class SupplierUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -6390877040754301159L;

	public SupplierUpdateEvent(Object source) {
		super(source);
    }

}