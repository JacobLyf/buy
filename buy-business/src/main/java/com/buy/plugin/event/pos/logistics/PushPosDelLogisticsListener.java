package com.buy.plugin.event.pos.logistics;

import org.apache.log4j.Logger;

import com.buy.service.pos.push.PushPosDelLogistics;
import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class PushPosDelLogisticsListener implements ApplicationListener<PushPosDelLogisticsEvent> {
	
	Logger L = Logger.getLogger(PushPosDelLogisticsListener.class);

	@Override
	public void onApplicationEvent(PushPosDelLogisticsEvent event) {
		
		PushPosDelLogistics source = (PushPosDelLogistics) event.getSource();
		if (StringUtil.isNull(source)) {
			L.info("推送POS数据源没有数据");
			return;
		}
		
		source.push();
	}
	
}
