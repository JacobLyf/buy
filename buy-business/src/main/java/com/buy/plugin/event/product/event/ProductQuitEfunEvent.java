package com.buy.plugin.event.product.event;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 商品退出幸运一折购事件.
 * 
 * @author Chengyb
 */
public class ProductQuitEfunEvent extends ApplicationEvent {

	private static final long serialVersionUID = 5664559108824456291L;

	public ProductQuitEfunEvent(Object source) {
		super(source);
    }

}