package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductScoreUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品评分更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductScoreUpdateListener implements ApplicationListener<ProductScoreUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductScoreUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_SCORE_UPDATE, 16);
	}
	
}