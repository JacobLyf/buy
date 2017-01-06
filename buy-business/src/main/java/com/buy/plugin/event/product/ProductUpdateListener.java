package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductUpdateListener implements ApplicationListener<ProductUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.UPDATE, 18);
	}
	
}