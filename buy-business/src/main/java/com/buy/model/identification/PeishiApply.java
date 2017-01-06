package com.buy.model.identification;

import com.jfinal.plugin.activerecord.Model;

public class PeishiApply extends Model<PeishiApply>{

	
	/**
	 * 状态：提交
	 */
	public static final int STATUS_COMMIT = 0;
	/**
	 * 状态：支付成功
	 */
	public static final int STATUS_SUCCESS = 1;
	/**
	 * 状态：支付失败
	 */
	public static final int STATUS_FAIL = 2;
	
	
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
	
	
	/**
	 * 付款方式：余额支付
	 */
	public static final int PAY_TYPE_CASH = 1;
	/**
	 * 付款方式：在线支付
	 */
	public static final int PAY_TYPE_ONLINE = 2;
	
	
	/**
	 * 编号前缀
	 */
	public static final String NO_PREFIX = "PS";
	
	private static final long serialVersionUID = 1L;
	
	public final static PeishiApply dao = new PeishiApply();
	
	
	
}
