package com.buy.plugin.event.sort;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 前台分类创建事件.
 * 
 * @author Chengyb
 */
public class FrontSortCreateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3317863893600471053L;

	public FrontSortCreateEvent(Object source) {
		super(source);
    }

}