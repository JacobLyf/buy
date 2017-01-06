package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductEfunScoreUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品幸运一折购评分更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductEfunScoreUpdateListener implements ApplicationListener<ProductEfunScoreUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductEfunScoreUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_EFUN_SCORE_UPDATE, 17);
	}
	
}