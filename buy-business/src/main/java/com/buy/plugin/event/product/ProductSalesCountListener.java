package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductSalesCountEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品销量更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductSalesCountListener implements ApplicationListener<ProductSalesCountEvent> {

	@Override
	public void onApplicationEvent(ProductSalesCountEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_SALES_COUNT_UPDATE, 16);
	}
	
}