package com.buy.plugin.event.activity;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.model.account.Account;
import com.buy.model.efun.EfunDrawRecord;
import com.buy.model.efun.EfunUserOrder;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

@Listener(enableAsync = true)
public class OlympicPrizeListener implements ApplicationListener<OlympicPrizeEvent>{

	private  Logger L = Logger.getLogger(OlympicPrizeListener.class);
	
	@Override
	@Before(Tx.class)
	public void onApplicationEvent(OlympicPrizeEvent event) {
		L.info("开始奥运会活动添加奖品记录");
		
		L.info("结束奥运会活动添加奖品记录");
	}
}
