package com.buy.plugin.event.shop;

import com.buy.plugin.rabbitmq.aop.shop.ShopIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 店铺索引评分更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ShopScoreUpdateListener implements ApplicationListener<ShopScoreUpdateEvent> {

	@Override
	public void onApplicationEvent(ShopScoreUpdateEvent event) {
		ShopIndex.send((String) event.getSource(), ExchangeType.SHOP_SCORE_UPDATE);
	}
	
}