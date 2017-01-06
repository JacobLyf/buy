package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductDeleteEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品索引删除事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductDeleteListener implements ApplicationListener<ProductDeleteEvent> {

	@Override
	public void onApplicationEvent(ProductDeleteEvent event) {
		ProductIndex.send(Integer.parseInt(event.getSource().toString()), ExchangeType.DELETE, 20);
	}
	
}