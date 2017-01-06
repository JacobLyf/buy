package com.buy.model.user;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 会员现金充值
 */
public class UserCashRechargeRecord extends Model<UserCashRechargeRecord> {
	
	/**
	 * 支付方式 - 汇款
	 */
	public static final int PAY_TYPE_REMITTANCE = 1;
	/**
	 * 支付方式 - 在线
	 */
	public static final int PAY_TYPE_ONLINE = 2;
	/**
	 * 支付方式-门店付款 
	 */
	public static final int PAY_TYPE_STORE = 3;
	
	/**
	 * 支付状态 - 待支付
	 */
	public static final int PAY_STATUS_WAITPAY = 0;
	/**
	 * 支付状态 - 已支付
	 */
	public static final int PAY_STATUS_PAIED = 1;
	/**
	 * 支付状态 - 取消
	 */
	public static final int PAY_STATUS_CANCEL = 2;
	
	
	/**
	 * 审核状态：待审核
	 */
	public static final int AUDIT_STATUS_WAIT = 0;
	
	/**
	 * 审核状态：通过
	 */
	public static final int AUDIT_STATUS_PASS = 1;
	
	/**
	 * 审核状态：未通过
	 */
	public static final int AUDIT_STATUS_UNPASS = 2;
	

	private static final long serialVersionUID = 1L;
	
	/**
	 * 编号前缀
	 */
	public static final String PREFIX_NO = "MCS";
	
	public static final UserCashRechargeRecord dao = new UserCashRechargeRecord();
	
	
	
	/**
	 * 通过编号获取记录
	 * @param no
	 * @return
	 * @author huangzq
	 */
	public UserCashRechargeRecord getByNo(String no){
		return dao.findFirst("select * from t_user_cash_recharge_record where no = ?",no);
	}
	
	/**
	 * 根据ID查找用户ID
	 * @param id
	 * @return
	 */
	public String getUserIdById(int id) {
		return Db.queryStr("SELECT user_id from t_user_cash_recharge_record WHERE id = ?", id);
	}

}
