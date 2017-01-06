package com.buy.model.user;

import java.util.Date;

import com.buy.common.JsonMessage;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.plugin.event.user.sign.UserSignEvent;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.EventKit;

/**
 * 签到 
 */
public class UserSign extends Model<UserSign> {

	private static final long serialVersionUID = 1;
	public static final UserSign dao = new UserSign();
	public static final String SIGN_KEY = "sign_";
	/**
	 * 保存签到
	 */
	public JsonMessage saveSignIn(String userId) {
		JsonMessage result = new JsonMessage();

		// 账户表锁表
		Account.dao.getAccountForUpdate(userId, User.FRONT_USER);

		// 验证签到
		boolean isSign = isSignToday(userId);
		if (isSign)
			return result.setStatusAndMsg("1", "今天已经签到");

		// 签到参数
		Date now = new Date();
		String signDate = DateUtil.DateToString(now, DateStyle.YYYY_MM_DD);	// 签到日期
		int comboy = getSignComboYestoday(userId);							// 连续签到天数 - 昨天
		int combo = comboy + 1;
		if (combo > 30)
			combo = 1;

		// 签到
		new UserSign()
				.set("user_id",		userId)
				.set("sign_date",	signDate)
				.set("create_time",	now)
				.set("combo",		combo)
				.save();

		// 添加积分
		int integralVal = SysParam.dao.getIntByCode("sign_reward");
		new Integral().save(userId, integralVal, "签到获取", IntegralRecord.TYPE_GET_INTEGRAL);

		// 签到奖励
		EventKit.postEvent(new UserSignEvent(userId));

		return result.setData(combo);
	}
	
	/**
	 * 签到信息
	 */
	public UserSign getSignInfo(String userId) {
		Date now = new Date();
		return UserSign.dao.findFirst(new StringBuffer(" SELECT id, combo FROM t_user_sign ")
			.append(" WHERE user_id = ? ")
			.append(" AND sign_date = ? ")
			.append(" ORDER BY id DESC ")
			.toString(),
			userId, DateUtil.DateToString(now, DateStyle.YYYY_MM_DD)
		);
	}
	
	/**
	 * 签到详细信息
	 */
	public Record getDetail(String userId) {
		Date now = new Date();
		int combo10 = 0;									// 连续获奖次数 - 每10天
		int comboy = getSignComboYestoday(userId);			// 连续签到天数 - 昨天
		int combot = getSignComboToday(userId);				// 连续签到天数 - 今天
		int combo = comboy;									// 连续签到天数
		String tip = "";									// 签到文案显示
		
		// 处理连续签到天数
		if (combot > 0)				++combo;
		if (combo >= 30) {
			if (combot == 0)		combo = 0;
			else if (combot == 1)	combo = 1;
		}		
		
		// 设置签到页面显示内容
		if (combo == 0) {
			combo10 = 0;
			tip = "距领取50积分奖励还有" + (10 - combo) + "天";
		} else if (combo == 10) {
			if (combot == 0) {
				combo10 = combo - 10;
				tip = "距领取5元红包抽奖还有" + 10 + "天";
			} else {
				combo10 = 10;
				tip = "今天是50积分奖励领取日";	
			}
		} else if (combo > 10 && combo < 20) {
			combo10 = combo - 10;
			tip = "距领取5元红包抽奖还有" + (10 - combo10) + "天";
		} else if (combo == 20) {
			if (combot == 0) {
				combo10 = combo - 20;
				tip = "距领取10元红包抽奖还有" + 10 + "天";
			} else {
				combo10 = 20;
				tip = "今天是抽奖赢5元红包奖励领取日";	
			}
		} else if (combo > 20 && combo < 30) {
			combo10 = combo - 20;
			tip = "距领取10元红包抽奖还有" + (10 - combo10) + "天";
		} else if (combo == 30) {
			combo10 = 30;
			tip = "今天是抽奖赢10元红包奖励领取日";
		} else {
			combo10 = combo;
			tip = "距领取50积分奖励还有" + (10 - combo10) + "天";
		}
		
		// 是否签到
		boolean isSign = isSignToday(userId);
		
		// 满20/30日是否获取奖励
		int flag20 = UserSignRewardRecord.dao.isGetReward(userId, UserSignRewardRecord.TYPE_20DAY) ? 1 : 0;
		int flag30 = UserSignRewardRecord.dao.isGetReward(userId, UserSignRewardRecord.TYPE_30DAY) ? 1 : 0;
		return new Record()
				.set("year",		DateUtil.DateToString(now, "yyyy"))
				.set("month",		DateUtil.DateToString(now, "MM"))
				.set("date",		DateUtil.DateToString(now, "dd"))
				.set("combo",		combo)
				.set("combo10",		combo10)
				.set("flag20",		flag20)
				.set("flag30",		flag30)
				.set("isSign",		true == isSign ? 1 : 0)
				.set("tip",			tip);
	}
	
	/**
	 * 查询连续签到次数
	 */
	public Integer getSignComboToday(String userId) {
		Date now = new Date();
		String sql  = new StringBuffer(" SELECT combo FROM t_user_sign ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_date = ? ")
				.toString();
		Integer combo = Db.queryInt(sql,userId, DateUtil.DateToString(now, DateStyle.YYYY_MM_DD));
		return StringUtil.isNull(combo) ? 0 : combo;
	}
	
	/**
	 * 查询昨天连续签到次数
	 */
	public Integer getSignComboYestoday(String userId) {
		Date yestoday = new Date();
		yestoday = DateUtil.addDay(yestoday, -1);
		String sql  = new StringBuffer(" SELECT combo FROM t_user_sign ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_date = ? ")
				.toString();
		Integer combo = Db.queryInt(sql, userId, DateUtil.DateToString(yestoday, DateStyle.YYYY_MM_DD));
		return StringUtil.isNull(combo) ? 0 : combo;
	}
	
	/**
	 * 是否签到
	 */
	public boolean isSignToday(String userId) {
		Date now = new Date();
		long count = Db.queryLong(new StringBuffer(" SELECT COUNT(1) FROM t_user_sign ")
				.append(" WHERE user_id = ? ")
				.append(" AND sign_date = ? ")
				.toString(),
				userId, DateUtil.DateToString(now, DateStyle.YYYY_MM_DD)
		);
		return count > 0 ? true : false;
	}
	
}