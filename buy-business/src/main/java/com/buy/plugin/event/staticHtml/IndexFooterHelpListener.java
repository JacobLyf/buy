package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;
/**
 * 页脚帮助
 * @author jekay
 *
 */
@Listener(enableAsync = true)
public class IndexFooterHelpListener implements ApplicationListener<IndexFooterHelpEvent> {

	@Override
	public void onApplicationEvent(IndexFooterHelpEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
		String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/initBase/indexFooterHelp", realPath+"html/index/footer.html");
    	}
	}

}
