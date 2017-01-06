package com.buy.plugin.event.sort;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 后台三级分类状态更新事件.
 * 
 * @author Chengyb
 */
public class BackSortStatusUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3977988676455237006L;

	public BackSortStatusUpdateEvent(Object source) {
		super(source);
    }

}