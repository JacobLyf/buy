package com.buy.plugin.event.sort;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 前台分类更新事件.
 * 
 * @author Chengyb
 */
public class FrontSortUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -6390877040754301159L;

	public FrontSortUpdateEvent(Object source) {
		super(source);
    }

}