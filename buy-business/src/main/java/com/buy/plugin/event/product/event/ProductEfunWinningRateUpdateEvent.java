package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品幸运一折购历史开奖率更新事件.
 * 
 * @author Chengyb
 */
public class ProductEfunWinningRateUpdateEvent extends ApplicationEvent {

	private static final long serialVersionUID = -5608284846782027771L;

	public ProductEfunWinningRateUpdateEvent(Object source) {
		super(source);
    }

}