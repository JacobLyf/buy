package com.buy.plugin.event.pos.inventory;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.service.pos.push.PushPosInventory;
import com.buy.string.StringUtil;

@Listener (enableAsync = true)
public class PushPosInventoryListener implements ApplicationListener<PushPosInventoryEvent> {
	
	Logger L = Logger.getLogger(PushPosInventoryListener.class);

	@Override
	public void onApplicationEvent(PushPosInventoryEvent event) {
		
		PushPosInventory source = (PushPosInventory) event.getSource();
		if (StringUtil.isNull(source)) {
			L.info("推送POS数据源没有数据");
			return;
		}
		
//		source.push();//等pos改好，再放开注释
	}
	
}
