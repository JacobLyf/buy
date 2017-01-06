package com.buy.plugin.event.search;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 会员最近搜索记录事件.
 * 
 * @author Chengyb
 */
public class RecentSearchEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5518013164177367128L;

	public RecentSearchEvent(Object source) {
		super(source);
    }

}