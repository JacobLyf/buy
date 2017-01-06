package com.buy.service.pos.push;

import java.util.List;

import org.apache.log4j.Logger;

import com.buy.model.pos.PushPosRecord;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

//POS 推送订单库存信息
public class PushPosInventory {

	static Logger L = Logger.getLogger(PushPosInventory.class);
	
	private String orderId; //(t_order:id 或  t_efun_user_order:id)
	private boolean isEfun; //true:幸运一折购订单，false：普通、九折购订单

	//pos推送接口名称
	public final static String REQ_STORE_INVENTORY = "updateStore";

	/**
	 * 设置基本查询条件信息
	 * @param orderId
	 * @param isEfun
	 * @return
	 * @author chenhg
	 * 2016年11月24日 下午4:56:54
	 */
	public PushPosInventory setOrderId(String orderId, boolean isEfun) {
		this.orderId = orderId;
		this.isEfun = isEfun;
		return this;
	}

	/**
	 * 推送
	 * @author chenhg
	 * 2016年11月23日 下午4:26:52
	 */
	public void push() {
		// 云店库存变更
		L.info("云店库存变更推送,订单id:"+orderId +",是否一折购订单:" + isEfun);
		//由于第三方回调：扣库存事务还没提交就要调用此事件驱动，所以需要睡一下觉觉先~~
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			L.error("云店库存变更推送,订单id:"+orderId +",是否一折购订单:" + isEfun+"--睡眠出错");
			e.printStackTrace();
		}
		//获取数据
		List<Record> inventoryList = this.getPushInventory(orderId, isEfun);
		if(inventoryList != null && inventoryList.size() > 0){
			//推送
			String subject = orderId+","+ isEfun;
			PushPosRecord.dao.push(REQ_STORE_INVENTORY, inventoryList, subject);
		}
	}


	/**
	 * 获取推送信息
	 * @param storeNo
	 * @return
	 * @author chenhg
	 * 2016年11月23日 下午4:27:27
	 */
	private List<Record> getPushInventory(String orderId, boolean isEfun) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  ssm.store_no storeNo, ");			   //云店/仓库编号
		sql.append("  ssm.pro_count count, ");			   //数量
		sql.append("  ssm.update_time updateTime, ");	   //更新时间
		sql.append("  ssm.sku_code skuCode, ");			   //skuCode
		sql.append("  ssm.store_lock_count lockQuantity ");//仓库锁定数
		
		if(isEfun){//幸运一折购订单
			sql.append(" FROM (SELECT o2o_shop_no, sku_code from t_efun_user_order WHERE id = ?) euo  ");
			sql.append(" LEFT JOIN t_store_sku_map ssm ON euo.o2o_shop_no = ssm.store_no  ");
			sql.append(" WHERE euo.sku_code = ssm.sku_code  ");
		}else{
			sql.append(" FROM (SELECT id, o2o_shop_no from t_order WHERE id = ?) o  ");
			sql.append(" LEFT JOIN t_order_detail od ON o.id = od.order_id ");
			sql.append(" LEFT JOIN t_store_sku_map ssm ON o.o2o_shop_no = ssm.store_no  ");
			sql.append(" WHERE od.sku_code = ssm.sku_code  ");
			
		}
		return Db.find(sql.toString(), orderId);
	}
	
}
