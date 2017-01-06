package com.buy.radomutil;

import java.util.Random;

/**
 * 生成随机数字符串
 * @author Sylveon
 */
public class RadomUtil {
	
	public final static String RADOM_UPPER = "QWERTYUIOPASDFGHJKLZXCVBNM";
	public final static String RADOM_LOWER = "qwertyuiopasdfghjklzxcvbnm";
	public final static String RADOM_NUMBER = "1234567890";
	public final static String RADOM_NUMBER_ODD = "13579";
	public final static String RADOM_NUMBER_EVE = "24680";
	
	/**
	 * 生成随机数字符串
	 * @param length		长度
	 * @param radom_str		随机字符串
	 * @return				生成的字符串
	 */
	public static String generate(int length, String radom_str) {
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < length; i++) {
			result.append(radom_str.charAt(new Random().nextInt(radom_str.length())));
		}
		return result.toString();
	}
	
	/**
	 * 生成随机数字符串
	 * @param length		长度
	 * @return				生成的字符串
	 */
	public static String generate(int length) {
		String radom_str = RadomUtil.RADOM_UPPER + RadomUtil.RADOM_LOWER + RadomUtil.RADOM_NUMBER;
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < length; i++) {
			result.append(radom_str.charAt(new Random().nextInt(radom_str.length())));
		}
		return result.toString();
	}
	
	/**
	 * app生成token
	 * @return
	 * @author huangzq
	 */
	public static String generateToken() {
		String randomStr = RadomUtil.RADOM_UPPER + RadomUtil.RADOM_LOWER + RadomUtil.RADOM_NUMBER;
		StringBuffer result = new StringBuffer();
		for(int i = 0; i < 10; i++) {
			result.append(randomStr.charAt(new Random().nextInt(randomStr.length())));
		}
		return result.toString();
	}
	
	
	
	
}
