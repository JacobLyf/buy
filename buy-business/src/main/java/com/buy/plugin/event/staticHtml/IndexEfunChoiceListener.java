package com.buy.plugin.event.staticHtml;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class IndexEfunChoiceListener implements ApplicationListener<IndexEfunChoiceEvent> {

	@Override
	public void onApplicationEvent(IndexEfunChoiceEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexEfunChoice", realPath+"html/index/efun_choice.html");
    	}
	}

}
