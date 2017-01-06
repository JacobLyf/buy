package com.buy.model.shop;

import com.jfinal.plugin.activerecord.Model;

public class O2OProductApply extends Model<O2OProductApply> {
	
	private static final long serialVersionUID = 1L;
	public final static O2OProductApply dao = new O2OProductApply();
	
	/**
	 * 流水号前缀
	 */
	public final static String NO_PREFIX = "SKD";
	/**
	 * 审核状态：待审核
	 */
	public static final int STATUS_WAIT = 0;
	
	/**
	 * 审核状态：通过
	 */
	public static final int STATUS_PASS = 1;
	
	/**
	 * 审核状态：未通过
	 */
	public static final int STATUS_UNPASS = 2;
	/**
	 * 进驻云店完成状态：未完成  o2o_status
	 */
	public static final int O2O_STATUS_NO = 0;
	
	/**
	 *  进驻云店完成状态：已完成
	 */
	public static final int O2O_STATUS_YES = 1;
}
