package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品幸运一折购评分更新事件.
 * 
 * @author Chengyb
 */
public class ProductEfunScoreUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -4584058594666126391L;

	public ProductEfunScoreUpdateEvent(Object source) {
		super(source);
    }

}