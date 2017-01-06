package com.buy.plugin.event.sms.user;

import com.buy.common.BaseConstants;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 注册成功后通知
 */
@Listener(enableAsync = true)
public class RegisterSucessSmsListener implements ApplicationListener<RegisterSucessSmsEvent> {
    @Override
    public void onApplicationEvent(RegisterSucessSmsEvent event) {
        Record source = (Record) event.getSource();

        if (StringUtil.isNull(source))
            return;

        String mobile = source.get("mobile");
        String password = source.get("password");

        // 发送注册短信给会员.（延迟62秒）
        if (StringUtil.notNull(mobile) && StringUtil.notNull(password)) {
            try {
                Thread.sleep(62 * 1000);
                SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_REGISTER_SUCESS, new String[] { mobile, password }, mobile, "", "会员注册", BaseConstants.DataFrom.APP);
            }

            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
