package com.buy.model.sms;

/**
 * 短信内容 
 */
public class SMSContent {
	
	/**
	 * 替换短信内容
	 * @param content
	 * @param datas
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年11月19日下午6:59:02
	 */
	public static String dealSmsContent(String content, String[] datas) {
		String result = "";
		String[] contentArr = content.split(SmsAndMsgTemplate.PH_CODE);
		for (int i = 0; i < contentArr.length; i++)
			result += i == datas.length ? contentArr[i] : contentArr[i] + datas[i];
		return result;
	}

}
