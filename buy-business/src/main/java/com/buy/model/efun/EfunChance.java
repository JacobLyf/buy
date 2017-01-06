package com.buy.model.efun;

import com.buy.common.BaseConstants;
import com.jfinal.plugin.activerecord.Model;

public class EfunChance extends Model<EfunChance>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final EfunChance dao = new EfunChance();
	
	public boolean updateNewestStatus(){
		String sql = " SELECT * FROM t_efun_chance ec ORDER BY ec.create_time DESC ";
		EfunChance newest = dao.findFirst(sql);
		newest.set("status", BaseConstants.NO);
		return newest.update();
	}
	
	public int getNewestChance(){
		String sql = " SELECT * FROM t_efun_chance ec ORDER BY ec.create_time DESC ";
		EfunChance newest = dao.findFirst(sql);
		return newest.getInt("chance");
	}
	
}
