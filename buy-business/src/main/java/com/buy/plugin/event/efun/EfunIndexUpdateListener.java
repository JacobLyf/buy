package com.buy.plugin.event.efun;

import com.buy.plugin.rabbitmq.aop.efun.EfunIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 幸运一折购索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class EfunIndexUpdateListener implements ApplicationListener<EfunUpdateEvent> {

	@Override
	public void onApplicationEvent(EfunUpdateEvent event) {
		EfunIndex.send((String) event.getSource(), ExchangeType.UPDATE);
	}
	
}