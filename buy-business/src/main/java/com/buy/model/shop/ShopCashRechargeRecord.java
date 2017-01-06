package com.buy.model.shop;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class ShopCashRechargeRecord extends Model<ShopCashRechargeRecord> {

	private static final long serialVersionUID = 1L;
	public static final ShopCashRechargeRecord dao = new ShopCashRechargeRecord();
	
	/**
	 * 支付状态：待支付
	 */
	public static final int PAY_STATUS_WAIT = 0;
	
	/**
	 * 支付状态：已支付
	 */
	public static final int PAY_STATUS_PASS = 1;
	
	/**
	 * 支付状态：取消支付
	 */
	public static final int PAY_STATUS_UNPASS = 2;
	
	
	
	
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
	
	
	
	/**
	 * 付款方式：汇款
	 */
	public static final int PAY_TYPE_REMIT = 1;
	/**
	 * 付款方式：在线支付
	 */
	public static final int PAY_TYPE_ONLINE = 2;
	
	
	
	/**
	 * 编号前缀
	 */
	public static final String PREFIX_NO = "MCS";
	
	
	/**
	 * 通过编号获取记录
	 * @param no
	 * @return
	 * @author huangzq
	 */
	public ShopCashRechargeRecord getByNo(String no){
		return dao.findFirst("select * from t_shop_cash_recharge_record where no = ?",no);
	}
	
	/**
	 * 根据ID查找用户ID
	 * @param id
	 * @return
	 */
	public String getUserIdById(int id) {
		return Db.queryStr("SELECT shop_id from t_shop_cash_recharge_record WHERE id = ?", id);
	}

}
