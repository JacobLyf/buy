package com.buy.plugin.event.staticHtml;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class AppEfunAdListener implements ApplicationListener<AppEfunAdEvent> {

	@Override
	public void onApplicationEvent(AppEfunAdEvent event) {
		//App
		String domain = PropKit.use("global.properties").get("app.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.app.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/efun/initEfunAd", realPath+"html/eql_ad.html");
    	}
    	
    	//Wap
    	String domainWap = PropKit.use("global.properties").get("wap.domain");
    	String realWapPathStr =  PropKit.use("global.properties").get("html.wap.path");
    	String[] realWapPaths = realWapPathStr.split(",");
    	for(String wapPath : realWapPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domainWap+"/efun/initEfunAd", wapPath+"html/eql_ad.html");
    	}
	}

}
