package com.buy.plugin.event.pos.order;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.service.pos.push.PushPosReturnGoods;
import com.buy.string.StringUtil;

@Listener (enableAsync = true)
public class PushPosOrderReturnListener implements ApplicationListener<PushPosOrderReturnEvent> {
	
	Logger L = Logger.getLogger(PushPosOrderReturnListener.class);

	@Override
	public void onApplicationEvent(PushPosOrderReturnEvent event) {
		
		PushPosReturnGoods source = (PushPosReturnGoods) event.getSource();
		if (StringUtil.isNull(source)) {
			L.info("推送POS--退货订单--数据源没有数据");
			return;
		}
		
		source.push();
	}
	
}
