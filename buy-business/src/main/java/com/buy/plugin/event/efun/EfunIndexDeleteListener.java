package com.buy.plugin.event.efun;

import com.buy.plugin.rabbitmq.aop.efun.EfunIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 幸运一折购索引删除事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class EfunIndexDeleteListener implements ApplicationListener<EfunDeleteEvent> {

	@Override
	public void onApplicationEvent(EfunDeleteEvent event) {
		EfunIndex.send((String) event.getSource(), ExchangeType.DELETE);
	}
	
}