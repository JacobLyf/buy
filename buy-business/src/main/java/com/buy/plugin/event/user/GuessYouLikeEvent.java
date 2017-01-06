package com.buy.plugin.event.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 猜你喜欢驱动事件
 * @author chenhg
 *
 */
public class GuessYouLikeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public GuessYouLikeEvent(Object source) {
		super(source);
	}

}
