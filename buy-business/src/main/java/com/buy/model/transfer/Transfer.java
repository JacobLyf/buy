package com.buy.model.transfer;


import com.jfinal.plugin.activerecord.Model;

public class Transfer extends Model<Transfer> {

	private static final long serialVersionUID = 1L;

	public static final Transfer dao = new Transfer();
	
	
	/**
	 * 状态类型 - 调拨中
	 */
	public final int TYPE_DOING = 1;
	/**
	 * 状态类型- 已完成
	 */
	public final int TYPE_FINISH = 2;
	/**
	 * 状态类型 - 已取消
	 */
	public final int TYPE_CANCEL = 3;

	/**
	 * 审核状态 - 申请中
	 */
	public final int AUDIT_STATUS_APPLY = 1;
	/**
	 * 审核状态 - 审核成功
	 */
	public final int AUDIT_STATUS_SUCCESS  = 2;
	/**
	 * 审核状态 - 审核失败
	 */
	public final int AUDIT_STATUS_FAILURE = 3;

}
