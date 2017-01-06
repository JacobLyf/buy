package com.buy.plugin.event.account;

import java.math.BigDecimal;
import java.util.Date;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.common.Ret;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.message.Message;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRecord;

/**
 * Listener - 用户现金对账单更新消息
 */
@Listener (enableAsync = true)
public class UserCashRecordUpdateListener implements ApplicationListener<UserCashRecordUpdateEvent> { 

	@Override
	public void onApplicationEvent(UserCashRecordUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		String userId = ret.get("userId");
		String userName = ret.get("userName");
		BigDecimal cash = ret.get("cash");
		int type = Integer.parseInt(ret.get("type").toString());
		String remark = ret.get("remark");
		
		// 设置发送模板
		String templateCode = "";
		switch (type) {
			/* ===== ===== ===== ===== 现金添加 ===== ===== ===== ===== */
			case UserCashRecord.TYPE_RECHARGE:			// 充值
			case UserCashRecord.TYPE_REFUND:			// 退款
			case UserCashRecord.TYPE_CANCEL_ORDER:		// 取消订单
			case UserCashRecord.TYPE_TRANSFER:			// 转账
			case UserCashRecord.TYPE_CHANG_ADD:			// 金额调整 - 增加
			case UserCashRecord.TYPE_ACTIVITY_SNATCH_MANEY: //抢红包活动
			case UserCashRecord.TYPE_SIGN: 				// 签到
				templateCode = SmsAndMsgTemplate.CASH_ADD;
				break;
		
			/* ===== ===== ===== ===== 现金减少===== ===== ===== ===== */
			case UserCashRecord.TYPE_SHOPPING:			// 购物
			case UserCashRecord.TYPE_WITHDRAW_CASH:		// 提现
			case UserCashRecord.TYPE_EUN_ORDER:			// E趣购
			case UserCashRecord.TYPE_CHANG_SUB:			// 金额调整 - 减少
			case UserCashRecord.TYPE_EFUN_FREIGHT:		// 支付幸运一折购运费
				templateCode = SmsAndMsgTemplate.CASH_SUB;
				break;
		}
		
		// 设置当前日期
		Date now = new Date();
		String date = DateUtil.DateToString(now, DateStyle.YYYY_MM_DD_HH_MM);
		
		// 消息内容
		String[] datas = new String[]{userName, date, cash.toString(), remark};
		String content = SmsAndMsgTemplate.dao.dealContent(templateCode, datas);

		// 发送消息
		Message.dao.add4Private(Message.TYPE_ACCOUNT_MESSAGE, null, Message.TITLE_CASH, content, templateCode, userId, User.FRONT_USER);
	}

}