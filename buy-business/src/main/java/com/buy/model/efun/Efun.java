package com.buy.model.efun;

import java.util.Date;

import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.jfinal.plugin.activerecord.Model;

/**
 * 幸运一折购表（期次表）
 */
public class Efun extends Model<Efun>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final Efun dao = new Efun();
	
	/**
	 * 获取最新期次
	 * @return
	 * @author Jacob
	 * 2016年1月15日下午4:15:07
	 */
	public Integer getNewestEfunId(){
		return this.getNewestEfun().getInt("id");
	}
	
	/**
	 * 获取最新期次对象
	 * @return
	 * @author Jacob
	 * 2016年1月15日下午5:03:19
	 */
	public Efun getNewestEfun(){
		Date now = new Date();
		String s = DateUtil.DateToString(DateUtil.addMinute(now, 10), DateStyle.YYYY_MM_DD_HH_MM);
		String dateStr = s.substring(0, s.length()-1)+"0";
		String sql = " SELECT e.* FROM t_efun e WHERE e.lottery_time = '"+dateStr+"'";
		return dao.findFirst(sql);
	}
	
	/**
	 * 获取最新期次对象(锁行)
	 * @return
	 * @author Jacob
	 * 2016年1月15日下午5:03:19
	 */
	public Efun getNewestEfunForUpdate(){
		Date now = new Date();
		String s = DateUtil.DateToString(DateUtil.addMinute(now, 10), DateStyle.YYYY_MM_DD_HH_MM);
		String dateStr = s.substring(0, s.length()-1)+"0";
		String sql = " SELECT e.* FROM t_efun e WHERE e.lottery_time = '"+dateStr+"' for update ";
		return dao.findFirst(sql);
	}
	
	/**
	 * 获取上期期次对象
	 * @return
	 * @author Jacob
	 * 2016年1月22日上午11:33:35
	 */
	public Efun getPreEfun(){
		Date now = new Date();
		String s = DateUtil.DateToString(now, DateStyle.YYYY_MM_DD_HH_MM);
		String dateStr = s.substring(0, s.length()-1)+"0";
		String sql = "SELECT e.lottery_time, e.win_number, e.id FROM t_efun e WHERE e.lottery_time = '"+dateStr+"'";
		return dao.findFirst(sql);
	}
	
	/**
	 * 获取最新一期开奖时间
	 * @return
	 * @author Jacob
	 * 2016年1月22日上午11:54:23
	 */
	public Date getNewestLotteryTime(){
		return this.getNewestEfun().getDate("lottery_time");
	}
	
	/**
	 * 获取当前时间的幸运一折购
	 * @return
	 * @author Jacob
	 * 2016年4月16日上午10:16:55
	 */
	public Efun getEfunByCreateTime(Date date){
		String sql = " SELECT * FROM t_efun WHERE create_time = ? ";
		return dao.findFirst(sql, date);
	}
	
}
