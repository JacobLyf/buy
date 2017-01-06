package com.buy.model.productApply;

import com.jfinal.plugin.activerecord.Model;

public class PublicProductApply extends Model<PublicProductApply>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7969231085036491900L;
	
	public static final PublicProductApply dao = new PublicProductApply();
	
	/**
	 * 审核状态：未审核
	 */
	public final static int AUDIT_STATUS_UNCHECK = 0;
	/**
	 * 审核状态：审核通过
	 */
	public final static int AUDIT_STATUS_SUCCESS = 1;
	/**
	 * 审核状态：审核不通过
	 */
	public final static int AUDIT_STATUS_FAILEE = 2;
	
	public PublicProductApply findByProductId (Integer productId) {
		String sql = "select * from t_public_pro_apply where product_id = " + productId;
		return PublicProductApply.dao.findFirst(sql);
	}
}
