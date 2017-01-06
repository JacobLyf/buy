package com.buy.plugin.event.user;

import com.buy.common.Ret;
import com.buy.model.message.Message;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 会员订单消息
 */
@Listener (enableAsync = true)
public class OrderUpdateListener implements ApplicationListener<OrderUpdateEvent> { 
	
	@Override
	public void onApplicationEvent(OrderUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		int returnId 		= (Integer)ret.get("returnId");
		String title		= ret.get("title").toString();
		String templateCode = ret.get("templateCode").toString();
		
		// 查询订单编号、用户ID、用户名
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	o.no orderNo, r.user_id userId, u.user_name userName");
		sql.append(" FROM t_order_return r");
		sql.append(" LEFT JOIN t_order o ON o.id = r.order_id");
		sql.append(" LEFT JOIN t_user u ON u.id = r.user_id");
		sql.append(" WHERE r.id = ?");
		// 查询结果
		Record record = Db.findFirst(sql.toString(), returnId);
		
		// 设置当前时间、消息内容参数、消息内容
		String orderNo = record.getStr("orderNo");
		String userId = record.getStr("userId");
		String userName = record.getStr("userName");
		
		// 发短信
		String[] datas = new String[]{userName, orderNo};
		String content = SmsAndMsgTemplate.dao.dealContent(templateCode, datas);
		Message.dao.add4Private(Message.TYPE_ORDER_MESSAGE, returnId+"", title, content, templateCode, userId, User.FRONT_USER);
	}

}