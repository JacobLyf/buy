package com.buy.plugin.event.sort;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 前台分类关联事件.
 * 
 * @author Chengyb
 */
public class FrontSortRelevanceEvent extends ApplicationEvent {

	private static final long serialVersionUID = -2027239096157869866L;

	public FrontSortRelevanceEvent(Object source) {
		super(source);
    }

}