package com.buy.model.store;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 仓库员工pos操作记录
 */
public class StorePosRecord extends Model<StorePosRecord> {

	private static final long serialVersionUID = 1L;

	public static final StorePosRecord dao = new StorePosRecord();
	
	/**
	 * 操作类型-注册
	 */
	public static final int TYPE_REGIST = 1;
	/**
	 * 操作类型-充值
	 */
	public static final int TYPE_RECHARGE = 2;
	/**
	 * 操作类型-新增线下订单
	 */
	public static final int TYPE_ADD_ORDER = 3;
	
	/**
	 * 获取备注
	 * @param store
	 * @param operationType
	 * @param operator
	 * @param mobile
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年1月29日上午11:42:37
	 */
	public String getRemark(Store store,int operationType,String operator,String mobile){
		String storeName = store.getStr("name");
		int type = store.getLong("type").intValue();
		String storeType = "仓库";
		if(1==type){
			storeType = "云店";
		}
		String remark = "";
		switch(operationType){
			case StorePosRecord.TYPE_REGIST:
				remark = "由"+storeType+"【"+storeName+"】 ，操作员 "+operator+" 协助会员"+mobile+"进行注册";
				break;
			case StorePosRecord.TYPE_RECHARGE:
				remark = "由"+storeType+"【"+storeName+"】 ，操作员 "+operator+" 协助会员"+mobile+"进行充值";
				break;
		}
		return remark;
	}

	/**
	 * 保存操作记录
	 * @param storeNo
	 * @param operationType
	 * @param operator
	 * @param remark
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年1月29日下午1:11:21
	 */
	public boolean save(String storeNo,int operationType,String operator,String remark){
		return new StorePosRecord()
	    .set("store_no", storeNo)
	    .set("type", operationType)
	    .set("operator", operator)
	    .set("remark", remark)
	    .set("create_time", new Date())
	    .save();
	}
	
}
