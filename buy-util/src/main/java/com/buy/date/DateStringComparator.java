package com.buy.date;

import java.util.Date;

/**
 * 时间字符串比较器.
 * 
 * @author Chengyb
 *
 */
public class DateStringComparator implements Comparable<String> {
	
	public int compare(String str1, String str2) {
		// 处理“上周”“更早之前”特殊情况.
		if(str1.equals("上周") && str2.equals("更早之前")) {
			return -1;
		}
		if(str1.equals("上周") || str1.equals("更早之前")) {
			return 1;
		}
		if(str2.equals("上周") || str2.equals("更早之前")) {
			return -1;
		}
		
		Date date1 = DateUtil.StringToDate(str1);
		Date date2 = DateUtil.StringToDate(str2);
		return date2.compareTo(date1);
	}

	@Override
	public int compareTo(String o) {
		return 0;
	}

}