package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductBrandUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品品牌索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductBrandUpdateListener implements ApplicationListener<ProductBrandUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductBrandUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_BRAND_UPDATE, 17);
	}
	
}