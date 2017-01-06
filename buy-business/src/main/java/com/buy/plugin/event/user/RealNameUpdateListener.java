package com.buy.plugin.event.user;

import java.util.Date;

import com.buy.common.Ret;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.message.Message;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 会员实名认证消息
 */
@Listener (enableAsync = true)
public class RealNameUpdateListener implements ApplicationListener<RealNameUpdateEvent> { 

	@Override
	public void onApplicationEvent(RealNameUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		int realNameId 		= ret.get("realNameId");
		String templateCode	= ret.get("templateCode");
		String remark		= ret.get("remark");
		
		// 查询会员ID、会员姓名、申请时间
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	a.user_id userId,");
		sql.append(" 	b.user_name userName,");
		sql.append(" 	a.create_time applyTime");
		sql.append(" FROM t_user_realname a");
		sql.append(" LEFT JOIN t_user b ON b.id = a.user_id");
		sql.append(" WHERE a.id = ?");
		// 查询结果
		Record record = Db.findFirst(sql.toString(), User.FRONT_USER, realNameId);
		
		// 设置申请时间、消息内容参数、消息内容
		Date applyTime =  record.getDate("applyTime");
		String date = DateUtil.DateToString(applyTime, DateStyle.YYYY_MM_DD_HH_MM);
		String userId = record.getStr("userId");
		String userName = record.getStr("userName");
		
		// 消息内容
		String[] datas = null;
		String content = "";
		// 审核通过
		if(SmsAndMsgTemplate.REAL_NAME_PASS.equals(templateCode)) {
			datas =  new String[]{userName, date};
			content = SmsAndMsgTemplate.dao.dealContent(SmsAndMsgTemplate.REAL_NAME_PASS, datas);
		// 审核不通过
		} else {
			datas = new String[]{userName, date, remark};
			content = SmsAndMsgTemplate.dao.dealContent(SmsAndMsgTemplate.REAL_NAME_UNPASS, datas);
		}
		
		// 发短信
		Message.dao.add4Private(Message.TYPE_SYSTEM_MESSAGE, null, Message.TITLE_REALNAME, content, templateCode, userId, User.FRONT_USER);
	}

}