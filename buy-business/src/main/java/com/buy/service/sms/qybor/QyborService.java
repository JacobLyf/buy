package com.buy.service.sms.qybor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsBatchRecord;
import com.buy.plugin.sms.SmsQybor;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

public class QyborService
{
	@Before(Tx.class)
	public void updateBySmsFail(SMS sms, String errorMsg)
	{
		sms
			.set("status", SMS.STATUS_FAIL)
			.set("remark", "后台发送短信，非法提交信息：" + errorMsg)
			.update();
	}
	
	@Before(Tx.class)
	public void updateBySms(SMS sms, String templateCode, List<String> phones)
	{
		int maxSend = SmsQybor.SMS_ONE_COUNT;
		int size = phones.size();
		Date now = new Date();
		
		/*
		 * 少于等于限制次数
		 */
		if (size <= maxSend)
		{
			SmsQybor smsQybor = new SmsQybor(phones, sms.getStr("content"), true, "", now);
			updateByQybor(sms, templateCode, smsQybor.sms());
			return;
		}
		
		/*
		 * 大于制次数
		 */
		List<String> sends = new ArrayList<String>();
		for (String p : phones)
		{
			if (size > maxSend)
			{
				sends.add(p);
				if (sends.size() == maxSend)
				{
					SmsQybor smsQybor = new SmsQybor(phones, sms.getStr("content"), true, "", now);
					updateByQybor(sms, templateCode, smsQybor.sms());
					sends = new ArrayList<String>();
					size -= maxSend;
				}
				else
				{
					continue;
				}
			}
			
			else
			{
				SmsQybor smsQybor = new SmsQybor(phones, sms.getStr("content"), true, "", now);
				updateByQybor(sms, templateCode, smsQybor.sms());
				return;
			}
		}
	}
	
	void updateByQybor(SMS sms, String templateCode, SmsQybor.Report report)
	{
		/*
		 * 设置参数
		 */
		Integer smsId = sms.getNumber("id").intValue();
		Integer status = sms.getInt("status");
		
		String batchno = report.getBatchno();
		String respcode = report.getRespcode();
		String respdesc = report.getRespdesc();
		
		/*
		 * 成功
		 */
		if ("0".equals(respcode))
		{
			if (SMS.STATUS_SENDING == status || SMS.STATUS_FAIL == status)
			{
				sms
					.set("status", 			SMS.STATUS_SUCCESS)
					.set("status_code", 	respcode)
					.set("template_code",	templateCode)
					.set("remark", 			"后台发送短信")
					.update();
			}
			status = SMS.STATUS_SUCCESS;
		}
		
		/*
		 * 失败
		 */
		else
		{
			if (SMS.STATUS_SENDING == status)
			{
				sms
					.set("status", 			SMS.STATUS_FAIL)
					.set("status_code", 	respcode)
					.set("template_code",	templateCode)
					.set("remark", 			"后台发送短信")
					.update();
			}
			status = SMS.STATUS_FAIL;
		}
	
		new SmsBatchRecord()
			.set("sms_id",			smsId)
			.set("batch_number",	batchno)
			.set("status", 			status)
			.set("remark",			respdesc)
			.save();
	}
	
}
