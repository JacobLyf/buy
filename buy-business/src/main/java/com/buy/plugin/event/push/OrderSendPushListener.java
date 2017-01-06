package com.buy.plugin.event.push;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.buy.model.push.Push;
import com.buy.model.push.PushUserMap;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.plugin.jpush.Jpush;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 订单发货推送
 */
@Listener (enableAsync = true)
public class OrderSendPushListener implements ApplicationListener<OrderSendPushEvent>
{
	Logger L = Logger.getLogger(OrderSendPushListener.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(OrderSendPushEvent event)
	{
		/*
		 * 查询订单ID集合
		 */
		List<String> orderList = (List<String>) event.getSource();
		if (StringUtil.isNull(orderList))
			return;
		if (orderList.size() == 0)
			return;
		
		/*
		 * 获取会员ID集合
		 */
		String orderIds = StringUtil.listToStringForSql(",", orderList);
		List<String> userIdList = Db.query("SELECT user_id FROM t_order WHERE id IN (" + orderIds +")");
		
		/*
		 * 获取推送注册ID集合 
		 */
		List<String> regIds = PushUserMap.dao.findRegIds(userIdList);
		if (StringUtil.isNull(regIds))
			return;
		if (regIds.size() == 0)
			return;
		
		for(String s : userIdList)
			L.info("(1)会员ID：" + s);
		
		/*
		 * 推送
		 */
		L.info("推送 - 订单发货通知");
		Push push = new Push();
		push.handleExtrasByApi(Push.JumpTo.ALL_ORDER, "");
		String content = SmsAndMsgTemplate.dao.getContentByType(SmsAndMsgTemplate.PUSH_ORDER_SEND);
		if (StringUtil.notNull(content))
		{
			push.set("content", content);
			push = Jpush.push4RegId(Jpush.PushPlatform.ALL, regIds, "订单发货通知", push);
			push.set("create_time", new Date()).save();
		}
	}
	
}
