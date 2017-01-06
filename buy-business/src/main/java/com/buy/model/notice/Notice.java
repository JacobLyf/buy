package com.buy.model.notice;

import com.jfinal.plugin.activerecord.Model;

public class Notice extends Model<Notice> {
	
	private static final long serialVersionUID = 1L;
	public static final Notice dao = new Notice();
	
	/**
	 * 消息类型 - 商城
	 */
	public static final int TYPE_EFUNSHOP = 1;
	/**
	 * 消息类型 - 会员
	 */
	public static final int TYPE_MEMBER = 2;
	/**
	 * 消息类型 - 店铺
	 */
	public static final int TYPE_SHOP = 3;
	/**
	 * 消息类型 - 代理商
	 */
	public static final int TYPE_AGENT = 4;
	/**
	 * 消息类型 - 供货商
	 */
	public static final int TYPE_SUPPLIER = 5;

}
