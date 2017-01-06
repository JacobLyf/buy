package com.buy.model.productApply;

import com.jfinal.plugin.activerecord.Model;

public class ShopSkuUpdateApply extends Model<ShopSkuUpdateApply>{



	/**
	 * 审核状态：未审核
	 */
	public final static int AUDIT_STATUS_UNCHECK = 0;
	/**
	 * 审核状态：审核通过
	 */
	public final static int AUDIT_STATUS_SUCCESS = 1;
	/**
	 * 审核状态：未审核失败
	 */
	public final static int AUDIT_STATUS_FAILEE = 2;

	

	private static final long serialVersionUID = 1L;
	public static final ShopSkuUpdateApply dao = new ShopSkuUpdateApply();

	
	

}
