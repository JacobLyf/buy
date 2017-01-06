package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationEvent;
/**
 * 首页Banner重新生成静态页面事件
 * @author jekay
 *
 */
public class IndexBannerEvent extends ApplicationEvent {
	private static final long serialVersionUID = -6128147674637124333L;

	public IndexBannerEvent(Object source) {
		super(source);
	}

}
