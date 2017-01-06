package com.buy.plugin.event.shop;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 店铺索引评分更新事件.
 * 
 * @author Chengyb
 */
public class ShopScoreUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1408389240566913036L;

	public ShopScoreUpdateEvent(Object source) {
		super(source);
    }

}