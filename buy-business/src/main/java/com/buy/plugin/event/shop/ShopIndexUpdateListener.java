package com.buy.plugin.event.shop;

import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.aop.shop.ShopIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 店铺索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ShopIndexUpdateListener implements ApplicationListener<ShopUpdateEvent> {

	@Override
	public void onApplicationEvent(ShopUpdateEvent event) {
		ShopIndex.send((String) event.getSource(), ExchangeType.UPDATE);
		
		ProductIndex.send((String) event.getSource(), ExchangeType.SHOP_UPDATE, 20);
	}
	
}