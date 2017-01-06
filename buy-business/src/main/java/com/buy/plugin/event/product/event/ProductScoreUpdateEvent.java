package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品评分更新事件.
 * 
 * @author Chengyb
 */
public class ProductScoreUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = 3674706456295743687L;

	public ProductScoreUpdateEvent(Object source) {
		super(source);
    }

}