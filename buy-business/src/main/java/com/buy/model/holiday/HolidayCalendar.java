package com.buy.model.holiday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HolidayCalendar {
	
	/*
	 * 常量 
	 */
	
	public final static String[] months = {"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" };
	public final static String[] weeks = {null, "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
	public final static String[] weeksSimple = {null, "日", "一", "二", "三", "四", "五", "六"};
	
	/*
	 * 变量
	 */
	
	private Date date;
	
	/*
	 * 方法
	 */
	
	public HolidayCalendar(Date date) {
		this.date = date;
	}
	
	/**
	 * 生成日历
	 * @param javaYear		java的年
	 * @param javaMonth		java的月
	 * @return				日历数组
	 */
	public static Date[] genCalendar(int javaYear, int javaMonth) {
		// 设置参数
		Calendar curr = Calendar.getInstance();
		curr.set(Calendar.YEAR, javaYear);
		curr.set(Calendar.MONTH, javaMonth);
		curr.set(Calendar.DAY_OF_MONTH, 1);
		int java_currWeekDay = curr.get(Calendar.DAY_OF_WEEK);				// 当前月 - 星期
		int java_currDays = curr.getActualMaximum(Calendar.DAY_OF_MONTH);	// 当前月 - 总天数
		int currBeg = java_currWeekDay - 1;									// 当前月 - 遍历开始下标
		int currEnd = java_currDays + currBeg;								// 当前月 - 遍历结束下标
		int date = 1;														// 当前月 - 日
		// 生成日历
		Date[] arr = new Date[42]; 
		Calendar temp = Calendar.getInstance();
		for(int i = currBeg; i < currEnd; i++) {
			temp.set(Calendar.YEAR, javaYear);
			temp.set(Calendar.MONTH, javaMonth);
			temp.set(Calendar.DAY_OF_MONTH, date);
			arr[i] = temp.getTime();
			date++;
		}
		return arr;
	}
	
	/**
	 * 生成年份
	 * @param yearBeg	开始年份
	 * @return			年份集合
	 */
	public static List<Integer> genYears(int yearBeg) {
		List<Integer> result = new ArrayList<Integer>();
		Calendar cal = Calendar.getInstance();
		int yserEnd = cal.get(Calendar.YEAR);
		for(int i = yearBeg; i <= yserEnd; i++)
			result.add(i);
		return result;
	}
	
	/*
	 * get / set
	 */
	
	public Date getDate() {
		return date;
	}

}
