package com.buy.service.user;

import java.util.Date;

import com.buy.model.SysParam;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.user.UserSign;
import com.buy.model.user.UserSignRewardRecord;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

public class UserSignRewardRecordService
{
	
	@Before(Tx.class)
	public void addReward(String userId)
	{
		UserSign sign = UserSign.dao.getSignInfo(userId);
		if (StringUtil.isNull(sign))
			return;
		
		Date now = new Date();
		int integralVal = SysParam.dao.getIntByCode("sign_reward");
		int signId = sign.getInt("id");
		new UserSignRewardRecord()
			.set("user_id",		userId)
			.set("sign_id",		signId)
			.set("type",		UserSignRewardRecord.TYPE_SIGN)
			.set("status",		UserSignRewardRecord.STATUS_HAS_GET)
			.set("remark",		integralVal + "积分")
			.set("create_time",	now)
			.save();
		
		// 连续签到10次奖励
		int combo = sign.getInt("combo");
		if (10 == combo)
		{
			new Integral().save(userId, 50, "连续签到10天奖励",  IntegralRecord.TYPE_GET_INTEGRAL);
			
			new UserSignRewardRecord()
			.set("user_id",		userId)
			.set("sign_id",		signId)
			.set("type",		UserSignRewardRecord.TYPE_10DAY)
			.set("status",		UserSignRewardRecord.STATUS_HAS_GET)
			.set("remark",		"50积分")
			.set("create_time",	now)
			.save();
		}
	}
	
}
