package com.buy.model.supplier;

import java.math.BigDecimal;
import java.util.Date;

import com.jfinal.plugin.activerecord.Model;

public class SupplierCashRecord extends Model<SupplierCashRecord> {

	private static final long serialVersionUID = 1L;
	public static final SupplierCashRecord dao = new SupplierCashRecord();
	
	/** 交易类型：提现 */
	public static final int TYPE_WITHDARWAL = 1;
	/** 交易类型：退款  */
	public static final int TYPE_REFUND = 2;
	/**
	 * 交易类型-转账
	 */
	public static final int TYPE_TRANSFER = 3;
	/**
	 * 交易类型-订单结算
	 */
	public static final int TYPE_BALANCE = 4;
	/**
	 * 交易类型-幸运一折购订单结算
	 */
	public static final int TYPE_EFUN_BALANCE = 5;
	
	/**
	 * 交易类型-金额调整（增加）
	 */
	public static final int TYPE_CHANG_ADD = 6;
	/**
	 * 交易类型-金额调整（减少）
	 */
	public static final int TYPE_CHANG_SUB = 7;
	
	/** 事项--现金申领常量（注：其它类型自己添加常量）*/
	public static final String REMARK_CASHWITHDRAWAL = "现金申领";
	
	/**
	 * 供货商现金对账单
	 * @param cash 变动金额
	 * @param remainCash  账户余额
	 * @param orderNo 
	 * @param type
	 * @param targetId
	 * @param supplierId
	 * @param remark
	 * @return
	 * @author chenhg
	 * 2016年3月3日 上午10:49:36
	 */
	public boolean add(BigDecimal cash, BigDecimal remainCash, String orderNo,
			int type, String supplierId, String remark){
		
		Supplier supplier = Supplier.dao.findByIdLoadColumns(supplierId, "no,name");
		SupplierCashRecord cashRecord = new SupplierCashRecord();
		cashRecord.set("cash", cash.setScale(2,BigDecimal.ROUND_DOWN));
		cashRecord.set("remain_cash", remainCash.setScale(2,BigDecimal.ROUND_DOWN));
		cashRecord.set("order_no", orderNo);
		cashRecord.set("supplier_id", supplierId);
		cashRecord.set("supplier_no", supplier.getStr("no"));
		cashRecord.set("supplier_name", supplier.getStr("name"));
		cashRecord.set("type", type);
		cashRecord.set("remark", remark);
		cashRecord.set("create_time", new Date());
		return cashRecord.save();
	}
	
}
