package com.buy.plugin.event.user.sign;

import com.buy.service.user.UserSignRewardRecordService;
import com.buy.string.StringUtil;
import com.jfinal.aop.Duang;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class UserSignListener implements ApplicationListener<UserSignEvent>
{
	UserSignRewardRecordService service = Duang.duang(UserSignRewardRecordService.class);
	

	@Override
	public void onApplicationEvent(UserSignEvent event)
	{
		String userId = (String) event.getSource();
		if (StringUtil.isNull(userId))
			return;
		
		service.addReward(userId);
	}

}
