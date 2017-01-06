package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductQuitEfunEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品退出幸运一折购事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductQuitEfunListener implements ApplicationListener<ProductQuitEfunEvent> {

	@Override
	public void onApplicationEvent(ProductQuitEfunEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_QUIT_EFUN, 19);
	}
	
}