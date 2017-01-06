package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductAppAdUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * APP广告商品列表更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductAppAdUpdateEventListener implements ApplicationListener<ProductAppAdUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductAppAdUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.APP_AD_PRODUCT_UPDATE, 20);
	}
	
}