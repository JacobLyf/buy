package com.buy.plugin.event.account;


import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

//注解标记，切勿忘记 
@Listener (enableAsync = true)
public class AccountUpdateListener implements ApplicationListener<AccountUpdateEvent> { 


	@Override
	public void onApplicationEvent(AccountUpdateEvent accountUpdateEvent) {
		
	
		//发短信
		
	
		
	}

}