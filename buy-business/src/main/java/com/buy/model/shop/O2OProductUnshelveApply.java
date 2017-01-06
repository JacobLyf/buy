package com.buy.model.shop;

import java.util.Date;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class O2OProductUnshelveApply extends Model<O2OProductUnshelveApply> {

	private static final long serialVersionUID = -6609711631695314027L;
	
	public final static O2OProductUnshelveApply dao = new O2OProductUnshelveApply();
	
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
	 * O2O完成状态：未完成
	 */
	public static final int O2O_STATUS_UNFINISHED = 0;
	
	/**
	 * O2O完成状态：已完成
	 */
	public static final int O2O_STATUS_FINISHED = 1;
	
	/**
	 * O2O云店商品下架申请.
	 * 
	 * @author Chengyb
	 */
	public void quit(String userId, String skuCode, String storeNo, int status) {
		if(!StringUtil.isBlank(skuCode) && !StringUtil.isBlank(storeNo)) {
			
			// 属性值.
			String propertyValuesSql = "SELECT product_id, property_decs FROM `t_pro_sku` where code = ?";
			Record record = Db.findFirst(propertyValuesSql, skuCode);
			
			// 属性名称.
			String sql = "SELECT GROUP_CONCAT(DISTINCT s.propertyName ORDER BY s.propertyId) AS propertyNames FROM v_com_sku_property s WHERE s.productId = ? GROUP BY s.productId";

			// 商品Id.
			Integer productId = null;
			
			// 属性描述.
			String string = "";
			
			if(null != record) {
				productId = record.getInt("product_id");
				
				// 属性值.
				String propertyValues = record.getStr("property_decs");
				
				String propertyNames = Db.queryFirst(sql, productId);
				
				if(null != propertyNames && null != propertyValues) {
					String[] a = propertyNames.split(",");
					String[] b = propertyValues.split(",");
					
					for (int i = 0; i < a.length - 1; i++) {
						string += a[i] + ":" + b[i] + ",";
					}
					string += a[a.length - 1] + ":" + b[a.length - 1];
				}
			} else {
				String productIdSql = "SELECT product_id FROM `v_com_sku` where code = ?";

				Record productIdRecord = Db.findFirst(productIdSql, skuCode);
				
				productId = productIdRecord.getInt("product_id");
			}
			
			// 添加退出申请记录.
			O2OProductUnshelveApply quit = new O2OProductUnshelveApply();
			quit.set("product_id", productId);
			quit.set("sku_code", skuCode);
			quit.set("store_no", storeNo);
			quit.set("perperty_desc", string);
			quit.set("status", status);
			quit.set("applicant_id", userId);
			Date date = new Date();
			quit.set("apply_time", date);
			if(status == O2OProductUnshelveApply.AUDIT_STATUS_PASS) {
				quit.set("audit_time", date);
			}
			quit.save();
		}
	}
	
	/**
	 * O2O云店商品下架申请.
	 */
	public void quit(String userId, String skuCode, String storeNo, int status, String reason) {
		if(!StringUtil.isBlank(skuCode) && !StringUtil.isBlank(storeNo)) {
			
			// 属性值.
			String propertyValuesSql = "SELECT product_id, property_decs FROM `t_pro_sku` where code = ?";
			Record record = Db.findFirst(propertyValuesSql, skuCode);
			
			// 属性名称.
			String sql = "SELECT GROUP_CONCAT(DISTINCT s.propertyName ORDER BY s.propertyId) AS propertyNames FROM v_com_sku_property s WHERE s.productId = ? GROUP BY s.productId";

			// 商品Id.
			Integer productId = null;
			
			// 属性描述.
			String string = "";
			
			if(null != record) {
				productId = record.getInt("product_id");
				
				// 属性值.
				String propertyValues = record.getStr("property_decs");
				
				String propertyNames = Db.queryFirst(sql, productId);
				
				if(null != propertyNames && null != propertyValues) {
					String[] a = propertyNames.split(",");
					String[] b = propertyValues.split(",");
					
					for (int i = 0; i < a.length - 1; i++) {
						string += a[i] + ":" + b[i] + ",";
					}
					string += a[a.length - 1] + ":" + b[a.length - 1];
				}
			} else {
				String productIdSql = "SELECT product_id FROM `v_com_sku` where code = ?";

				Record productIdRecord = Db.findFirst(productIdSql, skuCode);
				
				productId = productIdRecord.getInt("product_id");
			}
			
			// 添加退出申请记录.
			Date now = new Date();
			O2OProductUnshelveApply quit = new O2OProductUnshelveApply()
				.set("product_id",		productId)
				.set("sku_code",		skuCode)
				.set("store_no",		storeNo)
				.set("perperty_desc",	string)
				.set("status", 			status)
				.set("applicant_id", 	userId)
				.set("reason", 			reason)
				.set("apply_time", 		now);
			if(status == O2OProductUnshelveApply.AUDIT_STATUS_PASS)
				quit.set("audit_time",	now);
			quit.save();
		}
	}

}