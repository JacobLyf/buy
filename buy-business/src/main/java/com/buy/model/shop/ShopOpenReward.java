package com.buy.model.shop;

import com.jfinal.plugin.activerecord.Model;

public class ShopOpenReward extends Model<ShopOpenReward> {

	private static final long serialVersionUID = 1L;
	public static final ShopOpenReward dao = new ShopOpenReward();
	
	/** 奖励类型 - 开店首月 **/
	public static final int TYPE_ONE_MONTH = 1;
	/** 奖励类型 - 开店两个月 **/
	public static final int TYPE_TWO_MONTH = 2;
	
	/** 发放情况 - 未发放 **/
	public static final int GRANt_STATUS_WAIT = 0;
	/** 发放情况 - 已发放 **/
	public static final int GRANt_STATUS_SUCCESS = 1;
	
	public ShopOpenReward getByLock(int id) {
		return ShopOpenReward.dao.findFirst("SELECT * FROM t_open_shop_reward WHERE id = ? FOR UPDATE", id);
	}
	
}
