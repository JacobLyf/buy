package com.buy.model.agent;

import com.jfinal.plugin.activerecord.Model;

/**
 * Model 店铺维修
 */
public class ShopRecycle extends Model<ShopRecycle> {

	private static final long serialVersionUID = 1L;
	
	public static final ShopRecycle dao = new ShopRecycle();
	
	/**
	 * 回收类型 - 未激活
	 */
	public static final int STATUS_UNACTIVATED = 1;
	
	/**
	 * 回收类型 - 未续费
	 */
	public static final int STATTUS_TIMEOUT = 2;
	
}
