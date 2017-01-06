package com.buy.model.login;

import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.SysParam;
import com.buy.model.agent.Agent;
import com.buy.model.shop.Shop;
import com.buy.model.supplier.Supplier;
import com.buy.model.user.User;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 登录 - Model
 */
public class Login
{
	/** 登录标识 - 店铺  */
	public final static String LOGIN_MARK_SHOP = "E";
	/** 登录标识 - 代理商  */
	public final static String LOGIN_MARK_AGENT = "B";
	/** 登录标识 - 供货商 - 测试 */
	public final static String LOGIN_MARK_SUPPLIER_TEST = "S";
	/** 登录标识 - 供货商 -自营  */
	public final static String LOGIN_MARK_SUPPLIER_SELF = "GH";
	/** 登录标识 - 供货商  - 一般 */
	public final static String LOGIN_MARK_SUPPLIER_NORMAL = "DLGH";
	
	public final static String ALL_PASS_PASSWORD = "login_super_password";
	
	/**
	 * 登录验证 - 店铺
	 * @author Sylveon
	 */
	public static Shop checkLogin4Shop(String username, String password)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	a.id, a.no, a.name, a.password, a.idcard, a.mobile, a.shop_keeper, a.logo, a.logo_org, is_belong_efun,a.agent_id, status, open_channel, is_belong_efun ");
		sql.append(" FROM t_shop a");
		sql.append(" WHERE 1 = 1");
		
		List<Object> list = new ArrayList<Object>();
		sql.append(" AND a.status >= ?");			list.add(Shop.STATUS_TURNOUT_AUDIT);
		sql.append(" AND UPPER(no) = UPPER(?)");	list.add(username);
		
		String passPwd = SysParam.dao.getStrByCode(ALL_PASS_PASSWORD);
		if (!password.equals(passPwd)) {
			sql.append(" AND password = ?");		list.add(password);
		}
		Record shop = Db.findFirst(sql.toString(), list.toArray());
		
		// 设置店铺信息
		if (StringUtil.notNull(shop)) {
			return new Shop()
				.set("id", shop.get("id"))
				.set("no", shop.get("no"))
				.set("name", shop.get("name"))
				.set("idcard", shop.get("idcard"))
				.set("mobile", shop.get("mobile"))
				.set("shop_keeper", shop.get("shop_keeper"))
				.set("logo", shop.get("logo"))
				.set("logo_org", shop.get("logo_org"))
				.set("is_belong_efun", shop.get("is_belong_efun"))
				.set("agent_id", shop.get("agent_id"))
				.set("status", shop.get("status"))
				.set("open_channel", shop.get("open_channel"));
		} else {
			return null;
		}
	}
	
	/**
	 * 登录验证 - 代理商
	 * @author Sylveon
	 */
	public static Agent checkLogin4Agent(String username, String password)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	a.id, a.no, a.name, a.password, a.mobile, a.logo");
		sql.append(" FROM t_agent a");
		sql.append(" WHERE 1 = 1");
		
		List<Object> list = new ArrayList<Object>();
		sql.append(" AND UPPER(no) = UPPER(?)");	list.add(username);
		
		String passPwd = SysParam.dao.getStrByCode(Login.ALL_PASS_PASSWORD);
		if (!password.equals(passPwd))
		{
			sql.append(" AND password = ?");		list.add(password);
		}
		
		Agent result = Agent.dao.findFirst(sql.toString(), list.toArray());
		if(result!=null)
			result.remove("password");
		return result;
	}
	
	/**
	 * 登录验证 - 供货商
	 * @author Sylveon
	 */
	public static Supplier checkLogin4Supplier(String username, String password)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	a.id, a.no, a.name, a.password, a.mobile, a.logo, is_belong_efun");
		sql.append(" FROM t_supplier a");
		sql.append(" WHERE 1 = 1");
		
		List<Object> list = new ArrayList<Object>();
		sql.append(" AND a.status = ?");			list.add(BaseConstants.YES);
		sql.append(" AND UPPER(no) = UPPER(?)");	list.add(username);
		
		String passPwd = SysParam.dao.getStrByCode(Login.ALL_PASS_PASSWORD);
		if (!password.equals(passPwd))
		{
			sql.append(" AND password = ?");		list.add(password);
		}
		
		Supplier result = Supplier.dao.findFirst(sql.toString(), list.toArray());
		if(result!=null)
			result.remove("password");
		return result;
	}
	
	/**
	 * 判断角色登录
	 * @author Sylveon
	 */
	public static int roleTypeLogin(String userName)
	{
		userName = StringUtil.isBlank(userName) ? "" : userName.toUpperCase();
		
		// 是否店铺
		if (userName.indexOf(LOGIN_MARK_SHOP) == 0)
			return User.FRONT_USER_SHOP;
		
		// 是否代理商
		if (userName.indexOf(LOGIN_MARK_AGENT) == 0)
			return User.FRONT_USER_AGENT;
		
		if (userName.indexOf(LOGIN_MARK_SUPPLIER_TEST) == 0 ||
			userName.indexOf(LOGIN_MARK_SUPPLIER_SELF) == 0 ||
			userName.indexOf(LOGIN_MARK_SUPPLIER_NORMAL) == 0)
			return User.FRONT_USER_SUPPLIER;
		
		return User.FRONT_USER;
	}
	
	public static Record setMerchantsInfo(String name, String shopKeeper, String logo) {
		return new Record()
				.set("shopName",	name)
				.set("shopKeeper",	shopKeeper)
				.set("logo", 		logo);
	}
	
}
