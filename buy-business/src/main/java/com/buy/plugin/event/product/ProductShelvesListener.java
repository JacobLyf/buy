package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductShelvesEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品上架索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductShelvesListener implements ApplicationListener<ProductShelvesEvent> {

	@Override
	public void onApplicationEvent(ProductShelvesEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_SHELVES, 19);
	}
	
}