package com.buy.regexutil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil
{
		/** 用户名  **/
		public final static String REG_USERNAME = "^\\w{6,20}$";
		/** 姓名  **/
		public final static String REG_NAME = "^\\S{2,20}$";
		/** 姓名  - 中文  **/
		public final static String REG_NAME_CN = "^[\u4e00-\u9fa5]{2,15}$";
		public final static String REG_SHOP_NAME = "^[\\s\\S]{1,30}$";
		/** 密码  **/
		public final static String REG_PASSWORD = "^[a-zA-Z0-9`~!@#$%^&*()-=_+{};'\\,./\\[\\]:\"\\|<>?]{6,20}$";
		/** 手机号码 * */
		public final static String REG_MOBILE = "^1[3-9]\\d{9}$";
		/** 电话 * */
		public final static String REG_TELEPHONE = "^[0-9][0-9_-]{2,19}$";
		/** 电子邮箱 **/
		public final static String REG_EMAIL = "^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$";
		/** 邮政编码 **/
		public final static String REG_ZIP = "^[1-9][0-9]{5}$";
		/** 地址 **/
		public final static String REG_ADDRESS = "^[\\s\\S]{1,100}$";
		/** qq **/
		public final static String REG_QQ = "^[1-9][0-9]{4,10}$";
		/** 微信 **/
		public final static String REG_WECHAT = "(^[a-zA-Z][-_a-zA-Z0-9]{5,19})|([1-9][0-9]{4,10})|(1[3-9]\\d{9})$";
		
		/** 版本号 **/
		public final static String REG_VERSION = "^[1-9]{0,1}[0-9]\\.[0-9]\\.[0-9]$";
		
		/** 店铺简介 **/
		public final static String REG_SHOP_INTRO = "^[\\s\\S]{1,100}$";
		/** 时间 **/
		public final static String REG_TIME_HM  = "^([0-1][0-9]|[2][0-3]):([0-5][0-9])|([2][4]:[0][0])$";

		/** 日期 **/
		public final static String REG_TIME_YMD = "^[1-2][0-9]{3}[-]([0]{0,1}[1-9]|[1][0-2])[-]([0]{0,1}[1-9]|[1-2][0-9]|[3][0-1])$";
		
		/** 表情 **/
		public final static String REG_EMOJI = "^[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]$";
		
		public static void main(String[] args) {
			Pattern pattern = Pattern.compile(REG_ADDRESS);
			String str = " ";
			for (int i = 0; i < 30; i++)
				str += " 啦 ";
			Matcher matcher = pattern.matcher(str);
			System.out.println(matcher.matches());
		}
		
}
