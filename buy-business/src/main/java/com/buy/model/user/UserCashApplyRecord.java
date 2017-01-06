package com.buy.model.user;

import java.math.BigDecimal;
import java.util.Date;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.account.BankAccount;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class UserCashApplyRecord extends Model<UserCashApplyRecord> {

	private static final long serialVersionUID = 1L;
	
	public static final UserCashApplyRecord dao = new UserCashApplyRecord();
	
	/**
	 * 审核状态：待审核
	 */
	public static final int AUDIT_STATUS_WAIT = 0;
	
	/**
	 * 审核状态：通过
	 */
	public static final int AUDIT_STATUS_PASS = 1;
	
	/**
	 * 审核状态：未通过
	 */
	public static final int AUDIT_STATUS_UNPASS = 2;
	
	/**
	 * 获取今日可申领金额
	 * @param userId
	 * @return
	 * @author chenhg
	 * 2016年4月21日 上午11:15:30
	 */
	public BigDecimal getTodayAllowCash(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT IFNULL(SUM(a.cash),0) curCash FROM t_user_cash_apply_record a  ");
		sql.append(" WHERE a.audit_status <> ? ");
		sql.append(" and a.apply_date > ? ");
		sql.append(" AND a.apply_date < ? ");
		sql.append(" AND a.user_id = ? ");
		Date date = new Date();
		Date maxDate = DateUtil.getMaxDate(date);
		Date minDate = DateUtil.getMinDate(date);
		Record record = Db.findFirst(sql.toString(), UserCashApplyRecord.AUDIT_STATUS_UNPASS, minDate, maxDate, userId);
		BigDecimal hasApplyCash = new BigDecimal(record.get("curCash").toString());
		BigDecimal allowCash = new BigDecimal("20000");
		
		return allowCash.subtract(hasApplyCash);
	}
	
	
	/**
	 * 每天不能申请超过 20000元
	 * @param userId
	 * @param applyCash
	 * @return true：超过申请；false：没有超过申请
	 * @author chenhg
	 * 2016年4月21日 上午10:47:44
	 */
	public boolean applyCashAllow(String userId,BigDecimal applyCash){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT IFNULL(SUM(a.cash),0) curCash FROM t_user_cash_apply_record a  ");
		sql.append(" WHERE a.audit_status <> ? ");
		sql.append(" and a.apply_date > ? ");
		sql.append(" AND a.apply_date < ? ");
		sql.append(" AND a.user_id = ? ");
		Date date = new Date();
		Date maxDate = DateUtil.getMaxDate(date);
		Date minDate = DateUtil.getMinDate(date);
		Record record = Db.findFirst(sql.toString(), UserCashApplyRecord.AUDIT_STATUS_UNPASS, minDate, maxDate, userId);
		BigDecimal hasApplyCash = new BigDecimal(record.get("curCash").toString());
		BigDecimal all = hasApplyCash.add(applyCash);
		//限制每天申请金额：20000
		BigDecimal allowCash = new BigDecimal("20000");
		return all.compareTo(allowCash) > 0 ? true : false; 
	}
	/**
	 * 现金提取-申请列表
	 * @param userId
	 * @param page
	 * @author chenhg
	 */
	public Page<Record> getApplyList(String userId, Page<Object> page) {
		StringBuffer selectSql = new StringBuffer();
		StringBuffer whereSql = new StringBuffer();
		
		selectSql.append(" SELECT ");
		selectSql.append(" date_format(a.apply_date,'%Y-%m-%d') applyDate, ");
		selectSql.append(" a.bank_name bankName, ");
		selectSql.append(" a.cash applyCash, ");
		selectSql.append(" a.id applyId ");
		
		whereSql.append(" FROM ");
		whereSql.append(" t_user_cash_apply_record a ");
		whereSql.append(" WHERE  a.user_id = ? ");
		whereSql.append(" ORDER BY a.apply_date DESC  ");
		
		return Db.paginate(page.getPageNumber(), page.getPageSize(), selectSql.toString(), whereSql.toString(), userId);
		
	}
	
	
	/**
	 * 现金提现详情
	 * @author chenhg
	 */
	public Record getApplyDetail(String applyId){
		StringBuffer sql = new StringBuffer();
		
		sql.append(" SELECT ");
		sql.append(" a.cash applyCash, ");
		sql.append(" a.fees, ");
		sql.append(" a.actual_cash actualCash, ");
		sql.append(" date_format(a.apply_date,'%Y-%m-%d') applyDate, ");
		sql.append(" a.bank_name bankName, ");
		sql.append("	CASE LENGTH(a.bank_no)");
		sql.append("	WHEN  15 THEN CONCAT('**** **** *** ',RIGHT(a.bank_no,4))");
		sql.append("	WHEN  16 THEN CONCAT('**** **** **** ',RIGHT(a.bank_no,4))");
		sql.append("	WHEN  17 THEN CONCAT('**** **** ***** ',RIGHT(a.bank_no,4))");
		sql.append("	WHEN  18 THEN CONCAT('**** **** ****** ',RIGHT(a.bank_no,4))");
		sql.append("	WHEN  19 THEN CONCAT('**** **** **** *** ',RIGHT(a.bank_no,4))");
		sql.append("	ELSE a.bank_no");
		sql.append("	END accountNo, ");
		
		sql.append(" a.auditor_opinion auditorOpinion, ");
		sql.append(" a.audit_status auditStatus, ");
		sql.append(" date_format(a.audit_time,'%Y-%m-%d') auditDate ");
		
		sql.append(" FROM ");
		sql.append(" t_user_cash_apply_record a ");
		sql.append(" WHERE  a.id = ? ");
		
		return Db.findFirst(sql.toString(), applyId);
	}
	
	
	@Before(Tx.class)
	public boolean apply(Ret ret){
		String userId = ret.get("userId");
		String applyCash = StringUtil.cutNullBlank(ret.get("applyCash").toString());//申请金额
		//转换成BigDecimal
		BigDecimal bapplyCash = new BigDecimal(applyCash);
		//每天限额20000
		BigDecimal maxApply = new BigDecimal("20000");
		Cache withdrawCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
		String key = BaseConstants.Redis.KEY_CASH_WITHDRAW + userId;
		//计算失效时间
		Date now = new Date();
		Date max = DateUtil.getMaxDate(now);
		int second = (int)(max.getTime() - now.getTime()) / 1000;
		synchronized (this) {
			if(withdrawCache.exists(key)){
				BigDecimal hasApply = new BigDecimal(withdrawCache.get(key).toString());
				hasApply = hasApply.add(bapplyCash);
				if(hasApply.compareTo(maxApply) > 0){
					return false;
				}else{
					withdrawCache.setex(key,second,hasApply);
//					BigDecimal curTotal = new BigDecimal(withdrawCache.get(key).toString());
//					//再判断是否超额了
//					if(curTotal.compareTo(maxApply) > 0){
//						withdrawCache.setex(key,second,curTotal.subtract(bapplyCash));
//						return false;
//					}
				}
			}else{
				withdrawCache.setex(key, second, bapplyCash);
			}
		}
		
		
		//计算费用
		Record feeResult = SysParam.dao.calculateFees(applyCash);
		BigDecimal fee = new BigDecimal(feeResult.getStr("fees"));
		BigDecimal actualCash = new BigDecimal(feeResult.getStr("actualCash"));
		
		BigDecimal currFreezeCash,currCash;
		
		Account account = new Account().getAccountForUpdate(userId, Account.TYPE_USER);
		
		BigDecimal bcash = account.getBigDecimal("cash"); //原有金额
		BigDecimal bfreezeCash = account.getBigDecimal("freeze_cash"); //原冻结金额
		
		currFreezeCash = bfreezeCash.add(bapplyCash);//计算冻结金额
		currCash = bcash.subtract(bapplyCash);//计算现金余额
		if(currCash.compareTo(BigDecimal.ZERO)<0){
			return false;
		}
		account.set("cash", currCash);
		account.set("freeze_cash", currFreezeCash);
		account.update();
		
		UserCashApplyRecord cashApplyRecord = new UserCashApplyRecord();
		User user = User.dao.findById(userId);
		Record bankRecord = BankAccount.dao.getBankCardMessage(userId, BankAccount.TARGET_TYPE_UESER);
		cashApplyRecord
			.set("cash", applyCash)
			.set("fees", fee)
			.set("actual_cash", actualCash)
			.set("bank_name", bankRecord.get("bankName"))
			.set("bank_no", bankRecord.get("accountNo"))
			.set("account_name", bankRecord.get("accountName"))
			.set("user_id", user.get("id"))
			.set("user_no", user.get("user_name"))
			.set("user_name", user.get("user_name"))
			.set("apply_date", new Date())
			.set("audit_status", UserCashApplyRecord.AUDIT_STATUS_WAIT)
			.save();
		
		return true;
	}
}
