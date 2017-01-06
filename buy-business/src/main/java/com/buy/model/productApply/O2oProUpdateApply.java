package com.buy.model.productApply;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class O2oProUpdateApply extends Model<O2oProUpdateApply>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * 审核状态：未审核
	 */
	public final static int AUDIT_STATUS_UNCHECK = 0;
	/**
	 * 审核状态：商品资料修改中(审核通过）
	 */
	public final static int AUDIT_STATUS_UPDATING = 1;
	/**
	 * 审核状态：更改完成
	 */
	public final static int AUDIT_STATUS_FINISH = 2;
	/**
	 * 审核状态：审核失败
	 */
	public final static int AUDIT_STATUS_FAIL = 3;
	
	public static final O2oProUpdateApply dao = new O2oProUpdateApply();
	
	
	/**
	 * 查看更新O2O商品明细是否已完成
	 * @param applyId
	 * @return
	 * @author huangzq
	 */
	public boolean isFinish(Integer applyId){
		String sql = "select count(*) from t_o2o_pro_update_detail detail where detail.finish_status = ? and detail.apply_id = ?";
		long count = Db.queryLong(sql,O2oProUpdateDetail.FINISH_STATUS_NO,applyId);
		if(count>0){
			return false;
		}
		return true;
	}

	
	

}
