package com.buy.model.productApply;

import com.jfinal.plugin.activerecord.Model;

public class PublicSkuUpdateApply extends Model<PublicSkuUpdateApply>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5182888263767206837L;

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
	
	public static final PublicSkuUpdateApply dao = new PublicSkuUpdateApply();
}
