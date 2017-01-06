package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductEfunPriceUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品幸运一折购价格更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductEfunPriceUpdateListener implements ApplicationListener<ProductEfunPriceUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductEfunPriceUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_EFUN_PRICE_UPDATE, 18);
	}
	
}