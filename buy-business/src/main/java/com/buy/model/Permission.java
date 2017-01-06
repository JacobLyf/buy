package com.buy.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class Permission  extends Model<Permission>{
	
	/**
	 * 权限类型：菜单权限
	 */
	public final static int MENU_PERSSION = 0;
	/**
	 * 权限类型：功能权限
	 */
	public final static int FUN_PERSSION = 1;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Permission dao = new Permission();
	
	/**
	 * 根据用户组列出所有权限
	 * @param groupId
	 * @return
	 */
	public List<String> getGroupPermissions(Integer groupId){
		List<String> permissions = new ArrayList<String>();
		List<Permission> li =  dao.find("select p.* from shiro_permission p,shiro_g_p gp " +
				"where gp.group_id=? and p.permission_id = gp.permission_id", new Object[]{groupId});
		for(Permission p: li){
			permissions.add(p.getStr("permission_url"));
		}
		return  permissions;
	}
}
