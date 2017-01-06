package com.buy.model.productApply;

import java.util.Date;
import java.util.List;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class O2oProUpdateDetail extends Model<O2oProUpdateDetail>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 完成状态：未完成
	 */
	public static final  int FINISH_STATUS_NO = 0;
	/**
	 * 完成状态：已完成
	 */
	public static final int FINISH_STATUS_OK = 1;
	
	public static final O2oProUpdateDetail dao = new O2oProUpdateDetail();
	/**
	 * 添加申请详情
	 * @param skuCode
	 * @author huangzq
	 */
	public void addDetail(Integer applyId,String skuCode){
		String sql  = "select store_no from t_o2o_sku_map where sku_code = ?";
		List<String> storeNos = Db.query(sql,skuCode);
		Date now = new Date();
		if(StringUtil.notNull(storeNos)){
			for(String storeNo : storeNos){
				O2oProUpdateDetail detail = new O2oProUpdateDetail();
				detail.set("apply_id", applyId);
				detail.set("sku_code", skuCode);
				detail.set("store_no", storeNo);
				detail.set("finish_time", now);
				detail.set("finish_status", FINISH_STATUS_NO);
				detail.save();
			}
		}
	}
}
