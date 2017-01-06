package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductJoinEfunEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品加入幸运一折购事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductJoinEfunListener implements ApplicationListener<ProductJoinEfunEvent> {

	@Override
	public void onApplicationEvent(ProductJoinEfunEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_JOIN_EFUN, 19);
	}
	
}