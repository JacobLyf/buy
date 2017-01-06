package com.buy.model.account;

import java.math.BigDecimal;
import java.util.Date;

import com.buy.common.BaseConstants;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 购物币
 * @author Sylveon
 *
 */
public class ShoppingCoinRecord extends Model<ShoppingCoinRecord>{
	
	/**
	 * 交易类型-充值卡充值
	 */
	public static final int TYPE_RECHARGE = 0;
	
	/**
	 * 交易类型-幸运一折购返还
	 */
	public static final int TYPE_EFUN_RETURN = 1;
	
	/**
	 * 交易类型-撤销订单
	 */
	public static final int TYPE_CANCEL_ORDER = 2;
	
	/**
	 * 交易类型-购物抵消
	 */
	public static final int TYPE_SHOPPING_OFFSET = 3;
	
	private static final long serialVersionUID = 1L;
	public static final ShoppingCoinRecord dao = new ShoppingCoinRecord();
	
	/**
	 * 新增
	 * @param shopingCoin  变动购物币（可正可负）
	 * @param remainShopingCoin  购物币余额
	 * @param type  交易类型(0：充值卡充值  1：幸运一折购返还  2：撤销订单  3：购物抵消）
	 * @param orderNo  订单编号（或流水号）
	 * @param userId  用户id(操作人/转让人)
	 * @param receiveUserId  被转让人
	 * @param userType  用户类型：0会员 1店铺 2代理商 3供货商
	 * @param remark  备注事项
	 * @param status  交易状态（0：失败 1：成功）
	 * @return
	 */
	public boolean add(BigDecimal shopingCoin,BigDecimal remainShopingCoin,int type,String orderNo,String userId,String receiveUserId,String remark){
		ShoppingCoinRecord shoppingCoinRecord = new ShoppingCoinRecord();
		shoppingCoinRecord.set("shoping_coin", shopingCoin);
		shoppingCoinRecord.set("remain_shoping_coin", remainShopingCoin);
		shoppingCoinRecord.set("type", type);
		shoppingCoinRecord.set("order_no", orderNo);
		shoppingCoinRecord.set("user_id", userId);
		shoppingCoinRecord.set("receive_user_id", receiveUserId);
		shoppingCoinRecord.set("remark", remark);
		shoppingCoinRecord.set("status", BaseConstants.YES);
		shoppingCoinRecord.set("create_time", new Date());
		return shoppingCoinRecord.save();
	}
	
}
