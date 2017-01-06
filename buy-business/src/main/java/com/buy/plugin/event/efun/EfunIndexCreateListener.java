package com.buy.plugin.event.efun;

import com.buy.plugin.rabbitmq.aop.efun.EfunIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 幸运一折购索引创建事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class EfunIndexCreateListener implements ApplicationListener<EfunCreateEvent> {

	@Override
	public void onApplicationEvent(EfunCreateEvent event) {
		EfunIndex.send((String) event.getSource(), ExchangeType.CREATE);
	}
	
}