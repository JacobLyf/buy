package com.buy.plugin.event.shop.product;

import com.buy.model.shop.Shop;
import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationEvent;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class ShopProO2oEfunListener implements ApplicationListener<ApplicationEvent> {

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		String shopId = (String) event.getSource();
		
		if (StringUtil.notNull(shopId))
			Shop.dao.updateByO2oEfun(shopId);
	}

}
