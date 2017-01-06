package com.buy.model.activity;

import java.util.List;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 活动
 * @author huangzq
 *
 */
public class Activity extends Model<Activity>{
	
	/**
	 * 活动时间状态：未开始
	 */
	public final static int TIME_STATUS_NO_BEGIN = 1;
	/**
	 * 活动时间状态：进行中
	 */
	public final static int TIME_STATUS_UNDERWAY = 2;
	/**
	 * 活动时间状态：已结束
	 */
	public final static int TIME_STATUS_OVER = 3;

	/**
	 * 里约奥运会活动开始时间
	 */
	public static final String ORI_START_TIME = "2016-08-05 00:00:00";
	/**
	 * 里约奥运会活动结束时间
	 */
	public static final String ORI_END_TIME = "2016-08-23 23:59:59";
	
	/**
	 * iPhone7活动开始时间
	 */
	public static final String IPHONE7_START_TIME = "2016-09-15 00:00:00";
	
	/**
	 * iPhone7活动结束时间
	 */
	public static final String IPHONE7_END_TIME = "2016-10-05 23:59:59";
	
	/**
	 * 呼朋唤友帮讨钱开始时间.
	 */
	public static final String BARGAIN_START_TIME = "2016-10-05 23:59:59";
	
	/**
	 * 呼朋唤友帮讨钱结束时间.
	 */
	public static final String BARGAIN_END_TIME = "2016-11-30 23:59:59";

	private static final long serialVersionUID = 1L;
	
	public static final Activity dao = new Activity();
	
	//去除没有被调用的方法--chenhg
}
