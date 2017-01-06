package com.buy.plugin.event.account;


import java.math.BigDecimal;
import java.util.Date;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.common.Ret;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.message.Message;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;

/**
 * Listener - 店铺现金对账单更新消息
 */
@Listener (enableAsync = true)
public class ShopCashRecordUpdateListener implements ApplicationListener<ShopCashRecordUpdateEvent> { 

	@Override
	public void onApplicationEvent(ShopCashRecordUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		String shopId = ret.get("shopId");
		String shopNo = ret.get("shopNo");
		BigDecimal cash = (BigDecimal)ret.get("cash");
		int type = ret.get("type");
		String remark = ret.get("remark");
		
		// 设置消息模板
		String templateCode = "";
		switch (type) {
		
			/* ===== ===== ===== ===== 现金添加 ===== ===== ===== ===== */
			case ShopCashRecord.TYPE_RECHARGE:				// 充值
			case ShopCashRecord.TYPE_BALANCE:				// 订单结算
			case ShopCashRecord.TYPE_REBATE_PUBLIC_ORDER:	// 公共订单返利
			case ShopCashRecord.TYPE_REBATE_SELF_ORDER:		// 本店铺订单返利
			case ShopCashRecord.TYPE_REBATE_OTHER_ORDER:	// 其它店铺订单返利
			case ShopCashRecord.TYPE_REBATE_SHOP_EFUN:		// 店铺幸运一折购返利
			case ShopCashRecord.TYPE_EFUN_BALANCE:			// 幸运一折购订单结算
			case ShopCashRecord.TYPE_DEPOSIT_BACK:			// 保证金退回
			case ShopCashRecord.TYPE_PEISHI_BACK:			// 假一赔十退回
			case ShopCashRecord.TYPE_CHANG_ADD:				// 现金调整 - 增加
				templateCode = SmsAndMsgTemplate.CASH_ADD;
				break;
				
			/* ===== ===== ===== ===== 现金减少 ===== ===== ===== ===== */
			case ShopCashRecord.TYPE_WITHDARWAL:			// 提现
			case ShopCashRecord.TYPE_REFUND:				// 退款
			case ShopCashRecord.TYPE_TRANSFER:				// 转账
			case ShopCashRecord.TYPE_DEPOSIT:				// 保证金缴纳
			case ShopCashRecord.TYPE_PEISHI:				// 假一赔十
			case ShopCashRecord.TYPE_RENEW:					// 续费
			case ShopCashRecord.TYPE_CHANG_SUB:				// 现金调整 - 减少
				templateCode = SmsAndMsgTemplate.CASH_SUB;
				break;
		}
		
		// 设置当前时间
		Date now = new Date();
		String date = DateUtil.DateToString(now, DateStyle.YYYY_MM_DD_HH_MM);
		
		// 设置消息内容
		String[] datas = new String[]{shopNo, date, cash.toString(), remark};
		String content = SmsAndMsgTemplate.dao.dealContent(templateCode, datas);
		
		// 发消息
		Message.dao.add4Private(Message.TYPE_ACCOUNT_MESSAGE, null, Message.TITLE_CASH, content, templateCode, shopId, User.FRONT_USER_SHOP);
	}

}