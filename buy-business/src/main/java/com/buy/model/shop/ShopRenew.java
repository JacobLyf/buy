package com.buy.model.shop;

import java.math.BigDecimal;
import java.util.Date;

import net.dreamlu.event.EventKit;

import com.buy.common.JsonMessage;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.agent.AgentCashRecord;
import com.buy.model.trade.Trade;
import com.buy.plugin.event.shop.ShopUpdateEvent;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * Model - 店铺续费
 * @author 
 */
public class ShopRenew extends Model<ShopRenew> {
	
	private static final long serialVersionUID = 1L;
	public static final ShopRenew dao = new ShopRenew();
	/**
	 * 审核状态：未审核
	 */
	public static final int AUDIT_STATUS_UNCHECKED = 0; 
	/**
	 * 审核状态：审核通过
	 */
	public static final int AUDIT_STATUS_SUCCESS = 1; 
	/**
	 * 审核状态：审核不通过
	 */
	public static final int AUDIT_STATUS_FAIL = 2;
	
	
	
	
	/**
	 * 续费方式:手动续费
	 */
	public static final int TYPE_MANUAL = 1; 
	
	/**
	 * 续费方式:自动续费
	 */
	public static final int TYPE_AUTO = 2; 
	
	/**
	 * 续费类型:续费
	 */
	public static final int TYPE_RENEW = 1; 
	
	/**
	 * 续费类型:预缴费
	 */
	public static final int TYPE_PREPAID = 2; 
	
	
	
	/**
	 * 付款方式：余额支付
	 */
	public static final int PAY_TYPE_CASH= 1; 
	/**
	 * 付款方式：在线支付
	 */
	public static final int PAY_TYPE_ONLINE= 2; 
	
	/**
	 * 每次续费的月数
	 */
	public static final int RENEW_MONTHS = 3;
	
	
	/**
	 * 现金续费
	 * @param shopId
	 * @param times 倍数
	 * @return
	 */
	
	public JsonMessage cashRenew(String shopId,int times){
		JsonMessage jsonMessage = new JsonMessage();
		Date now = new Date();
		Account account = Account.dao.getAccountForUpdate(shopId, Account.TYPE_SHOP);
		BigDecimal remainCash = account.getBigDecimal("cash");
		//获取续费金额
		BigDecimal cash = SysParam.dao.getBigDecimalByCode("shop_renew_cash").multiply(new BigDecimal(times));
		//判断可用余额是否满足
		if(cash.compareTo(remainCash)==1){
			jsonMessage.setStatusAndMsg("2", "可用余额不足");
			return jsonMessage;
		}
		//更新账户余额
		account.set("cash", remainCash.subtract(cash));
		account.update();
		//更新店铺到期时间
		Shop shop = Shop.dao.findByIdLoadColumns(shopId, "id,no,agent_id,expire_date,forbidden_status");
		Date expireDate = shop.getDate("expire_date");
		//计算续费后的日期
		if(expireDate.before(new Date())){
			expireDate = DateUtil.getMaxDate(now);
		}
		expireDate = DateUtil.addMonth(expireDate, ShopRenew.RENEW_MONTHS*times);
		shop.set("expire_date", expireDate);
		int forbiddenStatus = shop.getInt("forbidden_status");
		//判断店铺是否处于到期冻结状态，是则续费完改为正常状态
		if(Shop.FORBIDDEN_STATUS_DISABLE_UNPAY == forbiddenStatus){
			shop.set("forbidden_status", Shop.FORBIDDEN_STATUS_NORMAL);
			shop.update();
			EventKit.postEvent(new ShopUpdateEvent(shop.getStr("id")));//更新索引
		}else{
			shop.update();
		}
		//添加现金对账单记录
		BigDecimal freezeCashShop = account.getBigDecimal("freeze_cash");
		ShopCashRecord.dao.add(cash.multiply(new BigDecimal(-1)), account.getBigDecimal("cash").add(freezeCashShop), "", ShopCashRecord.TYPE_RENEW, shopId, "店铺续费");
		//添加续费记录
		ShopRenew renew = new ShopRenew();
		renew.set("shop_id", shopId);
		renew.set("cash", cash);
		renew.set("type", ShopRenew.TYPE_MANUAL);
		renew.set("pay_type", ShopRenew.PAY_TYPE_CASH);
		renew.set("audit_status", ShopRenew.AUDIT_STATUS_SUCCESS);
		renew.set("audit_time", now);
		renew.set("expire_time", shop.getDate("expire_date"));
		renew.set("create_time", now);
		renew.save();
		//e趣商城收取续费金额
		BigDecimal rentCash = SysParam.dao.getBigDecimalByCode("efun_shop_renew_cash");
		//获取代理商账号
		Account agentAccount = Account.dao.getAccountForUpdate(shop.getStr("agent_id"), Account.TYPE_AGENT);
		//更新账户余额
		remainCash = agentAccount.getBigDecimal("cash");
		BigDecimal addCash = cash.subtract(rentCash);
		agentAccount.set("cash", remainCash.add(addCash));
		agentAccount.update();
		//添加代理商返利记录
		BigDecimal freezeCashAgent = agentAccount.getBigDecimal("freeze_cash");
		AgentCashRecord.dao.add(addCash, agentAccount.getBigDecimal("cash").add(freezeCashAgent), "", AgentCashRecord.REBATE_SHOP_RENT,renew.getInt("id").toString(), shop.getStr("agent_id"),AgentCashRecord.REMARK_REBATE_SHOP_RENT);
		return jsonMessage;
	}
	
	public Record getPayInfoByOpen(String shopId) {
		StringBuffer sql = new StringBuffer(" SELECT ")
			.append(" a.pay_way, ")
			.append(" a.third_bill_no, ")
			.append(" a.platform ")
			.append(" FROM t_trade a ")
			.append(" LEFT JOIN t_shop_apply b ON CONCAT(b.id, '') = a.target ")
			.append(" WHERE a.type = ? ")
			.append(" AND a.status = ? ")
			.append(" AND b.shop_id = ? ")
			.append(" ORDER BY a.create_time DESC ");
		
		return Db.findFirst(sql.toString(), Trade.TYPE_SHOP_OPEN, Trade.STATUS_SUCCESS, shopId);
	}
	
	public Record getPayInfoByTurn(String shopId) {
		StringBuffer sql = new StringBuffer(" SELECT ")
		.append(" a.pay_way, ")
		.append(" a.third_bill_no, ")
		.append(" a.platform ")
		.append(" FROM t_trade a ")
		.append(" LEFT JOIN t_shop_certification_record b ON CONCAT(b.id, '') = a.target ")
		.append(" WHERE a.type = ? ")
		.append(" AND a.status = ? ")
		.append(" AND b.shop_id = ? ")
		.append(" ORDER BY a.create_time DESC");
		
		return Db.findFirst(sql.toString(), Trade.TYPE_SHOP_ACTIVITY, Trade.STATUS_SUCCESS, shopId);
	}
	/**
	 * 获取最新一条续费信息
	 * @param shopId
	 * @return
	 */
	public Record getLastRenew(String shopId){
		String sql = "select * from t_shop_renew s where s.shop_id = ? order by s.id desc";
		return Db.findFirst(sql,shopId);
		
	}

}
