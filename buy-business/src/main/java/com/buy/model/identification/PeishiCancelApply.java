package com.buy.model.identification;

import com.jfinal.plugin.activerecord.Model;

public class PeishiCancelApply extends Model<PeishiCancelApply>{
	

	
	
	/**
	 * 审核状态：未审核
	 */
	public static final int AUDIT_STATUS_UNCHECK = 0;
	/**
	 * 审核状态：审核通过
	 */
	public static final int AUDIT_STATUS_SUCCESS = 1;
	/**
	 * 审核状态：审核失败
	 */
	public static final int AUDIT_STATUS_FAIL = 2;
	
	
	private static final long serialVersionUID = 1L;
	
	public final static PeishiCancelApply dao = new PeishiCancelApply();
	
	
	
}
