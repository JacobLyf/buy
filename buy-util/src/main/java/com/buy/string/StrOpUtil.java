package com.buy.string;




public class StrOpUtil {

	 /**
     * 默认的空值
     */
    public static final String EMPTY = "";
 
    /**
     * 检查字符串是否为空
     * @param str 字符串
     * @return
     */
    public static boolean isEmpty(String str) {
        if (null == str) {
            return true;
        } else if (0 == str.length()) {
            return true;
        } else {
            return false;
        }
    }
     
    /**
     * 检查字符串是否为空
     * @param str 字符串
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
 
    /**
     * 截取并保留标志位之前的字符串
     * @param str 字符串
     * @param expr 分隔符
     * @return
     */
    public static String substringBefore(String str, String expr) {
        if (isEmpty(str) || null == expr) {
            return str;
        }
        if (0 == expr.length()) {
            return EMPTY;
        }
        int pos = str.indexOf(expr);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }
 
    /**
     * 截取并保留标志位之后的字符串
     * @param str 字符串
     * @param expr 分隔符
     * @return
     */
    public static String substringAfter(String str, String expr) {
        if (isEmpty(str)) {
            return str;
        }
        if (null == expr) {
            return EMPTY;
        }
        int pos = str.indexOf(expr);
        if (pos == -1) {
            return EMPTY;
        }
        return str.substring(pos + expr.length());
    }
 
    /**
     * 截取并保留最后一个标志位之前的字符串
     * @param str 字符串
     * @param expr 分隔符
     * @return
     */
     public static String substringBeforeLast(String str, String expr) {
            if (isEmpty(str) || isEmpty(expr)) {
                return str;
            }
            int pos = str.lastIndexOf(expr);
            if (pos == -1) {
                return str;
            }
            return str.substring(0, pos);
        }
      
     /**
      * 截取并保留最后一个标志位之后的字符串
      * @param str
      * @param expr 分隔符
      * @return
      */
     public static String substringAfterLast(String str, String expr) {
            if (isEmpty(str)) {
                return str;
            }
            if (isEmpty(expr)) {
                return EMPTY;
            }
            int pos = str.lastIndexOf(expr);
            if (pos == -1 || pos == (str.length() - expr.length())) {
                return EMPTY;
            }
            return str.substring(pos + expr.length());
        }
      
     /**
      * 把字符串按分隔符转换为数组
      * @param string 字符串
      * @param expr 分隔符
      * @return
      */
     public static String[] stringToArray(String string, String expr){
         return string.split(expr);
     }
      
     /**
      * 去除字符串中的空格
      * @param str
      * @return
      */
     public static String noSpace(String str){
         str = str.trim();
         str = str.replace(" ", "_");
         return str;
     }

	/**
	 * 删除input字符串中的html格式
	 * 
	 * @param arg_str
	 * @return
	 */
	public static String deleteHtmlFormatInStr(String arg_str) {
		if (null == arg_str || arg_str.trim().equals("")) {
			return "";
		}
		// 去掉所有html元素,
		String str = arg_str.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
				"<[^>]*>", "");
		str = str.replaceAll("[(/>)<]", "");

		return str;
	}

	/**
	 * 按length长度，切割arg_str字符串
	 * @param arg_str
	 * @param length
	 * @return
	 */
	public static String splitStrIfTooLong(String arg_str, int length) {
		if (null == arg_str || arg_str.trim().equals("")) {
			return "";
		}

		String str;
		int len = arg_str.length();
		if (len <= length) {

			return arg_str;

		} else {
			str = arg_str.substring(0, length);
			str += "......";
		}
		return str;
	}

	/**
	 * 删除arg_str中的html标签，并且按length 长度切割字符串
	 * 
	 * @param arg_str
	 * @param length
	 */
	public static String deleteHtmlFormatAndSplitInStr(String arg_str,
			int length) {

		return splitStrIfTooLong(deleteHtmlFormatInStr(arg_str), length);
	}
}
