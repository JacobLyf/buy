package com.buy.model.user;

import java.util.ArrayList;
import java.util.List;

import com.buy.common.Ret;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 实名制
 */
public class RealName extends Model<RealName> {
	
	/**
	 * 实名制审核状态 - 等待审核
	 */
	public static final int STATUS_WAIT = 0;
	/**
	 * 实名制审核状态 - 审核通过
	 */
	public static final int STATUS_PASS = 1;
	/**
	 * 实名制审核状态 - 审核不通过
	 */
	public static final int STATUS_UNPASS = 2;
	

	private static final long serialVersionUID = 1L;

	public static final RealName dao = new RealName();
	
	/**
	 * 根据实名认证ID查找实名认证信息
	 * @param realNameId	实名认证ID
	 * @return				实名认证信息
	 * @author 				Sylveon
	 */
	public RealName getRealNameById(Integer realNameId) {
		String sql = "SELECT id, real_name, idcard FROM t_user_realname WHERE id = ?";
		return RealName.dao.findFirst(sql, realNameId);
	}
	
	/**
	 * 查找备注
	 * @param realNameId	实名认证ID
	 * @return				备注
	 * @author 				Sylveon
	 */
	public String getRemark(Integer realNameId) {
		String sql = "SELECT remark FROM t_user_realname WHERE id = ?";
		return Db.queryStr(sql, realNameId);
	}
	
	/**
	 * 身份证是否被使用
	 * @param r	条件
	 * @return	true 已使用；false 未使用
	 * @author 	Sylveon
	 */
	public boolean existRealName(Ret r) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	id");
		sql.append(" FROM t_user_realname");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND user_id = ?");
		sql.append(" AND idcard = ?");
		sql.append(" AND id != ?");
		List<Object> list = new ArrayList<>();
		list.add(r.get("userId"));
		list.add(r.get("idcard"));
		list.add(r.get("realNameId"));
		RealName get = RealName.dao.findFirst(sql.toString(), list.toArray());
		return null == get ? false : true;
	}
	
}
