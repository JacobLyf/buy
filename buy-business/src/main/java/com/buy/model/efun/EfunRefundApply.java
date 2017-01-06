package com.buy.model.efun;

import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.Model;

/**
 * 幸运一折购退款申请
 */
public class EfunRefundApply extends Model<EfunRefundApply>{
	
	
	private static final long serialVersionUID = 1L;
	private  Logger L = Logger.getLogger(EfunRefundApply.class);
	
	public static final EfunRefundApply dao = new EfunRefundApply();
	
	
	/**
	 * 状态：未退款
	 */
	public static final int STATUS_UN_REFUND = 0;
	/**
	 * 状态：已退款
	 */
	public static final int STATUS_REFUNDED = 1;
	
	
}