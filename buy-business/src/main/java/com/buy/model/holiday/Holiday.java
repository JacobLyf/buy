package com.buy.model.holiday;

import java.util.Date;

import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.jfinal.plugin.activerecord.Model;

public class Holiday extends Model<Holiday>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static Holiday dao = new Holiday();
	
	/**
	 * 根据日期获取节假日
	 * @param date
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月15日上午9:27:20
	 */
	public Holiday getHolidayByDate(Date date){
		String dateStr = DateUtil.DateToString(date, DateStyle.YYYY_MM_DD);
		String sql = "SELECT * FROM t_holiday h WHERE h.holiday = Date(?)";
		return dao.findFirst(sql,dateStr);
	}
	
	/**
	 * 判断日期是否为节假日
	 * @param date
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月15日上午9:29:28
	 */
	public boolean isHoliday(Date date){
		Holiday holiday = getHolidayByDate(date);
		if(null != holiday){
			return true;
		}
		return false;
	}
	
}
