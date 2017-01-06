package com.buy.plugin.event.mq;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * 发送Mq消息
 * @author huangzq
 *
 */
public class MqSendEvent extends ApplicationEvent {





	private static final long serialVersionUID = -6460553498402664859L;

	public MqSendEvent(Object source) {
		super(source);
    }

}