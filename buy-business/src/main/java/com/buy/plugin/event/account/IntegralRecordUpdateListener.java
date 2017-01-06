package com.buy.plugin.event.account;

import java.util.Date;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.common.Ret;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.message.Message;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;

/**
 * Listener - 用户积分对账单更新消息
 */
@Listener (enableAsync = true)
public class IntegralRecordUpdateListener implements ApplicationListener<IntegralRecordUpdateEvent> { 

	@Override
	public void onApplicationEvent(IntegralRecordUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		String userId = ret.get("userId");
		String userName = ret.get("userName");
		int intgral = Integer.parseInt(ret.get("intgral").toString());
		int type = Integer.parseInt(ret.get("type").toString());
		String remark = ret.get("remark");
		
		// 设置消息模板
		String templateCode = "";
		switch (type) {
		
			/* ===== ===== ===== ===== 积分添加 ===== ===== ===== ===== */
			case IntegralRecord.TYPE_CANCEL_ORDER:			// 取消订单
			case IntegralRecord.TYPE_GET_INTEGRAL:			// 获取积分
			case IntegralRecord.TYPE_SHOPPING_RETURN:		// 购物返积分
			case IntegralRecord.TYPE_CHANG_ADD:				// 调整积分增加
				templateCode = SmsAndMsgTemplate.INTEGRAL_ADD;
				break;
		
			/* ===== ===== ===== ===== 积分减少 ===== ===== ===== ===== */
			case IntegralRecord.TYPE_SHOPPING:				// 购物
			case IntegralRecord.TYPE_EFUN_ORDER:			// E趣购
			case IntegralRecord.TYPE_CHANG_SUB:				// 调整积分减少
				templateCode = SmsAndMsgTemplate.INTEGRAL_SUB;
				break;
		}
		
		// 设置当前时间
		Date now = new Date();
		String date = DateUtil.DateToString(now, DateStyle.YYYY_MM_DD_HH_MM);
		
		// 设置内容
		String[] datas = new String[]{userName, date, intgral + "", remark};
		String content = SmsAndMsgTemplate.dao.dealContent(templateCode, datas);
		
		// 发送消息
		Message.dao.add4Private(Message.TYPE_ACCOUNT_MESSAGE, null, Message.TITLE_INTEGRAL, content, templateCode, userId, User.FRONT_USER);	}

}