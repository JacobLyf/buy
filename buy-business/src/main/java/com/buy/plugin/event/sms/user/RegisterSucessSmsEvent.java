package com.buy.plugin.event.sms.user;

import net.dreamlu.event.core.ApplicationEvent;

/**
 * Event - 注册成功后通知
 */
public class RegisterSucessSmsEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    public RegisterSucessSmsEvent(Object source)
    {
        super(source);
    }
}
