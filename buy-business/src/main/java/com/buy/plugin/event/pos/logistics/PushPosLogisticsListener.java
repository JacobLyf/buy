package com.buy.plugin.event.pos.logistics;

import com.buy.service.pos.push.PushPosLogistics;
import com.buy.service.pos.push.PushPosProduct;
import com.buy.string.StringUtil;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;
import org.apache.log4j.Logger;

@Listener (enableAsync = true)
public class PushPosLogisticsListener implements ApplicationListener<PushPosLogisticsEvent> {
	
	Logger L = Logger.getLogger(PushPosLogisticsListener.class);

	@Override
	public void onApplicationEvent(PushPosLogisticsEvent event) {
		
		PushPosLogistics source = (PushPosLogistics) event.getSource();
		if (StringUtil.isNull(source)) {
			L.info("推送POS数据源没有数据");
			return;
		}
		
		source.push();
	}
	
}
