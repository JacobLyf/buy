package com.buy.plugin.event.brand;

import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 品牌索引删除事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class BrandDeleteListener implements ApplicationListener<BrandDeleteEvent> {

	@Override
	public void onApplicationEvent(BrandDeleteEvent event) {
		ProductIndex.send((Integer) event.getSource(), ExchangeType.BRAND_UPDATE, 17);
	}
	
}