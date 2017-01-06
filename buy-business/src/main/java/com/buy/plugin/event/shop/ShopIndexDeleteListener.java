package com.buy.plugin.event.shop;

import com.buy.plugin.rabbitmq.aop.shop.ShopIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 店铺索引删除事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ShopIndexDeleteListener implements ApplicationListener<ShopDeleteEvent> {

	@Override
	public void onApplicationEvent(ShopDeleteEvent event) {
		ShopIndex.send((String) event.getSource(), ExchangeType.DELETE);
	}
	
}