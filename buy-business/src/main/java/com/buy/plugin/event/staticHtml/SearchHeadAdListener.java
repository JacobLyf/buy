package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

@Listener(enableAsync = true)
public class SearchHeadAdListener implements ApplicationListener<SearchHeadAdEvent> {

	@Override
	public void onApplicationEvent(SearchHeadAdEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/searchHeadAd", realPath+"html/index/search_head_ad.html");
    	}
	}

}
