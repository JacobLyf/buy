package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationEvent;
/**
 * 首页Banner重新生成静态页面事件
 * @author jekay
 *
 */
public class IndexLuckyBuyEvent extends ApplicationEvent {
	private static final long serialVersionUID = -221736521364026284L;

	public IndexLuckyBuyEvent(Object source) {
		super(source);
	}

}
