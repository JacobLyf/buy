package com.buy.model.user;

import com.jfinal.plugin.activerecord.Model;

public class UserStockRecord extends Model<UserStockRecord> {
	
	private static final long serialVersionUID = 1L;
	public static final UserStockRecord dao = new UserStockRecord();
	
	public static final String REMARK = "申领认股凭证";
	
}
