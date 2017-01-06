package com.buy.model.im;

import java.util.Date;

import com.buy.model.account.Account;
import com.buy.model.user.User;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ImMerchant extends Model<ImMerchant>
{
	/** IM账号状态 - 未创建 **/
	public static final int STATUS_WITHOUT = 0;
	/** IM账号状态 - 已创建 **/
	public static final int STATUS_CREATIED = 1;
	/** IM账号状态 - 冻结 **/
	public static final int STATUS_FROZEN = 2;
	
	public void addByShop(String shopId, String shopName, String shopNo, Date createTime, int isSelf)
	{
		new ImMerchant()
			.set("merchant_id", 			shopId)
			.set("merchant_name", 			shopName)
			.set("merchant_no", 			shopNo)
			.set("is_belong_efun", 			isSelf)
			.set("merchant_create_time", 	createTime)
			.set("merchant_type", 			User.FRONT_USER_SHOP)
			.set("status",					ImMerchant.STATUS_WITHOUT)
			.save();
	}
	
	public void addBySupplier(String supplierId, String supplierName, String supplierNo, Date createTime, int isSelf)
	{
		
		new ImMerchant()
		.set("merchant_id", 			supplierId)
		.set("merchant_name", 			supplierName)
		.set("merchant_no", 			supplierNo)
		.set("is_belong_efun", 			isSelf)
		.set("merchant_create_time", 	createTime)
		.set("merchant_type", 			User.FRONT_USER_SUPPLIER)
		.set("status",					ImMerchant.STATUS_WITHOUT)
		.save();
	}
	
	public ImMerchant getForLock(int id) {
		return ImMerchant.dao.findFirst(
				"SELECT * FROM t_im_merchant WHERE id = ? FOR UPDATE",
				id
		);
	}
	
	public boolean isExistImAdmin(int id, String imAdmin)
	{
		long count = Db.queryLong("SELECT COUNT(1) FROM t_im_merchant WHERE id <> ? AND im_admin = ? AND status <> ?", id, imAdmin, ImMerchant.STATUS_FROZEN);
		return count > 0 ? true : false;
	}
	
	public boolean isExistImId(int id, int imId)
	{
		long count = Db.queryLong("SELECT COUNT(1) FROM t_im_merchant WHERE id <> ? AND im_id = ?", id, imId);
		return count > 0 ? true : false;
	}
	
	public boolean isExistIm(String merchantId, int merchantType)
	{
		long count = Db.queryLong(
				new StringBuffer(" SELECT COUNT(1) FROM t_im_merchant ")
					.append(" WHERE status = ? ")
					.append(" AND merchant_id = ? ")
					.append(" AND merchant_type = ? ")
					.toString(),
					
					STATUS_CREATIED, merchantId, merchantType
		);

		return count > 0 ? true : false;
	}
	
	/**
	 * 根据店主ID或供货商ID获取im_id
	 * @return
	 */
	public Integer getImId(Record r){
		String sql = "select im_id from t_im_merchant where status = ? and merchant_id = ? and merchant_type = ?";
		Integer merchantType = 0;
		String merchantId = "";
		if (StringUtil.notNull(r.getStr("shopId"))) {
			merchantType = Account.TYPE_SHOP;
			merchantId = r.getStr("shopId");
		} else if (StringUtil.notNull(r.getStr("supplierId"))) {
			merchantType = Account.TYPE_SUPPLIER;
			merchantId = r.getStr("supplierId");
		}
		return Db.queryInt(sql, ImMerchant.STATUS_CREATIED, merchantId, merchantType);
	}
	
	/**
	 * 根据商家编号获取im_id
	 * @param merchantNo 商家编号
	 * @return
	 */
	public Integer getImIdByMerchantNo(String merchantNo) {
		String sql = "select im_id from t_im_merchant where status = ? and merchant_no = ?";
		return Db.queryInt(sql, ImMerchant.STATUS_CREATIED, merchantNo);
	}
	
	private static final long serialVersionUID = 1L;
	public static final ImMerchant dao = new ImMerchant();

	/**
	 * 根据商品ID获取店铺ID和供货商ID
	 * @param proId
	 * @return
	 */
	public Record getShopAndSupplierId(Integer proId) {
		String sql = "select shop_id as shopId, supplier_id as supplierId from t_product where id = ?";
		return Db.findFirst(sql, proId);
	}

	/**
	 * 冻结店铺IM
	 * @param shopId
	 */
	public void updateByShopFrozen(String shopId) {
		Db.update("UPDATE t_im_merchant SET status = ? WHERE merchant_id = ? AND merchant_type = ?", STATUS_FROZEN, shopId, User.FRONT_USER_SHOP);

	}

}
