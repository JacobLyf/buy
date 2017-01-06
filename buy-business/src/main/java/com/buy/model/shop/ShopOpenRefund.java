package com.buy.model.shop;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ShopOpenRefund extends Model<ShopOpenRefund> {

	private static final long serialVersionUID = 1L;
	public static final ShopOpenRefund dao = new ShopOpenRefund();
	
	/** 审核状态 - 未审核 **/
	public static final int AUDIT_WAIT = 0;
	/** 审核状态 - 审核成功 **/
	public static final int AUDIT_PASS = 1;
	
	public ShopOpenRefund getForLock(int id) {
		return ShopOpenRefund.dao.findFirst("SELECT * FROM t_open_shop_refund WHERE id = ? FOR UPDATE", id);
	}
	
	public Integer getAuditStatusByApplyId(int applyId, int openChannel, String shopId) {
		StringBuffer sql = new StringBuffer("SELECT a.audit_status FROM t_open_shop_refund a")
			.append(" WHERE a.open_id = ? ")
			.append(" AND a.open_channel = ? ")
			.append(" AND a.shop_id = ? ")
			.append(" ORDER BY a.id DESC ");
		return Db.queryInt(sql.toString(), applyId, openChannel, shopId);
	}
	
	public Record checkRefundSuccess(String shopId) {
		Integer channel = Db.queryInt("SELECT open_channel FROM t_shop WHERE id = ? AND status = ?", shopId, Shop.STATUS_REFUND);
		if (StringUtil.isNull(channel))
			return null;
		
		Record result = new Record();
		StringBuffer sql = new StringBuffer(" SELECT a.audit_time refundTime, a.cash prepayCharge FROM t_open_shop_refund a ");
		if (Shop.OPEN_CHANNEL_SELF == channel) {
			sql
				.append(" LEFT JOIN t_shop_apply b ON b.id = a.open_id ")
				.append(" WHERE a.open_channel = ? ")
				.append(" AND b.shop_id = ? ")
				.append(" ORDER BY b.id DESC ");
			result = Db.findFirst(sql.toString(), Shop.OPEN_CHANNEL_SELF, shopId);
		} else if (Shop.OPEN_CHANNEL_TURNOUT == channel) {
			sql.append(" LEFT JOIN t_shop_certification_record b ON b.id = a.open_id ")
			.append(" AND a.open_channel = ? ")
			.append(" AND b.shop_id = ? ")
			.append(" AND b.type = ? ")
			.append(" ORDER BY b.id DESC ");
			result = Db.findFirst(sql.toString(), Shop.OPEN_CHANNEL_TURNOUT, shopId, ShopCertification.TYPE_ACTIVATION);
		}
		
		return result;
	}

}
