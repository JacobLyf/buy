package com.buy.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class ValidatorUtil {
	
	/** boolean specifying by default whether or not it is okay for a String to be empty */
    public static final boolean defaultEmptyOK = true;
	
	/** decimal point character differs by language and culture */
    public static final String decimalPointDelimiter = ".";
	
	// 身份实名认证

	/**
	 * 身份证前6位【ABCDEF】为行政区划数字代码（简称数字码）说明（参考《GB/T 2260-2007 中华人民共和国行政区划代码》）：
	 * 该数字码的编制原则和结构分析，它采用三层六位层次码结构，按层次分别表示我国各省（自治区，直辖市，特别行政区）、
	 * 市（地区，自治州，盟）、县（自治县、县级市、旗、自治旗、市辖区、林区、特区）。 数字码码位结构从左至右的含义是：
	 * 第一层为AB两位代码表示省、自治区、直辖市、特别行政区；
	 * 第二层为CD两位代码表示市、地区、自治州、盟、直辖市所辖市辖区、县汇总码、省（自治区）直辖县级行政区划汇总码，其中：
	 * ——01~20、51~70表示市，01、02还用于表示直辖市所辖市辖区、县汇总码； ——21~50表示地区、自治州、盟；
	 * ——90表示省（自治区）直辖县级行政区划汇总码。 第三层为EF两位表示县、自治县、县级市、旗、自治旗、市辖区、林区、特区，其中：
	 * ——01~20表示市辖区、地区（自治州、盟）辖县级市、市辖特区以及省（自治区）直辖县级行政区划中的县级市，01通常表示辖区汇总码；
	 * ——21~80表示县、自治县、旗、自治旗、林区、地区辖特区； ——81~99表示省（自治区）辖县级市。
	 */

	/**
	 * <p>
	 * 类说明:身份证合法性校验
	 * </p>
	 * <p>
	 * --15位身份证号码：第7、8位为出生年份(两位数)，第9、10位为出生月份，第11、12位代表出生日期，第15位代表性别，奇数为男，偶数为女。
	 * --18位身份证号码
	 * ：第7、8、9、10位为出生年份(四位数)，第11、第12位为出生月份，第13、14位代表出生日期，第17位代表性别，奇数为男，偶数为女。
	 * </p>
	 */

	// 每位加权因子
	private int power[] = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/**
	 * 验证所有的身份证的合法性
	 * 
	 * @param idcard
	 * @return
	 */
	public boolean isValidatedAllIdcard(String idcard) {
		if (is15Idcard(idcard)) {
			idcard = this.convertIdcarBy15bit(idcard);
			if (null == idcard) {
				return false;
			}
		} else if (!is18Idcard(idcard)) {

			return false;
		}
		return this.isValidate18Idcard(idcard);
	}

	/**
	 * 
	 * 判断18位身份证的合法性
	 * 
	 * @param idcard
	 * @return
	 */
	public boolean isValidate18Idcard(String idcard) {
		// 非18位为假
		if (18 != idcard.length()) {
			return false;
		}
		// 获取前17位
		String idcard17 = idcard.substring(0, 17);
		// 获取第18位
		String idcard18Code = idcard.substring(17, 18);
		char c[] = null;
		String checkCode = "";
		// 是否都为数字
		if (isDigital(idcard17)) {
			c = idcard17.toCharArray();
		} else {
			return false;
		}

		if (null != c) {
			int bit[] = new int[idcard17.length()];

			bit = converCharToInt(c);

			int sum17 = 0;

			sum17 = getPowerSum(bit);

			// 将和值与11取模得到余数进行校验码判断
			checkCode = getCheckCodeBySum(sum17);
			if (null == checkCode) {
				return false;
			}
			// 将身份证的第18位与算出来的校码进行匹配，不相等就为假
			if (!idcard18Code.equalsIgnoreCase(checkCode)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 将15位的身份证转成18位身份证
	 * 
	 * @param idcard
	 * @return
	 */
	public String convertIdcarBy15bit(String idcard) {
		String idcard17 = null;
		// 非15位身份证
		if (15 != idcard.length()) {
			return null;
		}

		if (isDigital(idcard)) {
			// 获取出生年月日
			String birthday = idcard.substring(6, 12);
			Date birthdate = null;
			try {
				birthdate = new SimpleDateFormat("yyMMdd").parse(birthday);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
			Calendar cday = Calendar.getInstance();
			cday.setTime(birthdate);
			String year = String.valueOf(cday.get(Calendar.YEAR));

			idcard17 = idcard.substring(0, 6) + year + idcard.substring(8);

			char c[] = idcard17.toCharArray();
			String checkCode = "";

			if (null != c) {
				int bit[] = new int[idcard17.length()];

				// 将字符数组转为整型数组
				bit = converCharToInt(c);
				int sum17 = 0;
				sum17 = getPowerSum(bit);

				// 获取和值与11取模得到余数进行校验码
				checkCode = getCheckCodeBySum(sum17);
				// 获取不到校验位
				if (null == checkCode) {
					return null;
				}

				// 将前17位与第18位校验码拼接
				idcard17 += checkCode;
			}
		} else { // 身份证包含数字
			return null;
		}
		return idcard17;
	}

	/**
	 * 15位和18位身份证号码的基本数字和位数验校
	 * 
	 * @param idcard
	 * @return
	 */
	public boolean isIdcard(String idcard) {
		return null == idcard || "".equals(idcard) ? false : Pattern.matches(
				"(^\\d{15}$)|(\\d{17}(?:\\d|x|X)$)", idcard);
	}

	/**
	 * 15位身份证号码的基本数字和位数验校
	 * 
	 * @param idcard
	 * @return
	 */
	public boolean is15Idcard(String idcard) {
		return Pattern.matches(
				"^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$",
				idcard);
	}

	/**
	 * 18位身份证号码的基本数字和位数验校
	 * 
	 * @param idcard
	 * @return
	 */
	public boolean is18Idcard(String idcard) {
		return Pattern
				.matches(
						"^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([\\d|x|X]{1})$",
						idcard);
	}

	/**
	 * 数字验证
	 * 
	 * @param str
	 * @return
	 */
	public boolean isDigital(String str) {
		return null == str || "".equals(str) ? false : str.matches("^[0-9]*$");
	}

	/**
	 * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
	 * 
	 * @param bit
	 * @return
	 */
	public int getPowerSum(int[] bit) {

		int sum = 0;

		if (power.length != bit.length) {
			return sum;
		}

		for (int i = 0; i < bit.length; i++) {
			for (int j = 0; j < power.length; j++) {
				if (i == j) {
					sum = sum + bit[i] * power[j];
				}
			}
		}
		return sum;
	}

	/**
	 * 将和值与11取模得到余数进行校验码判断
	 * 
	 * @param checkCode
	 * @param sum17
	 * @return 校验位
	 */
	public String getCheckCodeBySum(int sum17) {
		String checkCode = null;
		switch (sum17 % 11) {
		case 10:
			checkCode = "2";
			break;
		case 9:
			checkCode = "3";
			break;
		case 8:
			checkCode = "4";
			break;
		case 7:
			checkCode = "5";
			break;
		case 6:
			checkCode = "6";
			break;
		case 5:
			checkCode = "7";
			break;
		case 4:
			checkCode = "8";
			break;
		case 3:
			checkCode = "9";
			break;
		case 2:
			checkCode = "x";
			break;
		case 1:
			checkCode = "0";
			break;
		case 0:
			checkCode = "1";
			break;
		}
		return checkCode;
	}

	/**
	 * 将字符数组转为整型数组
	 * 
	 * @param c
	 * @return
	 * @throws NumberFormatException
	 */
	public int[] converCharToInt(char[] c) throws NumberFormatException {
		int[] a = new int[c.length];
		int k = 0;
		for (char temp : c) {
			a[k++] = Integer.parseInt(String.valueOf(temp));
		}
		return a;
	}
	
	/**
	 * True if string s is an unsigned floating point(real) number.
    *
    *  Also returns true for unsigned integers. If you wish
    *  to distinguish between integers and floating point numbers,
    *  first call isInteger, then call isFloat.
    *
    *  Does not accept exponential notation.
    */
   public static boolean isFloat(String s) {
       if (isEmpty(s)) return defaultEmptyOK;

       boolean seenDecimalPoint = false;

       if (s.startsWith(decimalPointDelimiter)) return false;

       // Search through string's characters one by one
       // until we find a non-numeric character.
       // When we do, return false; if we don't, return true.
       for (int i = 0; i < s.length(); i++) {
           // Check that current character is number.
           char c = s.charAt(i);

           if (c == decimalPointDelimiter.charAt(0)) {
               if (!seenDecimalPoint) {
                   seenDecimalPoint = true;
               } else {
                   return false;
               }
           } else {
               if (!isDigit(c)) return false;
           }
       }
       // All characters are numbers.
       return true;
   }

   /**
    * General routine for testing whether a string is a float.
    */
   public static boolean isFloat(String s, boolean allowNegative, boolean allowPositive, int minDecimal, int maxDecimal) {
       if (isEmpty(s)) return defaultEmptyOK;

       try {
           float temp = Float.parseFloat(s);
           if (!allowNegative && temp < 0) return false;
           if (!allowPositive && temp > 0) return false;
           int decimalPoint = s.indexOf(".");
           if (decimalPoint == -1) {
               if (minDecimal > 0) return false;
               return true;
           }
           // 1.2345; length=6; point=1; num=4
           int numDecimals = s.length() - decimalPoint - 1;
           if (minDecimal >= 0 && numDecimals < minDecimal) return false;
           if (maxDecimal >= 0 && numDecimals > maxDecimal) return false;
           return true;
       } catch (Exception e) {
           return false;
       }
   }

   /**
    * General routine for testing whether a string is a double.
    */
   public static boolean isDouble(String s, boolean allowNegative, boolean allowPositive, int minDecimal, int maxDecimal) {
       if (isEmpty(s)) return defaultEmptyOK;

       try {
           double temp = Double.parseDouble(s);
           if (!allowNegative && temp < 0) return false;
           if (!allowPositive && temp > 0) return false;
           int decimalPoint = s.indexOf(".");
           if (decimalPoint == -1) {
               if (minDecimal > 0) return false;
               return true;
           }
           // 1.2345; length=6; point=1; num=4
           int numDecimals = s.length() - decimalPoint - 1;
           if (minDecimal >= 0 && numDecimals < minDecimal) return false;
           if (maxDecimal >= 0 && numDecimals > maxDecimal) return false;
           return true;
       } catch (Exception e) {
           return false;
       }
   }
   
   /** Check whether string s is empty. */
   public static boolean isEmpty(String s) {
       return (null == s) || 0 == s.length();
   }
   
   /** Returns true if character c is a digit (0 .. 9). */
   public static boolean isDigit(char c) {
       return Character.isDigit(c);
   }

	public static void main(String[] args) {

		String idcard15 = "110105582wewe98";// 15位
		String idcard18 = "440902199dawda2174685";// 18位
		ValidatorUtil iv = new ValidatorUtil();
		System.out.println(iv.isValidatedAllIdcard(idcard15));
		System.out.println(iv.isValidatedAllIdcard(idcard18));
		System.out.println(iv.is15Idcard(idcard15));

	}

}
