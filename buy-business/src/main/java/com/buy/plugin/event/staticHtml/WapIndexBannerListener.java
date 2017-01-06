package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

@Listener(enableAsync = true)
public class WapIndexBannerListener implements ApplicationListener<WapIndexBannerEvent> {
	private static final Logger L = Logger.getLogger(WapIndexBannerListener.class);
	@Override
	public void onApplicationEvent(WapIndexBannerEvent event) {
		String domain = PropKit.use("global.properties").get("wap.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.wap.path");
		L.info("路径："+realPathStr);
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexBanner", realPath+"html/index_banner.html");
    	}
	}
}
