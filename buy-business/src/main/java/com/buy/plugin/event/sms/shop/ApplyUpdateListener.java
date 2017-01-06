package com.buy.plugin.event.sms.shop;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 开店审核
 */
@Listener (enableAsync = true)
public class ApplyUpdateListener implements ApplicationListener<ApplyUpdateEvent> { 

	@Override
	public void onApplicationEvent(ApplyUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		String smsCode = ret.get("smsCode");
		String[] datas = ret.get("datas");
		String mobile = ret.get("mobile");
		String checkCode = ret.get("checkCode");
		String remark = ret.get("remark");
		
		try {
			// 开店申请支付完成通知 - 延迟发送
			if (SmsAndMsgTemplate.SMS_SHOP_APPLY_PAY_FINISH.equals(smsCode))
				Thread.sleep(62 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// 发送短信
		SMS.dao.sendSMS(smsCode, datas, mobile, checkCode, remark, BaseConstants.DataFrom.PC);
		
	}

}