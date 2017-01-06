package com.buy.model.user;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 后台人员
 * @author Sylveon
 */
public class Admin extends Model<Admin> {

	private static final long serialVersionUID = 1L;
	public static final Admin dao = new Admin();
	
	/**
	 * 用户状态 - 正常
	 */
	public final static int STATUS_ENABLE = 1;
	/**
	 * 用户状态 - 禁用
	 */
	public final static int STATUS_DISABLE = 0;
	/**
	 * 用户状态 - 删除
	 */
	public final static int STATUS_DELETE = 2;

	public List<Admin> getAdminNameAndIdList(){
		return this.dao.find("select id,user_name from t_admin ");
	}
	
	/**
	 * 根据后台人员Id获取用户名
	 * @param adminId
	 * @return
	 * @author Jacob
	 * 2016年9月29日上午11:34:19
	 */
	public String getAdminName(String adminId){
		return dao.findByIdLoadColumns(adminId, "user_name").getStr("user_name");
	}

}
