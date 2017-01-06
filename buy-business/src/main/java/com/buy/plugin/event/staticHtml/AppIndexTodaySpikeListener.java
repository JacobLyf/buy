package com.buy.plugin.event.staticHtml;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class AppIndexTodaySpikeListener implements ApplicationListener<AppIndexTodaySpikeEvent> {

	@Override
	public void onApplicationEvent(AppIndexTodaySpikeEvent event) {
		String domain = PropKit.use("global.properties").get("app.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.app.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexTodaySpike", realPath+"html/index/index_todaySpike.html");
    	}
	}
	
}
