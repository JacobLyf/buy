package com.buy.model.account;

import com.jfinal.plugin.activerecord.Model;

public class BankAccountApprove extends Model<BankAccountApprove> {
	
	/** 审批状态 - 未审批 */
	public static final int APPROVE_STATUS_NOT = 0;
	/** 审批状态 - 成功 */
	public static final int APPROVE_STATUS_SUCCESS = 1;
	/** 审批状态 - 失败 */
	public static final int APPROVE_STATUS_FAIL = 2;
	
	/**
	 * 用户类型：会员
	 */
	public static final int TYPE_USER = 1;
	
	/**
	 * 用户类型：店铺
	 */
	public static final int TYPE_SHOP = 2;
	
	/**
	 * 用户类型：代理商
	 */
	public static final int TYPE_AGENT = 3;
	
	/**
	 * 用户类型：供货商
	 */
	public static final int TYPE_SUPPLIER = 4;
	
	/**
	 * 银行卡
	 */
	private static final long serialVersionUID = 1L;
	
	public static final BankAccountApprove dao = new BankAccountApprove();
	
}
