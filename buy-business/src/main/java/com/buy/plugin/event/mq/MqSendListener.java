package com.buy.plugin.event.mq;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.buy.common.BaseConfig;
import com.buy.common.JsonMessage;
import com.buy.model.error.ErrorLog;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 
 * 发送Mq消息
 *
 */
@Listener(enableAsync = true)
public class MqSendListener implements ApplicationListener<MqSendEvent> {
	private Logger L = Logger.getLogger(MqSendEvent.class);
		
	@Override
	public void onApplicationEvent(MqSendEvent event) {	
			L.info("发送到MQ");
			//请求路径
			String mqUrl = BaseConfig.globalProperties.get("mq.url"); 
			try{
				Record message = (Record) event.getSource();
				String routingKey = message.getStr("routingKey");
				String content = message.getStr("content");
				mqUrl+="/send/$"+routingKey+"?content="+content;
				String result = HttpKit.get(mqUrl);
				JSONObject r = JSONObject.parseObject(result);
				if(!r.getString("status").equals("0")){
					//记录异常信息
					ErrorLog.dao.add(ErrorLog.TYPE_MQ, mqUrl, null, null);					
				}
		        
			}catch(Exception e){
				ErrorLog.dao.add(ErrorLog.TYPE_MQ, mqUrl, e.getMessage(), "mq访问异常");				
				e.printStackTrace();
				L.error(e.getMessage());
			} 
		
	}
	
}