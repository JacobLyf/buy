package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

/**
 * 首页楼层广告页面生成
 * @author chenhg
 */
@Listener(enableAsync = true)
public class IndexFloorListener implements ApplicationListener<IndexFloorEvent> {

	@Override
	public void onApplicationEvent(IndexFloorEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexFloor", realPath+"html/index/floor.html");
    	}
	}

}
