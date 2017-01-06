package com.buy.plugin.event.staticHtml;

import net.dreamlu.event.core.ApplicationEvent;
/**
 * 首页分类重新生成静态页面事件
 * @author jekay
 *
 */
public class SortEvent extends ApplicationEvent {

	private static final long serialVersionUID = -4477657985054665444L;

	public SortEvent(Object source) {
		super(source);
	}

}
