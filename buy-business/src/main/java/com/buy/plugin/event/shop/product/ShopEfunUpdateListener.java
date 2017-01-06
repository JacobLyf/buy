package com.buy.plugin.event.shop.product;

import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class ShopEfunUpdateListener implements ApplicationListener<ShopEfunUpdateEvent> {

	@Override
	public void onApplicationEvent(ShopEfunUpdateEvent event) {
		ShopO2oEfunUpdate source = (ShopO2oEfunUpdate) event.getSource();
		
		if (StringUtil.notNull(source))
			source.updateByEfun();
	}

}
