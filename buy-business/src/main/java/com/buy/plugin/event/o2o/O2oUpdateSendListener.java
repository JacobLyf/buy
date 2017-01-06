package com.buy.plugin.event.o2o;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.plugin.rabbitmq.aop.o2o.O2oUpdate;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;

/**
 * 
 * O2O商品更新发送
 *
 */
@Listener(enableAsync = true)
public class O2oUpdateSendListener implements ApplicationListener<O2oUpdateSendEvent> {

	@Override
	public void onApplicationEvent(O2oUpdateSendEvent event) {
		O2oUpdate.send((Integer) event.getSource(), ExchangeType.PC_O2O_UPDATE);
	}
	
}