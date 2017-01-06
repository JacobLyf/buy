package com.buy.model.push;

import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class PushUserMap extends Model<PushUserMap> {
	
	private static final long serialVersionUID = 1L;
	public static final PushUserMap dao = new PushUserMap();
	
	public List<String> findRegIds(String userId) {
		if (StringUtil.isBlank(userId))
			return null;
		String sql = "SELECT registration_id regId FROM t_push_user_map WHERE user_id = ? AND is_push = ?";
		return Db.query(sql, userId, BaseConstants.YES);
	}
	
	public List<String> findRegIds(List<String> userIds) {
		if (StringUtil.isNull(userIds))
			return null;
		if (userIds.size() == 0)
			return null;
		String ids = StringUtil.listToStringForSql(",", userIds);
		String sql = "SELECT registration_id regId FROM t_push_user_map WHERE user_id IN (" + ids + ") AND is_push = ?";
		return Db.query(sql, BaseConstants.YES);
	}
	
	public PushUserMap getForLock(String regId, String userId) {
		String sql = "SELECT * FROM t_push_user_map WHERE registration_id = ? AND user_id = ? FOR UPDATE";
		return PushUserMap.dao.findFirst(sql, regId, userId);
	}
	
	public void deletePushUserMap(String regId, String userId) {
		String sql = "SELECT * FROM t_push_user_map WHERE registration_id = ? AND user_id = ?";
		PushUserMap pushUserMap = PushUserMap.dao.findFirst(sql, regId, userId);
		if (StringUtil.notNull(pushUserMap))
			pushUserMap.delete();
	}
	
}
