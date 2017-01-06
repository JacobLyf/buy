package com.buy.model.agent;

import java.math.BigDecimal;
import java.util.Date;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.account.BankAccount;
import com.buy.model.user.UserCashApplyRecord;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class AgentCashApplyRecord extends Model<AgentCashApplyRecord> {

	
	private static final long serialVersionUID = 1L;
	public static final AgentCashApplyRecord dao = new AgentCashApplyRecord();
	
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
	 * 每天不能申请超过 20000元
	 * @param userId
	 * @param applyCash
	 * @return true：超过申请；false：没有超过申请
	 * @author chenhg
	 * 2016年4月21日 上午10:47:44
	 */
	public boolean applyCashAllow(String agentId,BigDecimal applyCash){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT IFNULL(SUM(a.cash),0) curCash FROM t_agent_cash_apply_record a  ");
		sql.append(" WHERE a.audit_status <> ? ");
		sql.append(" and a.apply_date > ? ");
		sql.append(" AND a.apply_date < ? ");
		sql.append(" AND a.agent_id = ? ");
		Date date = new Date();
		Date maxDate = DateUtil.getMaxDate(date);
		Date minDate = DateUtil.getMinDate(date);
		Record record = Db.findFirst(sql.toString(), UserCashApplyRecord.AUDIT_STATUS_UNPASS, minDate, maxDate, agentId);
		BigDecimal hasApplyCash = new BigDecimal(record.get("curCash").toString());
		BigDecimal all = hasApplyCash.add(applyCash);
		//限制每天申请金额：20000
		BigDecimal allowCash = new BigDecimal("20000");
		return all.compareTo(allowCash) > 0 ? true : false; 
	}

	
	/**
	 * 现金提现
	 * @param ret
	 * @return
	 * @author chenhg
	 * 2016年7月26日 下午2:26:46
	 */
	public boolean apply(Ret ret){
		String agentId = ret.get("agentId").toString();
		String applyCash = StringUtil.cutNullBlank(ret.get("applyCash").toString());//申请金额
		//转换成BigDecimal
		BigDecimal bapplyCash = new BigDecimal(applyCash);
		//每天限额20000
		BigDecimal maxApply = new BigDecimal("20000");
		Cache withdrawCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
		String key = BaseConstants.Redis.KEY_CASH_WITHDRAW + agentId;
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
				}
			}else{
				withdrawCache.setex(key, second, bapplyCash);
			}
		}
		//计算费用
		Record feeResult = SysParam.dao.calculateFees(applyCash);
		BigDecimal fee = new BigDecimal(feeResult.getStr("fees"));
		BigDecimal actualCash = new BigDecimal(feeResult.getStr("actualCash"));
		
		//获取账户信息
		BigDecimal currFreezeCash,currCash;
		Account account = new Account().getAccountForUpdate(agentId, Account.TYPE_AGENT);
		
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
		
		AgentCashApplyRecord cashApplyRecord = new AgentCashApplyRecord();
		Record bankRecord = BankAccount.dao.getBankCardMessage(agentId, BankAccount.TARGET_TYPE_AGENT);
		Agent agent = Agent.dao.findById(agentId);
		cashApplyRecord
			.set("cash", ret.get("applyCash"))
			.set("fees", fee)
			.set("actual_cash", actualCash)
			.set("bank_name", bankRecord.get("bankName"))
			.set("bank_no", bankRecord.get("accountNo"))
			.set("account_name", bankRecord.get("accountName"))
			.set("agent_id", agent.get("id"))
			.set("agent_no", agent.get("no"))
			.set("agent_name", agent.get("name"))
			.set("apply_date", new Date())
			.set("audit_status", AgentCashApplyRecord.AUDIT_STATUS_WAIT)
			.save();
		return true;
	}
}
