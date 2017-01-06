package com.buy.model;

import com.jfinal.plugin.activerecord.Model;

public class Menu extends Model<Menu>{
	
	/**
	 * 菜单类型：系统
	 */
	public final static int TYPE_SYSTEN = 1;
	/**
	 * 菜单类型：(业务)咨询
	 */
	public final static int TYPE_BUSSINESS = 2;
	/**
	 * 菜单类型：(业务)其他
	 */
	public final static int TYPE_OTHER = 3;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final Menu dao = new Menu();
	
	

}
