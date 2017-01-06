package com.buy.model.shop;

import java.math.BigDecimal;
import java.util.Date;

import net.dreamlu.event.EventKit;

import org.apache.log4j.Logger;

import com.buy.common.Ret;
import com.buy.model.agent.ShopRecycle;
import com.buy.model.message.Message;
import com.buy.plugin.event.account.ShopCashRecordUpdateEvent;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Model;

public class ShopCashRecord extends Model<ShopCashRecord> {
	
	private  Logger L = Logger.getLogger(ShopCashRecord.class);

	private static final long serialVersionUID = 1L;
	
	public static final ShopCashRecord dao = new ShopCashRecord();
	

	/** 交易类型：提现 */
	public static final int TYPE_WITHDARWAL = 1;
	/** 交易类型 ：退款  */
	public static final int TYPE_REFUND = 2;
	/**
	 * 交易类型-转账
	 */
	public static final int TYPE_TRANSFER = 3;
	/**
	 * 交易类型-订单结算
	 */
	public static final int TYPE_BALANCE = 4;
	
	/** 交易类型 ：充值 */
	public static final int TYPE_RECHARGE = 5;
	/**
	 * 交易类型：保证金缴纳
	 */
	public static final int TYPE_DEPOSIT = 6;
	/**
	 * 交易类型：公共订单返利
	 */
	public static final int TYPE_REBATE_PUBLIC_ORDER = 7;
	/**
	 * 交易类型：本店铺订单返利
	 */
	public static final int TYPE_REBATE_SELF_ORDER = 8;
	/**
	 * 交易类型：其它店铺订单返利
	 */
	public static final int TYPE_REBATE_OTHER_ORDER = 9;
	/**
	 * 交易类型：店铺幸运一折购返利
	 */
	public static final int TYPE_REBATE_SHOP_EFUN = 10;
	/**
	 * 交易类型：假一赔十
	 */
	public static final int TYPE_PEISHI = 11;
	/**
	 * 交易类型：续费
	 */
	public static final int TYPE_RENEW = 12;
	/**
	 * 交易类型-幸运一折购订单结算
	 */
	public static final int TYPE_EFUN_BALANCE = 13;
	
	/**
	 * 交易类型：保证金退回
	 */
	public static final int TYPE_DEPOSIT_BACK = 14;
	/**
	 * 交易类型：假一赔十退回
	 */
	public static final int TYPE_PEISHI_BACK = 15;
	
	/**
	 * 交易类型-金额调整（增加）
	 */
	public static final int TYPE_CHANG_ADD = 16;
	/**
	 * 交易类型-金额调整（减少）
	 */
	public static final int TYPE_CHANG_SUB = 17;
	
	
	/** 事项--现金申领常量（注：其它类型自己添加常量）*/
	public static final String REMARK_CASHWITHDRAWAL = "现金申领";
	
	
	
	/**
	 * 添加用户现金记录
	 * @param cash
	 * @param remainCash
	 * @param orderNo
	 * @param type
	 * @param userId
	 * @param remark
	 * @return
	 * @author huangzq
	 */
	public boolean add(BigDecimal cash,BigDecimal remainCash,String orderNo,int type,String shopId,String remark){
		// 查询店铺信息
		Shop shop = Shop.dao.findByIdLoadColumns(shopId, "no,name");
		String shopNo = "";
		String shopName = "";
		// 若店铺空，则查询回收店铺
		if (null == shop) {
			ShopRecycle recycle = ShopRecycle.dao.findByIdLoadColumns(shopId, "no,name");
			shopNo = recycle.getStr("no");
			shopName = recycle.getStr("name");
		} else {
			shopNo = shop.getStr("no");
			shopName = shop.getStr("name");
		}
		
		cash = cash.setScale(2,BigDecimal.ROUND_DOWN);
		ShopCashRecord cashRecord = new ShopCashRecord();
		cashRecord.set("cash", cash);
		cashRecord.set("remain_cash", remainCash.setScale(2,BigDecimal.ROUND_DOWN));
		cashRecord.set("order_no", orderNo);
		cashRecord.set("shop_no", shopNo);
		cashRecord.set("shop_name", shopName);
		cashRecord.set("type", type);
		cashRecord.set("shop_id", shopId);
		cashRecord.set("remark", remark);
		cashRecord.set("create_time", new Date());
		boolean flag = cashRecord.save();
		// 发送消
		if (flag) {
			Ret source = Message.dao.init4ShopCashRecord(shopId, shopNo, cash.abs(), type, remark);
			EventKit.postEvent(new ShopCashRecordUpdateEvent(source));
		}
		return flag;
	}
	
	/**
	 * 添加用户现金记录(非购物订单使用)
	 * @param cash
	 * @param remainCash
	 * @param efunOrderId 幸运一折购订单ID（或者其他）
	 * @param type
	 * @param userId
	 * @param remark
	 * @return
	 * @author huangzq
	 */
	public boolean addNotOrder(BigDecimal cash,BigDecimal remainCash,String efunOrderId,int type,String shopId,String remark){
		Shop shop = Shop.dao.findByIdLoadColumns(shopId, "no,name");
		boolean flag = false;
		if(shop!=null){
			String shopNo = shop.getStr("no");
			cash = cash.setScale(2,BigDecimal.ROUND_DOWN);
			ShopCashRecord cashRecord = new ShopCashRecord();
			cashRecord.set("cash", cash);
			cashRecord.set("remain_cash", remainCash.setScale(2,BigDecimal.ROUND_DOWN));
			cashRecord.set("target_id", efunOrderId);
			cashRecord.set("shop_no", shopNo);
			cashRecord.set("shop_name", shop.getStr("name"));
			cashRecord.set("type", type);
			cashRecord.set("shop_id", shopId);
			cashRecord.set("remark", remark);
			cashRecord.set("create_time", new Date());
			flag = cashRecord.save();
			// 发送消
			if (flag) {
				Ret source = Message.dao.init4ShopCashRecord(shopId, shopNo, cash.abs(), type, remark);
				EventKit.postEvent(new ShopCashRecordUpdateEvent(source));
			}
		}else{
			L.info("店铺不存在，导致无法添加店铺现金对账单");
		}
		return flag;
	}
	
}
