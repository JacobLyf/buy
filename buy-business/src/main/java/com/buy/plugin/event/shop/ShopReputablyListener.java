package com.buy.plugin.event.shop;


import com.buy.service.shop.ModelShopService;
import com.jfinal.aop.Duang;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 店铺好评率评分
 */
@Listener(enableAsync = true)
public class ShopReputablyListener implements ApplicationListener<ShopReputablyEvent> {
	
	ModelShopService shopService = Duang.duang(ModelShopService.class);

	@Override
	public void onApplicationEvent(ShopReputablyEvent event) {
		
		// 店铺好评率评分
		String orderId = (String) event.getSource();
		shopService.updateShopReputably(orderId);
		
	}

}
