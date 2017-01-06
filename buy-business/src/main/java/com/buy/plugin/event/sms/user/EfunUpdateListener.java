package com.buy.plugin.event.sms.user;

import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 幸运一折购中奖
 */
@Listener (enableAsync = true)
public class EfunUpdateListener implements ApplicationListener<EfunUpdateEvent> {

	@Override
	public void onApplicationEvent(EfunUpdateEvent event) {
		
		// 查询中奖用户信息
		Ret source = (Ret) event.getSource();
		int efunId = (Integer)source.get("efunId");
		int winNumber = (Integer)source.get("winNumber");
		List<Record> efunList = EfunUserOrder.dao.getUserInfo4Efun(efunId, winNumber);
		
		// 为中奖人发送短信
		for (Record efunInfo : efunList) {
			// 设置短信内容参数
			String userName = efunInfo.getStr("userName");
			Date createTime = efunInfo.getDate("createTime");
			String date = DateUtil.DateToString(createTime, DateStyle.MM_DD_HH_MM_CN);
			String proName = efunInfo.getStr("proName");
			String[] datas = new String[]{userName, date, proName};
			
			// 发短信
			String mobile = efunInfo.getStr("mobile");
			SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_FUN_DARW_WIN, datas, mobile, "", "幸运一折购中奖通知", BaseConstants.DataFrom.PC);
		}
	} 

}
