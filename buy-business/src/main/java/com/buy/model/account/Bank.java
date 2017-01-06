package com.buy.model.account;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class Bank extends Model<Bank>{

	/**
	 * 银行
	 */
	private static final long serialVersionUID = 1L;
	
	public static final Bank dao = new Bank();

	/**
	 * 获得所有银行列表
	 */
	public List<Record> findBankList(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	id bankId, ");
		sql.append(" 	`name` bankName ");
		sql.append(" FROM ");
		sql.append(" 	t_bank ");
		sql.append(" ORDER BY ");
		sql.append(" 	id ");
		return Db.find(sql.toString());
	}
}
