package com.buy.plugin.event.push;

import java.util.Date;

import com.buy.model.push.Push;
import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 手动推送
 */
@Listener (enableAsync = true)
public class HandlePushListener implements ApplicationListener<HandlePushEvent>
{

	@Override
	public void onApplicationEvent(HandlePushEvent event)
	{
		Push push = (Push) event.getSource();
		push = push.pushHand();
		if (push!=null)
			push.set("create_time", new Date()).save();
	}

}
