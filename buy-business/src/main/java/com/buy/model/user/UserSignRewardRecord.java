package com.buy.model.user;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import com.buy.common.JsonMessage;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.account.Account;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class UserSignRewardRecord extends Model<UserSignRewardRecord> {
	
	private static final long serialVersionUID = 1L;
	public static final UserSignRewardRecord dao = new UserSignRewardRecord();
	
	/** 奖励类型 - 签到 **/
	public static final int TYPE_SIGN = 1;
	/** 奖励类型 - 满10日 **/
	public static final int TYPE_10DAY= 2;
	/** 奖励类型 - 满20日 **/
	public static final int TYPE_20DAY = 3;
	/** 奖励类型 - 满30日 **/
	public static final int TYPE_30DAY = 4;
	
	/** 获奖状态 - 未获取 **/
	public static final int STATUS_NOT_GET = 0;
	/** 获奖状态 - 已获取 **/
	public static final int STATUS_HAS_GET = 1;
	
	/** 奖励 - 满20日 **/
	public static final int[] REWARD_20DAY  = {5, 3, 2, 1, 200, 150, 100, 50};
	/** 奖励 - 满30日 **/
	public static final int[] REWARD_30DAY  = {10, 5, 3, 1, 300, 200, 100, 50};
	
	/** 中奖概率 - 满20日 **/
	public static final int[] PERCENT_20DAY  = {2, 3, 5, 10, 10, 10, 10, 50};
	/** 中奖概率 - 满30日 **/
	public static final int[] PERCENT_30DAY  = {2, 3, 5, 10, 10, 10, 10, 50};
	
	/** 获奖备注 - 满20日 **/
	public static final String[] REMARK_20DAY  = {"5元现金红包", "3元现金红包", "2元现金红包", "1元现金红包", "200积分", "150积分", "100积分", "50积分"};
	/** 获奖备注 - 满30日 **/
	public static final String[] REMARK_30DAY  = {"10元现金红包", "5元现金红包", "3元现金红包", "1元现金红包", "300积分", "200积分", "100积分", "50积分"};
	
	/** 页面显示 - 满20日 **/
	public static final int[] HTML_20DAY  = {0, 2, 6, 4, 1, 3, 7, 5};
	/** 页面显示 - 满30日 **/
	public static final int[] HTML_30DAY  = {0, 2, 6, 4, 1, 3, 7, 5};
	
	/**
	 * 添加获奖记录
	 */
	public void addByToReward(String userId, int type) {
		UserSign sign =  UserSign.dao.getSignInfo(userId);
		if (StringUtil.isNull(sign))
			return;
		int signId = sign.getInt("id");
		Db.update(new StringBuffer(" INSERT INTO t_user_sign_reward_record (user_id, sign_id, type, status, create_time) ")
				.append(" SELECT ?, ?, ?, ?, NOW() FROM DUAL WHERE NOT EXISTS ( ")
					.append(" SELECT user_id, sign_id, create_time FROM t_user_sign_reward_record ")
					.append(" WHERE user_id = ? ")
					.append(" AND sign_id = ? ")
					.append(" AND type = ? ")
				.append(" ) ")
				.toString(), userId , signId, type , STATUS_NOT_GET, userId, signId, type);
	}
	
	/**
	 * 是否获奖
	 */
	public boolean isGetReward(String userId, int type) {
		Date now = new Date();
		int diffdate = 10;
		if (TYPE_20DAY == type)			diffdate = 20;
		else if (TYPE_30DAY == type)	diffdate = 30;
		Date before = DateUtil.addDay(now, diffdate * -1);
		
		Integer signId = Db.queryInt(new StringBuffer(" SELECT id FROM t_user_sign ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_date BETWEEN ? AND ? ")
				.append(" AND combo = ? ").toString(),
				userId,
				DateUtil.DateToString(before, DateStyle.YYYY_MM_DD),
				DateUtil.DateToString(now, DateStyle.YYYY_MM_DD),
				diffdate);
		if (StringUtil.isNull(signId))
			return false;
		
		long count = Db.queryLong(new StringBuffer(" SELECT COUNT(1) FROM t_user_sign_reward_record ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_id = ? ")
				.append(" AND type = ? ")
				.append(" AND status = ? ")
				.toString(), userId, signId, type, STATUS_HAS_GET);
		return  count > 0 ? true : false;
	}
	
	/**
	 * 抽奖
	 */
	public JsonMessage updateByReward(String userId, int type) {
		JsonMessage result = new JsonMessage();
		
		Date now = new Date();
		int diffdate = 10;									// 连续签到满足天数
		int[] percentArr = null;							// 抽奖概率
		String[] remarkArr = null;							// 备注
		int[] rewardArr = null;								// 奖励
		int[] htmlArr = null;								// 页面显示中奖
		
		// 设置 - 连续签到满足天数 + 抽奖概率
		if (TYPE_20DAY == type) {
			diffdate = 20;
			percentArr = PERCENT_20DAY;
			remarkArr = REMARK_20DAY;
			rewardArr = REWARD_20DAY;
			htmlArr = HTML_20DAY;
		} else if (TYPE_30DAY == type) {
			diffdate = 30;
			percentArr = PERCENT_30DAY;
			remarkArr = REMARK_30DAY;
			rewardArr = REWARD_30DAY;
			htmlArr = HTML_30DAY;
		} else {
			return result.setStatusAndMsg("1", "非法提交");
		}
		
		// 验证签到
		Integer signId = Db.queryInt(new StringBuffer(" SELECT id FROM t_user_sign ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_date = ? ")
				.append(" AND combo = ? ")
				.toString(), userId, DateUtil.DateToString(now, DateStyle.YYYY_MM_DD), diffdate);
		if (StringUtil.isNull(signId))
			return result.setStatusAndMsg("1", "非法提交");
		
		// 验证获奖记录
		Integer rewardId = Db.queryInt(new StringBuffer(" SELECT id FROM t_user_sign_reward_record ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_id = ? ")
				.append(" AND type = ? ")
				.toString(), userId, signId, type);
		if (StringUtil.isNull(rewardId))
			return result.setStatusAndMsg("1", "非法提交");
		
		// 验证是否已获取奖励
		String lockSql = "SELECT * FROM t_user_sign_reward_record WHERE id = ? FOR UPDATE";
		UserSignRewardRecord lock =  UserSignRewardRecord.dao.findFirst(lockSql, rewardId);
		int status = lock.getInt("status");
		if (STATUS_HAS_GET == status)
			return result.setStatusAndMsg("1", "非法提交");
		
		// 中奖
		int ran = new Random().nextInt(100) + 1;
		StringBuffer sql = new StringBuffer(" SELECT (CASE ");
		int i = 1;
		int j = 0;
		for (int pa : percentArr) {
			int percent = pa;
			int beg = i;
			int end = i + percent -1;
			sql
				.append(" WHEN ").append(ran)
				.append(" BETWEEN ").append(beg)
				.append(" AND ").append(end)
				.append(" THEN ").append(j);
			i += percent;
			j++;
		}
		sql.append(" ELSE 0 END) FROM DUAL ");
		int boom = Db.queryNumber(sql.toString()).intValue();
		
		// 修改获奖记录
		lock
			.set("remark",		remarkArr[boom])
			.set("status",		STATUS_HAS_GET)
			.set("create_time",	now)
			.update();
		
		// 奖励
		if (boom <= 3) {
			// 现金奖励
			BigDecimal cash = new BigDecimal(rewardArr[boom] + "");
			// 更新账户余额
			Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
			account.set("cash", account.getBigDecimal("cash").add(cash)).update();
			// 添加现金记录
			BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
			UserCashRecord.dao.add(cash, account.getBigDecimal("cash").add(freezeCash),  UserCashRecord.TYPE_SIGN, userId, "连续签到" + diffdate + "天奖励");
		} else {
			// 积分奖励
			int integral = rewardArr[boom];
			new Integral().save(userId, integral, "连续签到" + diffdate + "天奖励",  IntegralRecord.TYPE_GET_INTEGRAL);
		}
		
		// 处理返回中奖参数
		return result.setData(htmlArr[boom]);
	}
	
	/**
	 * 奖励记录
	 */
	public Page<Record> findByPage(Page<?> page, String userId) {
		String select = new StringBuffer(" SELECT create_time createTime, remark ").toString();
		String where = new StringBuffer(" FROM t_user_sign_reward_record ")
				.append(" WHERE user_id = ? ")
				.append(" AND status = ? ")
				.append(" ORDER BY create_time DESC, id DESC ")
				.toString();
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select, where, userId, STATUS_HAS_GET);
	}
	
}
