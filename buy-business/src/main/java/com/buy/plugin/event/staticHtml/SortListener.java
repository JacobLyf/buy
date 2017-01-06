package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.html.Htmlbuilder;
import com.jfinal.kit.PropKit;
/**
 * 首页分类重新生成静态页面
 * @author jekay
 *
 */
@Listener(enableAsync = true)
public class SortListener implements ApplicationListener<SortEvent>{

	@Override
	public void onApplicationEvent(SortEvent event) {
		String domain = PropKit.use("global.properties").get("web.domain");
    	String realPathStr =  PropKit.use("global.properties").get("html.path");
    	String[] realPaths = realPathStr.split(",");
    	for(String realPath : realPaths){
    		Htmlbuilder hb = new Htmlbuilder("webapp");
    		hb.createHtmlPage(domain+"/index/sort", realPath+"html/index/sort.html");
    	}
	}

}
