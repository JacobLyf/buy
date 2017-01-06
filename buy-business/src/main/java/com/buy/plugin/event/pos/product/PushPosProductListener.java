package com.buy.plugin.event.pos.product;

import org.apache.log4j.Logger;

import com.buy.service.pos.push.PushPosProduct;
import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class PushPosProductListener implements ApplicationListener<PushPosProductEvent> {
	
	Logger L = Logger.getLogger(PushPosProductListener.class);

	@Override
	public void onApplicationEvent(PushPosProductEvent event) {
		
		PushPosProduct source = (PushPosProduct) event.getSource();
		if (StringUtil.isNull(source)) {
			L.info("推送POS数据源没有数据");
			return;
		}
		
		source.push();
	}
	
}
