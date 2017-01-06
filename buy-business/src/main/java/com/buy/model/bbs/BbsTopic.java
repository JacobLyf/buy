package com.buy.model.bbs;

import com.jfinal.plugin.activerecord.Model;

/**
 * Model - BBS - 帖子
 */
public class BbsTopic extends Model<BbsTopic> {

	private static final long serialVersionUID = 1L;
	
	public static final BbsTopic dao = new BbsTopic();

	/**
	 * 创建人类型 - 后台
	 */
	public static final int USER_TYPE_BACK = 1;
	/**
	 * 创建人类型 - 前台
	 */
	public static final int USER_TYPE_FRONT = 2;
	
}
