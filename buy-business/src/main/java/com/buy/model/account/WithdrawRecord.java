package com.buy.model.account;

import com.jfinal.plugin.activerecord.Model;

public class WithdrawRecord extends Model<WithdrawRecord>{
	
	/**
	 * 交易状态：进行中
	 */
	public static final int STATUS_ON_WAY = 1;
	/**
	 * 交易状态：成功
	 */
	public static final int STATUS_SUCCESS = 2;
	/**
	 * 交易状态：失败
	 */
	public static final int STATUS_FAIL = 3;
	/**
	 * 用户类型：会员
	 */
	public static final int USER_TYPE_USER = 1;
	/**
	 * 用户类型：店铺
	 */
	public static final int USER_TYPE_SHOP = 2;
	/**
	 * 用户类型：代理
	 */
	public static final int USER_TYPE_AGENT = 3;
	
	/**
	 * 用户类型：供货
	 */
	public static final int USER_TYPE_SUPPLIER = 4;

	/**
	 * 提现记录
	 */
	private static final long serialVersionUID = 1L;
	
	public static final WithdrawRecord dao = new WithdrawRecord();

	
	
}
