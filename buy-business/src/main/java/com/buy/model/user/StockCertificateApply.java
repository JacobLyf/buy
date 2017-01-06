package com.buy.model.user;

import com.jfinal.plugin.activerecord.Model;

public class StockCertificateApply extends Model<StockCertificateApply> {

	private static final long serialVersionUID = 1L;
	public static final StockCertificateApply dao = new StockCertificateApply();
	
	/**
	 * 审核状态：未审核
	 */
	public static final int STATUS_WAIT = 0;
	
	/**
	 * 审核状态：审核通过
	 */
	public static final int STATUS_PASS = 1;
	/**
	 * 审核状态：审核不通过
	 */
	public static final int STATUS_UNPASS = 2;
	

}
