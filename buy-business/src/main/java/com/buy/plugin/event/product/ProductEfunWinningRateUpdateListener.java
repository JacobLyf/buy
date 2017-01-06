package com.buy.plugin.event.product;

import com.buy.plugin.event.product.event.ProductEfunWinningRateUpdateEvent;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品幸运一折购历史开奖率更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductEfunWinningRateUpdateListener implements ApplicationListener<ProductEfunWinningRateUpdateEvent> {

	@Override
	public void onApplicationEvent(ProductEfunWinningRateUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.PRODUCT_EFUN_WINNING_RATE_UPDATE, 17);
	}
	
}