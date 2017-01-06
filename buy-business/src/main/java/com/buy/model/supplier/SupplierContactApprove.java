package com.buy.model.supplier;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class SupplierContactApprove extends Model<SupplierContactApprove>{
	
	/** 审批状态 - 未审批 */
	public static final int APPROVE_STATUS_NOT = 0;
	/** 审批状态 - 成功 */
	public static final int APPROVE_STATUS_SUCCESS = 1;
	/** 审批状态 - 失败 */
	public static final int APPROVE_STATUS_FAIL = 2;
	
	private static final long serialVersionUID = 1L;
	
	public final static SupplierContactApprove dao = new SupplierContactApprove();
	
	/**
	 * 获取供货商修改个人资料申请状态 
	 */
	public Integer getStatusBySupplierId(String supplierId) {
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" approve_status status ");
		sql.append(" FROM t_supplier_contact_approve ");
		sql.append(" WHERE supplier_id = ? ");
		sql.append(" ORDER BY create_time DESC ");
		return Db.queryInt(sql.toString(), supplierId);
	}
	
}
