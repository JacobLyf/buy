package com.buy.model.store;

import java.util.Date;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 进出库记录
 */
public class StoreInOutRecord extends Model<StoreInOutRecord> {

	private static final long serialVersionUID = 1L;

	public static final StoreInOutRecord dao = new StoreInOutRecord();
	

	/**
	 * 操作类型-sku入库
	 */
	public static final int TYPE_SKU_IN = 1;
	/**
	 * 操作类型-sku出库
	 */
	public static final int TYPE_SKU_OUT = 2;
	/**
	 * 操作类型-盘点
	 */
	public static final int TYPE_SKU_SET_COUNT = 3;
	/**
	 * 操作类型-门店下单
	 */
	public static final int TYPE_O2O_ORDER = 4;
	/**
	 * 操作类型-线上加库存
	 */
	public static final int TYPE_ONLINE_ORDER_IN = 5;
	
	/**
	 * 操作类型-线上减库存
	 */
	public static final int TYPE_ONLINE_ORDER_OUT = 6;
	
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
	public String getRemark(String storeNo,int operationType,String operator){
		Store store = Store.dao.getStoreByNo(storeNo);
		String storeName = store.getStr("name");
		int type = store.getLong("type").intValue();
		String storeType = "仓库";
		if(1==type){
			storeType = "云店";
		}
		String remark = "";
		switch(operationType){
		
			case StoreInOutRecord.TYPE_SKU_IN:
				remark = "由"+storeType+"【"+storeName+"】 ，操作员 "+operator+" 进行SKU入库";
				break;
			case StoreInOutRecord.TYPE_SKU_OUT:
				remark = "由"+storeType+"【"+storeName+"】 ，操作员 "+operator+" 进行SKU出库";
				break;
			case StoreInOutRecord.TYPE_SKU_SET_COUNT:
				remark = "由"+storeType+"【"+storeName+"】 ，操作员 "+operator+" 进行SKU盘点";
				break;
			case StoreInOutRecord.TYPE_O2O_ORDER:
				remark = "由"+storeType+"【"+storeName+"】 ，操作员 "+operator+" 门店下单扣库存";
				break;
			case StoreInOutRecord.TYPE_ONLINE_ORDER_IN:
				remark = "线上加库存";
				break;
			case StoreInOutRecord.TYPE_ONLINE_ORDER_OUT:
				remark = "线上扣库存";
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
	public boolean save(String storeNo,String skuCode,int count,int operationType,String operator,String posOrderNo){
		String remark = this.getRemark(storeNo, operationType, operator);
		if(StringUtil.notBlank(posOrderNo)){
			remark+=",POS零售单号为:"+posOrderNo;
		}
		return new StoreInOutRecord()
	    .set("store_no", storeNo)
	    .set("sku_code", skuCode)
	    .set("count", count)
	    .set("type", operationType)
	    .set("operator", operator)
	    .set("remark", remark)
	    .set("create_time", new Date())
	    .save();
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
	public boolean save(String storeNo,String skuCode,int count,int operationType,String operator){
		return this.save(storeNo, skuCode, count, operationType, operator, null);
	}
}
