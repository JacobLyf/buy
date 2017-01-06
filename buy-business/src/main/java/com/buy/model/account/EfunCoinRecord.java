package com.buy.model.account;

import java.math.BigDecimal;
import java.util.Date;

import com.buy.common.BaseConstants;
import com.jfinal.plugin.activerecord.Model;

/**
 * 用户e趣币
 * @author Sylveon
 *
 */
public class EfunCoinRecord extends Model<EfunCoinRecord>{
	
	/**
	 * 交易类型-充值卡充值
	 */
	public static final int TYPE_RECHARGE = 0;
	
	/**
	 * 交易类型-购物币返还
	 */
	public static final int TYPE_SHOPPINGCOIN_RETURN = 1;
	
	/**
	 * 交易类型-撤销订单
	 */
	public static final int TYPE_CANCEL_ORDER = 2;
	
	/**
	 * 交易类型-转让（代理商转让店铺)
	 */
	public static final int TYPE_TRANSFER = 3;
	
	/**
	 * 交易类型-购物抵消
	 */
	public static final int TYPE_SHOPPING_OFFSET = 4;
	
	/**
	 * 交易类型-购物抵消
	 */
	public static final int TYPE_JOIN_EFUN = 5;
	
	private static final long serialVersionUID = 1L;
	private static final EfunCoinRecord dao = new EfunCoinRecord();
	
	public boolean add(BigDecimal efunCoin,BigDecimal remainEfunCoin,int type,String orderNo,String userId,String receiveUserId,String remark){
		
		EfunCoinRecord entity = new EfunCoinRecord();
		entity.set("efun_coin", efunCoin);
		entity.set("remain_efun_coin", remainEfunCoin);
		entity.set("type", type);
		entity.set("order_no", orderNo);
		entity.set("user_id", userId);
		entity.set("receive_user_id", receiveUserId);
		entity.set("remark", remark);
		entity.set("status", BaseConstants.YES);
		entity.set("create_time", new Date());
		return entity.save();
		
	}
	
}
