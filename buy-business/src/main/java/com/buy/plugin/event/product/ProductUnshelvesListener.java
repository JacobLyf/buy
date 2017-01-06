package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductUnshelvesEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品下架索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductUnshelvesListener implements ApplicationListener<ProductUnshelvesEvent> {

	@Override
	public void onApplicationEvent(ProductUnshelvesEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_UNSHELVES, 19);
	}
	
}