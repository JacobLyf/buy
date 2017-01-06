package com.buy.model.shop;

import com.buy.model.trade.Trade;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 店铺申请
 * @author Sylveon
 */
public class ShopApply extends Model<ShopApply> {
	
	/** 申请中 **/
	public final static int STATUS_APPLYING = 0;
	/** 申请通过 **/
	public final static int STATUS_APPLY_SUCCESS = 1;
	/** 申请失败 */
	public final static int STATUS_APPLY_FAIL = 2;
	
	/** 支付提交 */
	public final static int PAY_STATUS_SUBMIT = 0;
	/** 支付成功 */
	public final static int PAY_STATUS_SUCCESS = 1;
	/** 支付失败 */
	public final static int PAY_STATUS_FAIL = 2;

	public ShopApply getByIdAndLock(int shopApplyId) {
		return ShopApply.dao.findFirst("SELECT * FROM t_shop_apply WHERE id = ? FOR UPDATE", shopApplyId);
	}
	
	public boolean checkAddShopApply(String realName, String idcard, String mobile) {
		StringBuffer sql = new StringBuffer(" SELECT audit_status FROM t_shop_apply ");
		sql.append(" WHERE real_name = ? ");
		sql.append(" AND idcard = ? ");
		sql.append(" AND mobile = ? ");
		sql.append(" AND audit_status BETWEEN ? AND ? ");
		sql.append(" AND pay_status = ? ");
		sql.append(" ORDER BY create_time DESC ");
		
		Integer status = Db.queryInt(
			sql.toString(),
			realName,
			idcard,
			mobile,
			ShopApply.PAY_STATUS_SUBMIT,
			ShopApply.PAY_STATUS_SUCCESS,
			Trade.STATUS_SUCCESS
		);
		if (null == status)
			return true;
		if (STATUS_APPLYING == status)
			return false;
		return true;
	}
	
	public String getShopId(int applyId) {
		return Db.queryStr("SELECT shop_id FROM t_shop_apply WHERE id = ?", applyId);
	}
	
	private static final long serialVersionUID = 1L;
	public static ShopApply dao = new ShopApply();

}
