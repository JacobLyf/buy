package com.buy.encrypt;

import com.buy.string.StringUtil;

/**
 * 加密 
 */
public class EncryptUtil {
	
	/**
	 * 邮箱加密
	 * @param email
	 */
	public static String encryptEmail(String email) {
		String result = "";
		if(StringUtil.notNull(email)) {
			int index = email.lastIndexOf("@");
			String temp = email.substring(0, index);
			result = encryptByBegEnd(temp) + email.substring(index, email.length());
		}
		return result;
	}
	
	/**
	 * 星号中间加密
	 * @param str	原值
	 * @return		星号机密后字符串
	 */
	public static String encryptByMid(String str) {
		int encryptFlag = 1;
		if(StringUtil.notNull(str)) {
			int length = str.length();
			if(length > 8)
				encryptFlag = encryptFlag + ((length - 5) / 3); 
			System.out.println(encryptFlag);
			String mark = "";
			for(int i = 0; i < length - (encryptFlag * 2); i++)
				mark += "*";
			return str.substring(0, encryptFlag) + mark + str.substring(str.length() - 3, str.length());
		}
		return str;
	}
	
	/**
	 * 星号前后加密
	 * @param str	原值
	 * @return		星号机密后字符串
	 */
	public static String encryptByBegEnd(String str) {
		int encryptFlag = 1;
		if(StringUtil.notNull(str)) {
			int length = str.length();
			if(length > 8)
				encryptFlag = encryptFlag + ((length - 5) / 3) + 1; 
			String mark = "";
			for(int i = 0; i < encryptFlag; i++)
				mark += "*";
			String temp = str.substring(encryptFlag, str.length() - encryptFlag);
			return mark + temp + mark;
		}
		return str;
	}

}
