package com.buy.date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DateUtil {  
	
	public static final String pattern_ymd = "yyyy-MM-dd"; // pattern_ymd
	public static final String pattern_ymd_hms = "yyyy-MM-dd HH:mm:ss"; // pattern_ymdtime
	public static final String pattern_ymd_hms_s = "yyyy-MM-dd HH:mm:ss:SSS"; // pattern_ymdtimeMillisecond
  
    /** 
     * 获取SimpleDateFormat 
     * @param parttern 日期格式 
     * @return SimpleDateFormat对象 
     * @throws RuntimeException 异常：非法日期格式 
     */  
    private static SimpleDateFormat getDateFormat(String parttern) throws RuntimeException {  
        return new SimpleDateFormat(parttern);  
    }  
  
    /** 
     * 获取日期中的某数值。如获取月份 
     * @param date 日期 
     * @param dateType 日期格式 
     * @return 数值 
     */  
    private static int getInteger(Date date, int dateType) {  
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        return calendar.get(dateType);  
    }  
      
    /** 
     * 增加日期中某类型的某数值。如增加日期 
     * @param date 日期字符串 
     * @param dateType 类型 
     * @param amount 数值 
     * @return 计算后日期字符串 
     */  
    private static String addInteger(String date, int dateType, int amount) {  
        String dateString = null;  
        DateStyle dateStyle = getDateStyle(date);  
        if (null != dateStyle) {  
            Date myDate = StringToDate(date, dateStyle);  
            myDate = addInteger(myDate, dateType, amount);  
            dateString = DateToString(myDate, dateStyle);  
        }  
        return dateString;  
    }  
      
    /** 
     * 增加日期中某类型的某数值。如增加日期 
     * @param date 日期 
     * @param dateType 类型 
     * @param amount 数值 
     * @return 计算后日期 
     */  
    private static Date addInteger(Date date, int dateType, int amount) {  
        Date myDate = null;  
        if (null != date) {  
            Calendar calendar = Calendar.getInstance();  
            calendar.setTime(date);  
            calendar.add(dateType, amount);  
            myDate = calendar.getTime();  
        }  
        return myDate;  
    }  
  
    /** 
     * 获取精确的日期 
     * @param timestamps 时间long集合 
     * @return 日期 
     */  
    private static Date getAccurateDate(List<Long> timestamps) {  
        Date date = null;  
        long timestamp = 0;  
        Map<Long, long[]> map = new HashMap<Long, long[]>();  
        List<Long> absoluteValues = new ArrayList<Long>();  
  
        if (null != timestamps && timestamps.size() > 0) {  
            if (timestamps.size() > 1) {  
                for (int i = 0; i < timestamps.size(); i++) {  
                    for (int j = i + 1; j < timestamps.size(); j++) {  
                        long absoluteValue = Math.abs(timestamps.get(i) - timestamps.get(j));  
                        absoluteValues.add(absoluteValue);  
                        long[] timestampTmp = { timestamps.get(i), timestamps.get(j) };  
                        map.put(absoluteValue, timestampTmp);  
                    }  
                }  
  
                // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的  
                long minAbsoluteValue = -1;  
                if (!absoluteValues.isEmpty()) {  
                    // 如果timestamps的size为2，这是差值只有一个，因此要给默认值  
                    minAbsoluteValue = absoluteValues.get(0);  
                }  
                for (int i = 0; i < absoluteValues.size(); i++) {  
                    for (int j = i + 1; j < absoluteValues.size(); j++) {  
                        if (absoluteValues.get(i) > absoluteValues.get(j)) {  
                            minAbsoluteValue = absoluteValues.get(j);  
                        } else {  
                            minAbsoluteValue = absoluteValues.get(i);  
                        }  
                    }  
                }  
  
                if (minAbsoluteValue != -1) {  
                    long[] timestampsLastTmp = map.get(minAbsoluteValue);  
                    if (absoluteValues.size() > 1) {  
                        timestamp = Math.max(timestampsLastTmp[0], timestampsLastTmp[1]);  
                    } else if (1 == absoluteValues.size()) {  
                        // 当timestamps的size为2，需要与当前时间作为参照  
                        long dateOne = timestampsLastTmp[0];  
                        long dateTwo = timestampsLastTmp[1];  
                        if ((Math.abs(dateOne - dateTwo)) < 100000000000L) {  
                            timestamp = Math.max(timestampsLastTmp[0], timestampsLastTmp[1]);  
                        } else {  
                            long now = new Date().getTime();  
                            if (Math.abs(dateOne - now) <= Math.abs(dateTwo - now)) {  
                                timestamp = dateOne;  
                            } else {  
                                timestamp = dateTwo;  
                            }  
                        }  
                    }  
                }  
            } else {  
                timestamp = timestamps.get(0);  
            }  
        }  
  
        if (0 != timestamp) {  
            date = new Date(timestamp);  
        }  
        return date;  
    }  
  
    /** 
     * 判断字符串是否为日期字符串 
     * @param date 日期字符串 
     * @return true or false 
     */  
    public static boolean isDate(String date) {  
        boolean isDate = false;  
        if (null != date) {  
            if (null != StringToDate(date)) {  
                isDate = true;  
            }  
        }  
        return isDate;  
    }  
    
    /**
     * 判断字符串是否为传入指定格式的日期字符串
     * @param date
     * @param parttern
     * @return
     * @author chenhg
     * 2016年6月22日 上午11:30:34
     */
    public static boolean isDateForParttern(String date, String parttern){
    	 boolean isDate = false;  
         if (null != date) {  
             if (null != StringToDate(date, parttern)) {  
                 isDate = true;  
             }  
         }  
         return isDate;  
    }
  
    /** 
     * 获取日期字符串的日期风格。失敗返回null。 
     * @param date 日期字符串 
     * @return 日期风格 
     */  
    public static DateStyle getDateStyle(String date) {  
        DateStyle dateStyle = null;  
        Map<Long, DateStyle> map = new HashMap<Long, DateStyle>();  
        List<Long> timestamps = new ArrayList<Long>();  
        for (DateStyle style : DateStyle.values()) {  
            Date dateTmp = StringToDate(date, style.getValue());  
            if (null != dateTmp) {  
                timestamps.add(dateTmp.getTime());  
                map.put(dateTmp.getTime(), style);  
            }  
        }  
        dateStyle = map.get(getAccurateDate(timestamps).getTime());  
        return dateStyle;  
    }  
  
    /** 
     * 将日期字符串转化为日期。失败返回null。 
     * @param date 日期字符串 
     * @return 日期 
     */  
    public static Date StringToDate(String date) {  
        DateStyle dateStyle = null;  
        return StringToDate(date, dateStyle);  
    }  
  
    /** 
     * 将日期字符串转化为日期。失败返回null。 
     * @param date 日期字符串 
     * @param parttern 日期格式 
     * @return 日期 
     */  
    public static Date StringToDate(String date, String parttern) {  
        Date myDate = null;  
        if (null != date) {  
            try {  
                myDate = getDateFormat(parttern).parse(date);  
            } catch (Exception e) {  
            }  
        }  
        return myDate;  
    }  
  
    /** 
     * 将日期字符串转化为日期。失败返回null。 
     * @param date 日期字符串 
     * @param dateStyle 日期风格 
     * @return 日期 
     */  
    public static Date StringToDate(String date, DateStyle dateStyle) {  
        Date myDate = null;  
        if (null == dateStyle) {  
            List<Long> timestamps = new ArrayList<Long>();  
            for (DateStyle style : DateStyle.values()) {  
                Date dateTmp = StringToDate(date, style.getValue());  
                if (null != dateTmp) {  
                    timestamps.add(dateTmp.getTime());  
                }  
            }  
            myDate = getAccurateDate(timestamps);  
        } else {  
            myDate = StringToDate(date, dateStyle.getValue());  
        }  
        return myDate;  
    }  
  
    /** 
     * 将日期转化为日期字符串。失败返回null。 
     * @param date 日期 
     * @param parttern 日期格式 
     * @return 日期字符串 
     */  
    public static String DateToString(Date date, String parttern) {  
        String dateString = null;  
        if (null != date) {  
            try {  
                dateString = getDateFormat(parttern).format(date);  
            } catch (Exception e) {  
            }  
        }  
        return dateString;  
    }  
  
    /** 
     * 将日期转化为日期字符串。失败返回null。 
     * @param date 日期 
     * @param dateStyle 日期风格 
     * @return 日期字符串 
     */  
    public static String DateToString(Date date, DateStyle dateStyle) {  
        String dateString = null;  
        if (null != dateStyle) {  
            dateString = DateToString(date, dateStyle.getValue());  
        }  
        return dateString;  
    }  
  
    /** 
     * 将日期字符串转化为另一日期字符串。失败返回null。 
     * @param date 旧日期字符串 
     * @param parttern 新日期格式 
     * @return 新日期字符串 
     */  
    public static String StringToString(String date, String parttern) {  
        return StringToString(date, null, parttern);  
    }  
  
    /** 
     * 将日期字符串转化为另一日期字符串。失败返回null。 
     * @param date 旧日期字符串 
     * @param dateStyle 新日期风格 
     * @return 新日期字符串 
     */  
    public static String StringToString(String date, DateStyle dateStyle) {  
        return StringToString(date, null, dateStyle);  
    }  
  
    /** 
     * 将日期字符串转化为另一日期字符串。失败返回null。 
     * @param date 旧日期字符串 
     * @param olddParttern 旧日期格式 
     * @param newParttern 新日期格式 
     * @return 新日期字符串 
     */  
    public static String StringToString(String date, String olddParttern, String newParttern) {  
        String dateString = null;  
        if (null == olddParttern) {  
            DateStyle style = getDateStyle(date);  
            if (null != style) {  
                Date myDate = StringToDate(date, style.getValue());  
                dateString = DateToString(myDate, newParttern);  
            }  
        } else {  
            Date myDate = StringToDate(date, olddParttern);  
            dateString = DateToString(myDate, newParttern);  
        }  
        return dateString;  
    }  
  
    /** 
     * 将日期字符串转化为另一日期字符串。失败返回null。 
     * @param date 旧日期字符串 
     * @param olddDteStyle 旧日期风格 
     * @param newDateStyle 新日期风格 
     * @return 新日期字符串 
     */  
    public static String StringToString(String date, DateStyle olddDteStyle, DateStyle newDateStyle) {  
        String dateString = null;  
        if (null == olddDteStyle) {  
            DateStyle style = getDateStyle(date);  
            dateString = StringToString(date, style.getValue(), newDateStyle.getValue());  
        } else {  
            dateString = StringToString(date, olddDteStyle.getValue(), newDateStyle.getValue());  
        }  
        return dateString;  
    }  
  
    /** 
     * 增加日期的年份。失败返回null。 
     * @param date 日期 
     * @param yearAmount 增加数量。可为负数 
     * @return 增加年份后的日期字符串 
     */  
    public static String addYear(String date, int yearAmount) {  
        return addInteger(date, Calendar.YEAR, yearAmount);  
    }  
      
    /** 
     * 增加日期的年份。失败返回null。 
     * @param date 日期 
     * @param yearAmount 增加数量。可为负数 
     * @return 增加年份后的日期 
     */  
    public static Date addYear(Date date, int yearAmount) {  
        return addInteger(date, Calendar.YEAR, yearAmount);  
    }  
      
    /** 
     * 增加日期的月份。失败返回null。 
     * @param date 日期 
     * @param yearAmount 增加数量。可为负数 
     * @return 增加月份后的日期字符串 
     */  
    public static String addMonth(String date, int monthAmount) {  
        return addInteger(date, Calendar.MONTH, monthAmount);  
    }  
      
    /** 
     * 增加日期的月份。失败返回null。 
     * @param date 日期 
     * @param monthAmount 增加数量。可为负数 
     * @return 增加月份后的日期 
     */  
    public static Date addMonth(Date date, int monthAmount) {  
        return addInteger(date, Calendar.MONTH, monthAmount);  
    }  
      
    /** 
     * 增加日期的天数。失败返回null。 
     * @param date 日期字符串 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加天数后的日期字符串 
     */  
    public static String addDay(String date, int dayAmount) {  
        return addInteger(date, Calendar.DATE, dayAmount);  
    }  
  
    /** 
     * 增加日期的天数。失败返回null。 
     * @param date 日期 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加天数后的日期 
     */  
    public static Date addDay(Date date, int dayAmount) {  
        return addInteger(date, Calendar.DATE, dayAmount);  
    }  
      
    /** 
     * 增加日期的小时。失败返回null。 
     * @param date 日期字符串 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加小时后的日期字符串 
     */  
    public static String addHour(String date, int hourAmount) {  
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);  
    }  
  
    /** 
     * 增加日期的小时。失败返回null。 
     * @param date 日期 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加小时后的日期 
     */  
    public static Date addHour(Date date, int hourAmount) {  
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);  
    }  
      
    /** 
     * 增加日期的分钟。失败返回null。 
     * @param date 日期字符串 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加分钟后的日期字符串 
     */  
    public static String addMinute(String date, int hourAmount) {  
        return addInteger(date, Calendar.MINUTE, hourAmount);  
    }  
  
    /** 
     * 增加日期的分钟。失败返回null。 
     * @param date 日期 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加分钟后的日期 
     */  
    public static Date addMinute(Date date, int hourAmount) {  
        return addInteger(date, Calendar.MINUTE, hourAmount);  
    }  
      
    /** 
     * 增加日期的秒钟。失败返回null。 
     * @param date 日期字符串 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加秒钟后的日期字符串 
     */  
    public static String addSecond(String date, int hourAmount) {  
        return addInteger(date, Calendar.SECOND, hourAmount);  
    }  
  
    /** 
     * 增加日期的秒钟。失败返回null。 
     * @param date 日期 
     * @param dayAmount 增加数量。可为负数 
     * @return 增加秒钟后的日期 
     */  
    public static Date addSecond(Date date, int hourAmount) {  
        return addInteger(date, Calendar.SECOND, hourAmount);  
    }  
  
    /** 
     * 获取日期的年份。失败返回0。 
     * @param date 日期字符串 
     * @return 年份 
     */  
    public static int getYear(String date) {  
        return getYear(StringToDate(date));  
    }  
  
    /** 
     * 获取日期的年份。失败返回0。 
     * @param date 日期 
     * @return 年份 
     */  
    public static int getYear(Date date) {  
        return getInteger(date, Calendar.YEAR);  
    }  
  
    /** 
     * 获取日期的月份。失败返回0。 
     * @param date 日期字符串 
     * @return 月份 
     */  
    public static int getMonth(String date) {  
        return getMonth(StringToDate(date));  
    }  
  
    /** 
     * 获取日期的月份。失败返回0。 
     * @param date 日期 
     * @return 月份 
     */  
    public static int getMonth(Date date) {  
        return getInteger(date, Calendar.MONTH)+1;  
    }  
  
    /** 
     * 获取日期的天数。失败返回0。 
     * @param date 日期字符串 
     * @return 天 
     */  
    public static int getDay(String date) {  
        return getDay(StringToDate(date));  
    }  
  
    /** 
     * 获取日期的天数。失败返回0。 
     * @param date 日期 
     * @return 天 
     */  
    public static int getDay(Date date) {  
        return getInteger(date, Calendar.DATE);  
    }  
      
    /** 
     * 获取日期的小时。失败返回0。 
     * @param date 日期字符串 
     * @return 小时 
     */  
    public static int getHour(String date) {  
        return getHour(StringToDate(date));  
    }  
  
    /** 
     * 获取日期的小时。失败返回0。 
     * @param date 日期 
     * @return 小时 
     */  
    public static int getHour(Date date) {  
        return getInteger(date, Calendar.HOUR_OF_DAY);  
    }  
      
    /** 
     * 获取日期的分钟。失败返回0。 
     * @param date 日期字符串 
     * @return 分钟 
     */  
    public static int getMinute(String date) {  
        return getMinute(StringToDate(date));  
    }  
  
    /** 
     * 获取日期的分钟。失败返回0。 
     * @param date 日期 
     * @return 分钟 
     */  
    public static int getMinute(Date date) {  
        return getInteger(date, Calendar.MINUTE);  
    }  
      
    /** 
     * 获取日期的秒钟。失败返回0。 
     * @param date 日期字符串 
     * @return 秒钟 
     */  
    public static int getSecond(String date) {  
        return getSecond(StringToDate(date));  
    }  
  
    /** 
     * 获取日期的秒钟。失败返回0。 
     * @param date 日期 
     * @return 秒钟 
     */  
    public static int getSecond(Date date) {  
        return getInteger(date, Calendar.SECOND);  
    }  
  
    /** 
     * 获取日期 。默认yyyy-MM-dd格式。失败返回null。 
     * @param date 日期字符串 
     * @return 日期 
     */  
    public static String getDate(String date) {  
        return StringToString(date, DateStyle.YYYY_MM_DD);  
    }  
  
    /** 
     * 获取日期。默认yyyy-MM-dd格式。失败返回null。 
     * @param date 日期 
     * @return 日期 
     */  
    public static String getDate(Date date) {  
        return DateToString(date, DateStyle.YYYY_MM_DD);  
    }  
  
    /** 
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。 
     * @param date 日期字符串 
     * @return 时间 
     */  
    public static String getTime(String date) {  
        return StringToString(date, DateStyle.HH_MM_SS);  
    }  
  
    /** 
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。 
     * @param date 日期 
     * @return 时间 
     */  
    public static String getTime(Date date) {  
        return DateToString(date, DateStyle.HH_MM_SS);  
    }  
  
    /** 
     * 获取日期的星期。失败返回null。 
     * @param date 日期字符串 
     * @return 星期 
     */  
    public static Week getWeek(String date) {  
        Week week = null;  
        DateStyle dateStyle = getDateStyle(date);  
        if (null != dateStyle) {  
            Date myDate = StringToDate(date, dateStyle);  
            week = getWeek(myDate);  
        }  
        return week;  
    }  
  
    /** 
     * 获取日期的星期。失败返回null。 
     * @param date 日期 
     * @return 星期 
     */  
    public static Week getWeek(Date date) {  
        Week week = null;  
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        int weekNumber = calendar.get(Calendar.DAY_OF_WEEK) - 1;  
        switch (weekNumber) {  
        case 0:  
            week = Week.SUNDAY;  
            break;  
        case 1:  
            week = Week.MONDAY;  
            break;  
        case 2:  
            week = Week.TUESDAY;  
            break;  
        case 3:  
            week = Week.WEDNESDAY;  
            break;  
        case 4:  
            week = Week.THURSDAY;  
            break;  
        case 5:  
            week = Week.FRIDAY;  
            break;  
        case 6:  
            week = Week.SATURDAY;  
            break;  
        }  
        return week;  
    }  
      
    /** 
     * 获取两个日期相差的天数 
     * @param date 日期字符串 
     * @param otherDate 另一个日期字符串 
     * @return 相差天数 
     */  
    public static int getIntervalDays(String date, String otherDate) {  
        return getIntervalDays(StringToDate(date), StringToDate(otherDate));  
    }  
      
    /** 
     * @param date 日期 
     * @param otherDate 另一个日期 
     * @return 相差天数 
     */  
    public static int getIntervalDays(Date date, Date otherDate) {
        long time = Math.abs(date.getTime() - otherDate.getTime());  
        return (int) (time/(24 * 60 * 60 * 1000));  
    }  
    
    /** 
     * @param date 日期 
     * @param otherDate 另一个日期 
     * @return 相差小时
     */  
    public static int getIntervalHours(Date date, Date otherDate) {  
        date = DateUtil.StringToDate(DateUtil.getDate(date));  
        long time = Math.abs(date.getTime() - otherDate.getTime());  
        return (int) (time/(60 * 60 * 1000));  
    }
    
    /** 
     * @param date 日期 
     * @param otherDate 另一个日期 
     * @return 相差分钟数
     */  
    public static int getIntervalMinu(Date date, Date otherDate) {  
    	date = DateUtil.StringToDate(DateUtil.DateToString(date, pattern_ymd_hms), pattern_ymd_hms);  
    	long time = Math.abs(date.getTime() - otherDate.getTime());
    	return (int) (time/(60 * 1000));  
    }
    
    /** 
     * @param date 日期 
     * @param otherDate 另一个日期 
     * @return 相差秒数（有正负），大于返回正数，小于返回负数
     */  
    public static long getIntervalSec(Date date, Date otherDate) {  
    	date = DateUtil.StringToDate(DateUtil.DateToString(date, pattern_ymd_hms), pattern_ymd_hms);  
    	long time = otherDate.getTime() - date.getTime();
    	return time/1000;  
    }
    
   
    
    /**
     * 获取当天最小时间点
     * @param date
     * @return
     * @author huangzq
     */
    public static Date getMinDate(Date date){
    	return StringToDate(DateToString(date,"yyyy-MM-dd"));
    	
    }
    /**
     * 获取当天最大时间点
     * @param date
     * @return
     * @author huangzq
     */
    public static Date getMaxDate(Date date){
    	return StringToDate(DateToString(date,"yyyy-MM-dd")+" 23:59:59");
    	
    }
    
	/**
	 * 获取上周日的日期.
	 * 
	 * @param intervalWeek
	 *            间隔的周数.
	 * @author Chengyb
	 * @return
	 */
    public static Date getSunday(Calendar calendar, Integer intervalWeek) {
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		calendar.add(Calendar.WEEK_OF_MONTH, intervalWeek);
		
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		
		return calendar.getTime();
    }
    
    /**
     * 获取昨天的凌晨（0:00)
     * @return
     * @author chenhg
     * 2016年2月23日 下午1:54:52
     */
    public static Date getYesterdayBegin(Date date){
    	return StringToDate(DateToString(addDay(date, -1),"yyyy-MM-dd")+" 00:00:00");
    }
    
    /**
     * 获取昨天的最大时间（23:59:59）
     * @return
     * @author chenhg
     * 2016年2月23日 下午1:54:52
     */
    public static Date getYesterdayEnd(Date date){
    	return StringToDate(DateToString(addDay(date, -1),"yyyy-MM-dd")+" 23:59:59");
    }
    
    /**
	 * 获取显示字符串.
	 * 
	 * @author Chengyb
	 */
	public static String showText(Date date) {
		String[] weekDays = { "日", "一", "二", "三", "四", "五", "六" };

		// 上周日.
		Calendar.getInstance().setTime(new Date());
		Date lastSunday = DateUtil.getSunday(Calendar.getInstance(), -1);
		
		
		// 上上周日.
		Calendar.getInstance().setTime(new Date());
		Date last2Sunday = DateUtil.getSunday(Calendar.getInstance(), -2);

		// =====================================
		// 【周】显示字符.
		// =====================================*/
		String weekString = null;

		switch (DateUtil.getIntervalDays(new Date(), date)) {
		case 0:
			return "今天";
		case 1:
			return "昨天";
		case 2:
			return "前天";

		default:
			if (date.after(lastSunday)) {
				weekString = "星期"; // 本周.
			} else if (date.before(lastSunday) && date.after(last2Sunday)) {
				weekString = "上周"; // 上周.
			} else if (date.before(last2Sunday)) {
				return "更早之前";
			}

			// =====================================
			// 【日】显示字符.
			// =====================================*/
			String dayString = null;

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
			dayString = w < 0 ? weekDays[0] : weekDays[w];

			return weekString + dayString;
		}
	}
	
	/**
	 * 比较日期时间
	 * @param date
	 * @param otherDate
	 * @param type 1：比较到月份 2：比较到哪一号 3：比较到小时 4：比较到分钟 5：比较到秒
	 * @return 1:date>otherDate 0:date == otherDate -1:date < otherDate
	 * @author chenhg
	 * 2016年8月23日 下午2:32:23
	 */
	public static int compareTime(Date date, Date otherDate, int type){
		DateStyle dateStyle = null;
		switch (type) {
		case 1:
			dateStyle = DateStyle.YYYY_MM;
			break;
		case 2:
			dateStyle = DateStyle.YYYY_MM_DD;
			break;
		case 3:
			dateStyle = DateStyle.YYYY_MM_DD_HH;
			break;
		case 4:
			dateStyle = DateStyle.YYYY_MM_DD_HH_MM;
			break;
		default:
			dateStyle = DateStyle.YYYY_MM_DD_HH_MM_SS;
			break;
		}
		long time = StringToDate(DateToString(date, dateStyle),dateStyle).getTime();
		long otherTime = StringToDate(DateToString(otherDate, dateStyle),dateStyle).getTime();
		if(time == otherTime){
			return 0;
		}else if(time > otherTime){
			return 1;
		}else{
			return -1;
		}
	}
	
	/**
	 * 本月第一天
	 */
	public static String getThisMonthFirstDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return new StringBuffer()
				.append(cal.get(Calendar.YEAR))
				.append("-")
				.append(cal.get(Calendar.MONTH) + 1)
				.append("-")
				.append(cal.get(Calendar.DATE))
				.append(" 00:00:00")
				.toString();
	}
	
	/**
	 * 本月最后一天
	 */
	public static String getThisMonthLastDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return new StringBuffer()
				.append(cal.get(Calendar.YEAR))
				.append("-")
				.append(cal.get(Calendar.MONTH) + 1)
				.append("-")
				.append(cal.get(Calendar.DATE))
				.append(" 23:59:59")
				.toString();
	}
    
}  