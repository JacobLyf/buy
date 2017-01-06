package com.buy.model.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.dreamlu.event.EventKit;

import com.buy.common.Ret;
import com.buy.model.message.Message;
import com.buy.plugin.event.account.UserCashRecordUpdateEvent;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class UserCashRecord extends Model<UserCashRecord> {

	/**
	 * 交易类型-购物
	 */
	public static final int TYPE_SHOPPING = 1;

	/**
	 * 交易类型-充值
	 */
	public static final int TYPE_RECHARGE = 2;

	/**
	 * 交易类型-提现
	 */
	public static final int TYPE_WITHDRAW_CASH = 3;

	/**
	 * 交易类型-退款
	 */
	public static final int TYPE_REFUND = 4;

	/**
	 * 交易类型-取消订单
	 */
	public static final int TYPE_CANCEL_ORDER = 5;
	/**
	 * 交易类型-转账
	 */
	public static final int TYPE_TRANSFER = 6;
	/**
	 * 交易类型-E趣购
	 */
	public static final int TYPE_EUN_ORDER = 7;

	/**
	 * 交易类型-金额调整（增加）
	 */
	public static final int TYPE_CHANG_ADD = 8;
	/**
	 * 交易类型-金额调整（减少）
	 */
	public static final int TYPE_CHANG_SUB = 9;
	/**
	 * 交易类型-支付幸运一折购运费
	 */
	public static final int TYPE_EFUN_FREIGHT = 10;
	/**
	 * 抢钱活动
	 */
	public static final int TYPE_ACTIVITY_SNATCH_MANEY = 11;
	/**
	 * 签到
	 */
	public static final int TYPE_SIGN = 12;

	/**
	 * 一折购退款
	 */
	public static final int TYPE_EFUN_REFUND = 13;

	/**
	 * 事项--现金申领常量（注：其它类型自己添加常量）
	 */
	public static final String REMARK_CASHWITHDRAWAL = "现金申领";

	/**
	 * 现金记录
	 */
	private static final long serialVersionUID = 1L;

	public static final UserCashRecord dao = new UserCashRecord();

	/**
	 * 添加用户现金记录
	 * 
	 * @param cash
	 * @param remainCash
	 * @param orderNo
	 * @param type
	 * @param userId
	 * @param remark
	 * @return
	 * @author huangzq
	 */
	public boolean add(BigDecimal cash, BigDecimal remainCash, int type, String userId, String remark) {
		User user = User.dao.findByIdLoadColumns(userId, "user_name");
		String userName = user.getStr("user_name");

		cash = cash.setScale(2, BigDecimal.ROUND_DOWN);
		UserCashRecord cashRecord = new UserCashRecord();
		cashRecord.set("cash", cash);
		cashRecord.set("remain_cash", remainCash.setScale(2, BigDecimal.ROUND_DOWN));
		// cashRecord.set("order_no", orderNo);
		cashRecord.set("user_no", userName);
		cashRecord.set("user_name", userName);
		cashRecord.set("type", type);
		cashRecord.set("user_id", userId);
		cashRecord.set("remark", remark);
		cashRecord.set("create_time", new Date());
		boolean flag = cashRecord.save();
		// 发送消息
		if (flag) {
			Ret source = Message.dao.init4UserCashRecord(userId, userName, cash.abs(), type, remark);
			EventKit.postEvent(new UserCashRecordUpdateEvent(source));
		}
		return flag;
	}

	/**
	 * 现金对账记录
	 * 
	 * @author chenhg
	 */
	public Page<Record> getUserCashRecord(Ret ret, Page<Object> page) {
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<>();

		select.append(" SELECT");
		select.append(" 	cr.id recordId,");
		select.append(" 	cr.remain_cash remainCash,");
		select.append(" 	cr.remark,");
		select.append(" 	date_format(cr.create_time,'%Y-%m-%d %H:%i:%S') happenTime,");
		select.append(" 	cr.cash");
		where.append(" FROM t_user_cash_record cr");
		where.append(" where cr.user_id = ?");
		paras.add(ret.get("userId"));
		if ("1".equals(ret.get("type").toString())) {// 转入
			where.append("  and cr.cash > 0 ");
		}
		if ("2".equals(ret.get("type").toString())) {// 转出
			where.append("  and cr.cash < 0 ");
		}

		where.append(" ORDER BY cr.create_time DESC");

		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(),
				paras.toArray());
	}
}
