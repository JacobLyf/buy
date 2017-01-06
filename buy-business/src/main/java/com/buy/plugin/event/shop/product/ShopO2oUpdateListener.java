package com.buy.plugin.event.shop.product;

import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class ShopO2oUpdateListener implements ApplicationListener<ShopO2oUpdateEvent> {

	@Override
	public void onApplicationEvent(ShopO2oUpdateEvent event) {
		ShopO2oEfunUpdate source = (ShopO2oEfunUpdate) event.getSource();
		
		if (StringUtil.notNull(source))
			source.updateByO2o();
	}

}
