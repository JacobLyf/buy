package com.buy.plugin.event.supplier;

import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 供应商更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class SupplierIndexUpdateListener implements ApplicationListener<SupplierUpdateEvent> {

	@Override
	public void onApplicationEvent(SupplierUpdateEvent event) {
		ProductIndex.send((String) event.getSource(), ExchangeType.SUPPLIER_UPDATE, 20);
	}
	
}