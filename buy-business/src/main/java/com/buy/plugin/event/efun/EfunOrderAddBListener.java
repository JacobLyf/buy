package com.buy.plugin.event.efun;

import java.util.Map;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.model.efun.EfunUserOrder;

@Listener (enableAsync = true)
public class EfunOrderAddBListener implements ApplicationListener<EfunOrderAddBEvent>{
	
	Logger L = Logger.getLogger(EfunOrderAddBListener.class);
	 
	@Override
	public void onApplicationEvent(EfunOrderAddBEvent e){
		L.info("会员参与幸运一折购成功事件驱动--根据算法生成假数据");
		@SuppressWarnings("unchecked")
		Map<String,Object> map = (Map<String, Object>) e.getSource();
		String skuCode = map.get("skuCode").toString();
		int efunId = Integer.valueOf(map.get("efunId").toString());
		//增加假数据
   	 	EfunUserOrder.dao.addB(skuCode, efunId);
	}

}
