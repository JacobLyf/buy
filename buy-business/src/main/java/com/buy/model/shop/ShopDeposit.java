package com.buy.model.shop;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 保证金
 * @author Administrator
 *
 */
public class ShopDeposit extends Model<ShopDeposit> {
	/**
	 * 审核状态：未审核
	 */
	public static final int AUDIT_STATUS_UNCHECKED = 0; 
	/**
	 * 审核状态：审核通过
	 */
	public static final int AUDIT_STATUS_SUCCESS = 1; 
	/**
	 * 审核状态：审核不通过
	 */
	public static final int AUDIT_STATUS_FAIL = 2;
	
	
	
	/**
	 * 类型：缴纳
	 */
	public static final int TYPE_PAYMENT = 1;
	/**
	 * 类型：申退
	 */
	public static final int TYPE_BACK = 2;
	
	
	/**
	 * 付款方式：余额支付
	 */
	public static final int PAY_TYPE_CASH= 1; 
	/**
	 * 付款方式：在线支付
	 */
	public static final int PAY_TYPE_ONLINE= 2; 
	
	private static final long serialVersionUID = 1L;
	public static ShopDeposit dao = new ShopDeposit();

	/**
	 * 通过ID查询店铺ID
	 * @param id
	 * @return
	 */
	public String getShopIdById(int id) {
		return Db.queryStr("SELECT shop_id FROM t_shop_deposit WHERE id = ?", id);
	}
	
	/**
	 * 查询店铺保证金记录（锁行）
	 * @param id
	 * @return
	 */
	public ShopDeposit getByIdAndLock(int id) {
		return ShopDeposit.dao.findFirst("SELECT * FROM t_shop_deposit WHERE id = ? FOR UPDATE", id);
	}
	
}