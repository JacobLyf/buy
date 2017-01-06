package com.buy.model.college;

import com.jfinal.plugin.activerecord.Model;

public class CollegeType extends Model<CollegeType> {

	private static final long serialVersionUID = 1L;
	
	public static final CollegeType dao = new CollegeType();
	
	/**
	 * 文章类型 - e趣学院
	 */
	public static final int TYPE_EFUN_COLLEGE = 1;
	
	/**
	 * 文章类型 - 帮助中心
	 */
	public static final int TYPE_HELP_CENTER = 2;
	
	/**
	 * e趣学院 - 会员
	 */
	public static final int EFUN_COLLEGE_USER = 1;
	
	/**
	 * e趣学院 - 店主
	 */
	public static final int EFUN_COLLEGE_SHOP = 2;
	
}
