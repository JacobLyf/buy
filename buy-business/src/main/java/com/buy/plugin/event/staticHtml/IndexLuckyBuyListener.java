package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;

/**
 * banner
 * @author jekay
 *
 */
@Listener(enableAsync = true)
public class IndexLuckyBuyListener implements ApplicationListener<IndexLuckyBuyEvent> {

	@Override
	public void onApplicationEvent(IndexLuckyBuyEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexLuckyBuy", realPath+"html/index/lucky_buy_ad.html");
    	}
	}

}
