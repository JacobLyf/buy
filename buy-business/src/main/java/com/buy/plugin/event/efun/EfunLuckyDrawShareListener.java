package com.buy.plugin.event.efun;

import java.util.Map;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.model.sms.SMS;

@Listener (enableAsync = true)
public class EfunLuckyDrawShareListener implements ApplicationListener<EfunLuckyDrawShareEvent>
{
	 Logger L = Logger.getLogger(EfunLuckyDrawShareListener.class);
	 
	 String[] datas = new String[2];
	 String mobile = null;
	
	@Override
	public void onApplicationEvent(EfunLuckyDrawShareEvent e)
	{
		L.info("一折购抽奖活动--分享验证登录");
		@SuppressWarnings("unchecked")
		Map<String,String> map = (Map<String, String>) e.getSource();
		mobile = map.get("mobile");
		datas = new String[]{ map.get("userName"), map.get("password")};
		new Thread(){
            public void run(){
               try {
            	Thread.sleep(60*1000);//同一个号码延迟一分钟发送短信
            	SMS.dao.sendSMS("register_sucess", datas, mobile, "", "提醒一折购抽奖活动分享验证登录", BaseConstants.DataFrom.PC);
               } catch (InterruptedException e) { }
            }
         }.start();  
	}

}
