package com.buy.plugin.event.shop;

import com.buy.plugin.event.shop.event.ShopAppAdUpdateEvent;
import com.buy.plugin.rabbitmq.aop.shop.ShopIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * APP广告店铺列表更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ShopAppAdBannerUpdateEventListener implements ApplicationListener<ShopAppAdUpdateEvent> {

	@Override
	public void onApplicationEvent(ShopAppAdUpdateEvent event) {
		ShopIndex.send((Integer) event.getSource(), ExchangeType.APP_AD_SHOP_UPDATE, 20);
	}
	
}