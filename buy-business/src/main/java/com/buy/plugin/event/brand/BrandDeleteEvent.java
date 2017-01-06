package com.buy.plugin.event.brand;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 品牌删除事件.
 * 
 * @author Chengyb
 */
public class BrandDeleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 215712643071936052L;

	public BrandDeleteEvent(Object source) {
		super(source);
    }

}