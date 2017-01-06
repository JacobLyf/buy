package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

@Listener(enableAsync = true)
public class IndexHeadAdListener implements ApplicationListener<IndexHeadAdEvent> {

	@Override
	public void onApplicationEvent(IndexHeadAdEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexHeadAd", realPath+"html/index/index_head_ad.html");
    	}
	}

}
