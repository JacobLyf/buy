package com.buy.model.user;

import com.jfinal.plugin.activerecord.Model;

public class UserStockAccount extends Model<UserStockAccount> {

	private static final long serialVersionUID = -3867789051307336687L;

	public static final UserStockAccount dao = new UserStockAccount();
	
	/**
	 * 一元股(改逻辑，可以增加对应的股权类型id)
	 */
	public static final int STOCK_TYPE_ONE = 1;
	/**
	 * 原始股
	 */
	public static final int TYPE_ORIGINAL = 1;
	/**
	 * 增值股
	 */
	public static final int TYPE_ADD = 2;
	
	/**
	 * 并发插入分割符号
	 */
	public static final String SPLIT = "S";
	/**
	 * 获取股权账号同时锁住该账号
	 * @param userId
	 * @param stockType
	 * @param type
	 * @return
	 * @author chenhg
	 * 2016年8月25日 下午3:44:15
	 */
	public UserStockAccount getStockAccountForUpdate(String userId ,int stockType, int type){
		UserStockAccount account =  UserStockAccount.dao.findFirst("select a.id from t_user_stock_account a where a.user_id = ? and a.stock_type_id = ? AND a.type = ?",new Object[]{userId,stockType,type});
		if(account == null){
			return null;
		}
		Integer id = account.getInt("id");
		return UserStockAccount.dao.findFirst("select * from t_user_stock_account a where a.id = ? for update",new Object[]{id});
	}
	
	/**
	 * 获得股权可转让股权数
	 * @param userId
	 * @return
	 * @author chenhg
	 * 2016年8月26日 下午1:17:29
	 */
	public int getStockAccountNum(String userId){
		UserStockAccount account =  UserStockAccount.dao.findFirst("select no_transferd_count from t_user_stock_account a where a.user_id = ? ", userId);
		if(account == null){
			return 0;
		}else{
			return account.getInt("no_transferd_count");
		}
	}
	
	/**
	 * 获得已经转让的股权数
	 * @param userId
	 * @return
	 * @author chenhg
	 * 2016年8月26日 下午6:10:16
	 */
	public int getTransferdCount(String userId){
		UserStockAccount account =  UserStockAccount.dao.findFirst("select transfered_count from t_user_stock_account a where a.user_id = ? ", userId);
		if(account == null){
			return 0;
		}else{
			return account.getInt("transfered_count");
		}
	}
	
	
}
