package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductCreateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品索引创建事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductCreateListener implements ApplicationListener<ProductCreateEvent> {

	@Override
	public void onApplicationEvent(ProductCreateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.CREATE, 20);
	}
	
}