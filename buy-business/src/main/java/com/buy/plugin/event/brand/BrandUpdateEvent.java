package com.buy.plugin.event.brand;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 品牌更新事件.
 * 
 * @author Chengyb
 */
public class BrandUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -3084870873913985452L;

	public BrandUpdateEvent(Object source) {
		super(source);
    }

}