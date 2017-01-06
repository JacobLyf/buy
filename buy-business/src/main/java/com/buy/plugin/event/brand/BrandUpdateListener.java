package com.buy.plugin.event.brand;

import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 品牌索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class BrandUpdateListener implements ApplicationListener<BrandUpdateEvent> {

	@Override
	public void onApplicationEvent(BrandUpdateEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.BRAND_UPDATE, 17);
	}
	
}