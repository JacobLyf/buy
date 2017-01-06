package com.buy.model.account;

import java.math.BigDecimal;
import java.util.Date;

import com.buy.model.agent.AgentCashRecord;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.integral.IntegralUserRecord;
import com.buy.model.order.Order;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.supplier.SupplierCashRecord;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRecord;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class Account extends Model<Account>{

	/**
	 * 账户
	 */
	private static final long serialVersionUID = 1L;
	
	public static final Account dao = new Account();

	/**
	 * 用户类型：会员
	 */
	public static final int TYPE_USER = 1;
	
	/**
	 * 用户类型：店铺
	 */
	public static final int TYPE_SHOP = 2;
	
	/**
	 * 用户类型：代理商
	 */
	public static final int TYPE_AGENT = 3;
	
	/**
	 * 用户类型：供货商
	 */
	public static final int TYPE_SUPPLIER = 4;
	
	/**
	 * 账户变动 - 添加
	 */
	public static final int CHANGE_ADD = 1;
	/**
	 * 账户变动 - 减少
	 */
	public static final int CHANGE_SUB = 2;
	
	
	/**
	 * 根据用户id获取账号
	 * @param userId
	 * @return
	 * @author huangzq
	 */
	public Account getAccountByUserId(String userId,Integer targetType){
		return Account.dao.findFirst("select * from t_account a where a.target_id = ? and a.target_type = ? ",new Object[]{userId,targetType});
		
	}
	/**
	 * 获取账号同时锁住该账号
	 * @param userId
	 * @return
	 * @author huangzq
	 */
	public Account getAccountForUpdate(String userId ,Integer targetType){
		Account account =  Account.dao.findFirst("select a.id from t_account a where a.target_id = ? and a.target_type = ?",new Object[]{userId,targetType});
		Integer id = account.getInt("id");
		return Account.dao.findFirst("select * from t_account a where a.id = ? for update",new Object[]{id});
		
	}
	/**
	 * 获得该用户的积分值
	 * @author HuangSx
	 * @date : 2015年9月24日 下午1:45:48
	 * @param userId
	 * @return
	 */
	public Integer getUserIntegralBalance(String userId){
		String sql ="select integral from t_account where target_id= ? and target_type= ?";
		return Db.queryInt(sql,userId,User.FRONT_USER);
	}
	
	/**
	 * 获取用户可用现金金额
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2015年12月11日下午3:23:06
	 */
	public BigDecimal getUserCash(String userId){
		String sql ="select cash from t_account where target_id=?";
		return Db.queryBigDecimal(sql,userId);
	}
	
	public boolean add(String targetId,Integer targetType){
		Account account = new Account();
		account.set("target_id", targetId);
		account.set("target_type", targetType);
		account.set("create_time", new Date());
		account.set("update_time", new Date());
		return account.save();
	}
	
	/**
	 * 更新所有会员剩余有效积分总数
	 * @author Jacob
	 * 2016年1月3日上午9:39:27
	 */
	public void updateUserAccountIntegral(){
		StringBuffer sql = new StringBuffer();
		sql.append(" UPDATE t_account a ");
		sql.append(" INNER JOIN ( ");
		sql.append(" 	SELECT ");
		sql.append(" 		SUM(remain_integral) remain_integral, ");
		sql.append(" 		user_id ");
		sql.append(" 	FROM ");
		sql.append(" 		`t_integral` ");
		sql.append(" 	WHERE ");
		sql.append(" 		validity_period > DATE_FORMAT(NOW(), '%Y-%m-%d 00:00:00') ");
		sql.append(" 	AND remain_integral > 0 ");
		sql.append(" 	GROUP BY ");
		sql.append(" 		user_id ");
		sql.append(" ) i ON a.target_id = i.user_id ");
		sql.append(" AND a.target_type = ? ");
		sql.append(" SET a.integral = i.remain_integral ");
		Db.update(sql.toString(),this.TYPE_USER);
	}
	
	/**
	 * 更新会员账户现金金额保存现金对账单
	 * @param userId 会员ID
	 * @param changeCash 变动金额
	 * @param type 对账单类型
	 * @param remark 备注
	 * @author Jacob
	 * 2016年1月18日上午11:07:00
	 */
	public void updateAccountCashAddSaveRecord(String userId,BigDecimal changeCash,Integer type,String remark){
		//更新账户现金余额
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		BigDecimal cash = account.getBigDecimal("cash");
		account.set("cash", cash.subtract(changeCash));
		account.update();
		
		//获取变动后账户现金余额
		cash = account.getBigDecimal("cash");
		
		// 处理余额
		BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
		BigDecimal remianCash = cash.add(freezeCash);
		
		//保存现金对账单
		UserCashRecord.dao.add(changeCash.multiply(new BigDecimal(-1)), remianCash, type, userId, remark);
	}
	
	/**
	 * 现金调整 - 增加
	 * @param targetId
	 * @param targetType
	 * @param changeCash
	 * @param remark
	 */
	public boolean updateAccountCashAdd(String targetId, Integer targetType, BigDecimal changeCash, String remark) {
		// 更新账户现金余额
		Account account = Account.dao.getAccountForUpdate(targetId, targetType);
		BigDecimal remian = account.getBigDecimal("cash");
		remian = remian.add(changeCash);
		account.set("cash", remian);
		account.set("update_time", new Date());
		account.update();
		
		// 保存现金对账单
		BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
		BigDecimal remianCash = remian.add(freezeCash);
		switch (targetType) {
		// 会员
		case User.FRONT_USER:
			UserCashRecord.dao.add(changeCash, remianCash,  UserCashRecord.TYPE_CHANG_ADD, targetId, remark);
			break;
		// 店铺
		case User.FRONT_USER_SHOP:
			ShopCashRecord.dao.add(changeCash, remianCash, "", ShopCashRecord.TYPE_CHANG_ADD, targetId, remark);
			break;
		// 代理商
		case User.FRONT_USER_AGENT:
			Integer accId = account.get("id");
			AgentCashRecord.dao.add(changeCash, remianCash, "", AgentCashRecord.TYPE_CHANG_ADD, accId + "", targetId, remark);
			break;
		// 代理商
		case User.FRONT_USER_SUPPLIER: 
			SupplierCashRecord.dao.add(changeCash, remianCash, "", SupplierCashRecord.TYPE_CHANG_ADD, targetId, remark);
			break;
		}
		
		return true;
	}
	
	/**
	 * 现金调整 - 减少
	 * @param targetId
	 * @param targetType
	 * @param changeCash
	 * @param remark
	 */
	public boolean updateAccountCashSub(String targetId, Integer targetType, BigDecimal changeCash, String remark) {
		// 更新账户现金余额
		Account account = Account.dao.getAccountForUpdate(targetId, targetType);
		BigDecimal remian = account.getBigDecimal("cash");
		changeCash = changeCash.multiply(new BigDecimal(-1));
		remian = remian.add(changeCash);
		if(remian.doubleValue() < 0)
			return false;
		account.set("cash", remian);
		account.set("update_time", new Date());
		account.update();
		
		// 保存现金对账单
		BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
		BigDecimal remianCash = remian.add(freezeCash);
		switch (targetType) {
		// 会员
		case User.FRONT_USER:
			UserCashRecord.dao.add(changeCash, remianCash,  UserCashRecord.TYPE_CHANG_SUB, targetId, remark);
			break;
		// 店铺
		case User.FRONT_USER_SHOP:
			ShopCashRecord.dao.add(changeCash, remianCash, "", ShopCashRecord.TYPE_CHANG_SUB, targetId, remark);
			break;
		// 代理商
		case User.FRONT_USER_AGENT:
			Integer accId = account.get("id");
			AgentCashRecord.dao.add(changeCash, remianCash, "", AgentCashRecord.TYPE_CHANG_SUB, accId + "", targetId, remark);
			break;
		// 代理商
		case User.FRONT_USER_SUPPLIER: 
			SupplierCashRecord.dao.add(changeCash, remianCash, "", SupplierCashRecord.TYPE_CHANG_SUB, targetId, remark);
			break;
		}
		
		return true;
	}
	
	/**
	 * 更新会员账户积分余额保存积分对账单（同时更新积分条目剩余值）
	 * @param userId 会员ID
	 * @param orderId 订单ID
	 * @param changeIntegral 变动积分数量
	 * @param integralRecordType 积分对账单类型 
	 * @param integralUserRecordType 用户积分使用记录表类型
	 * @param remark 对账单备注
	 * @author Jacob
	 * 2016年1月18日上午11:44:54
	 */
	public void updateAccountIntegralAddSaveRecord(String userId,String orderId,Integer changeIntegral,Integer integralRecordType,Integer integralUserRecordType,String remark){
		//更新账户现金余额
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		account.set("integral", account.getInt("integral")-changeIntegral);
		account.update();
		//保存积分对账单
		IntegralRecord integralRecord = new IntegralRecord();
		integralRecord.set("integral", -changeIntegral);//变动金额（可正可负）
		integralRecord.set("remain_integral", account.getInt("integral"));//账户余额
		integralRecord.set("type", integralRecordType);//积分消费类型
		integralRecord.set("user_id", userId);//用户id
		String userName = User.dao.getUserName(userId);
		integralRecord.set("user_no", userName);//用户账号
		integralRecord.set("user_name", userName);//用户名（这里也存用户账号）
		integralRecord.set("remark", remark);//备注
		integralRecord.set("create_time", new Date());
		integralRecord.save();
		//更新积分剩余值
		/**********************【积分扣除规则：按照积分有效期时间优先使用有效期快到期的积分】************************/
		//保存使用积分记录
		IntegralUserRecord.dao.saveIntegralUserRecord4Pay(orderId, userId, changeIntegral,integralUserRecordType);
	}
	
	/**
	 * 获取用户的股权、已申领认证股权
	 * @param targetId
	 * @param targetType
	 * @return
	 * @author chenhg
	 * 2016年2月25日 下午5:36:04
	 */
	public Record getStockMessage(String targetId, Integer targetType){
		String sql = "select stock, applied_stock from t_account where target_id = ? and target_type = ?";
		return Db.findFirst(sql, targetId, targetType);
	}
	
	/**
	 * 更新会员账户剩余股权值
	 * @param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2016年2月26日下午2:58:40
	 */
	public boolean updateUserStock(String userId){
		Account account = dao.getAccountForUpdate(userId, TYPE_USER);
		//获取会员所有订单商品总额（已收货并且交易正常的订单）
		BigDecimal allOrderTotal = Order.dao.getAllOrderTotal(userId);
		int allStock = allOrderTotal.intValue()/100;//累计总股权数=订单商品总额/100
		account.set("stock", allStock-account.getInt("applied_stock"));//累计总股权数-已申领认证股权数=剩余股权数
		return account.update();
	}
	
	/**
	 * 获取账户金额
	 * @param userId
	 * @param targetType：目标类型(1:用户，2：店铺，3：代理商，4：供货商)
	 * @return
	 * @author chenhg
	 * 2016年7月23日 下午2:26:57
	 */
	public BigDecimal getRemainCash(String targetId, int targetType){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" a.cash, ");
		sql.append(" a.freeze_cash ");
		sql.append(" FROM ");
		sql.append(" t_account a ");
		sql.append(" WHERE ");
		sql.append(" a.target_id = ? ");
		sql.append(" AND a.target_type = ? ");
		Record record =  Db.findFirst(sql.toString(), targetId, targetType);
		BigDecimal remainCash = new BigDecimal("0.00");
		if(record!=null){
			BigDecimal cash = record.getBigDecimal("cash");
			BigDecimal freezeCash = record.getBigDecimal("freeze_cash");
			remainCash = cash.add(freezeCash);
		}
		return remainCash;
	}
	
	/**
	 * 验证申领金额 不可大于 可申领金额
	 * @param targetId
	 * @param applyCash
	 * @param targetType目标类型(1:用户，2：店铺，3：代理商，4：供货商)
	 * @return
	 * @author chenhg
	 * 2016年7月23日 下午4:38:40
	 */
	public boolean applyCashIsOK(String targetId,BigDecimal applyCash, int targetType){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" a.cash ");
		sql.append(" FROM ");
		sql.append(" t_account a ");
		sql.append(" WHERE ");
		sql.append(" a.target_id = ? ");
		sql.append(" AND a.target_type = ? ");
		Record record = Db.findFirst(sql.toString(), targetId, targetType);
		String cash = record.get("cash").toString();
		BigDecimal bCash = new BigDecimal(cash);
		
		return bCash.compareTo(applyCash) >= 0 ? true : false; 
	}
	
	/**
	 * 现金提取--获取账户金额信息(四个角色)
	 * @param targetId
	 * @param targetType目标类型(1:用户，2：店铺，3：代理商，4：供货商)
	 * @return
	 * @author chenhg
	 * 2016年7月25日 下午2:11:15
	 */
	public Record getAccountMessage(String targetId, int targetType){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" a.cash, ");
		sql.append(" a.freeze_cash, ");
		sql.append(" a.cash + a.freeze_cash totalCash");
		sql.append(" FROM ");
		sql.append(" t_account a ");
		sql.append(" WHERE ");
		sql.append(" a.target_id = ? ");
		sql.append(" AND a.target_type = ? ");
		return Db.findFirst(sql.toString(), targetId, targetType);
	}
	
	/**
	 * app、商家版app
	 * 进入现金提现
	 * @param userId
	 * @return
	 * @author chenhg
	 * 2016年7月25日 下午2:22:34
	 */
	public Record getAppAccountMessage(String targetId, int targetType){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" a.cash usableCash, ");
		sql.append(" a.freeze_cash, ");
		sql.append(" date_format(now(),'%Y-%m-%d') applyDate ");
		sql.append(" FROM ");
		sql.append(" t_account a ");
		sql.append(" WHERE ");
		sql.append(" a.target_id = ? ");
		sql.append(" AND a.target_type = ? ");
		Record record =  Db.findFirst(sql.toString(), targetId, targetType);
		BigDecimal remainCash = new BigDecimal("0.00");
		BigDecimal usableCash = new BigDecimal("0.00");
		if(record!=null){
			usableCash = record.getBigDecimal("usableCash");
			BigDecimal freezeCash = record.getBigDecimal("freeze_cash");
			remainCash = usableCash.add(freezeCash);
		}
		
		record.set("remainCash", remainCash);
		
		Record bankRecord = BankAccount.dao.getBankCardMessage(targetId, targetType);
		
		if(bankRecord!=null){
			record.set("accountName", bankRecord.get("accountName"));
			record.set("accountNo", bankRecord.get("accountNo"));
			record.set("bankName", bankRecord.get("bankName"));
		}else{//还没有设置银行卡时
			record.set("accountName", "");
			record.set("accountNo", "");
			record.set("bankName", "");
		}
		
		return record;
	}
	
	/**
	 * 获取会员我的股权
	 * @author chenhg
	 * 2016年3月7日 上午11:39:01
	 */
	public Record getMyStock(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" a.stock remainStock ");
		sql.append(" FROM ");
		sql.append(" t_account a ");
		sql.append(" WHERE ");
		sql.append(" a.target_id = ? ");
		sql.append(" AND a.target_type = ? ");
		
		return Db.findFirst(sql.toString(), userId, Account.TYPE_USER);
	}
	
	/**
	 * 用户积分余额
	 * @author jekay
	 * @date : 2016年3月7日 上午11:27:51
	 */
	public Integer myIntegral(String userId){
		StringBuffer sbf = new StringBuffer();
		sbf.append("SELECT ");
		sbf.append(" a.integral as remainIntegral ");//我的积分余额
		sbf.append(" FROM ");
		sbf.append(" t_account a ");
		sbf.append(" WHERE ");
		sbf.append(" a.target_id = ? ");
		return Db.queryInt(sbf.toString(),userId);
	}
	
	/**
	 * 奖励用户积分
	 * @param userId 用户ID
	 * @param integeral 积分值
	 */
	public void rewardUserIntegral(String userId, Integer integeral){
		Account account = Account.dao.getAccountByUserId(userId, TYPE_USER);
		account.set("integral", account.getInt("integral") + integeral);
		account.update();
	}
	
	/**
	 * 根据用户id获取账号以及账户信息
	 * @param userId
	 * @return
	 * @author huangzq
	 */
	public Record getAccountAndUserByUserId(String userId,Integer targetType){
		return Db.findFirst("select t1.id user_id,t1.user_name,t2.* from t_user t1,t_account t2 where t2.target_id = t1.id and t2.target_type = ? AND t1.id = ? ",new Object[]{targetType,userId});
		
	}
	
	/**
	 * 获取可用金额、可用积分
	 * @author chenhg
	 * @param userId
	 * @param targetType
	 * @return
	 * @date 2017年1月1日 上午9:21:51
	 */
	public Record getCashAndIntegral(String userId, int targetType){
		return Db.findFirst("SELECT a.integral, a.cash FROM t_account a WHERE a.target_id = ? AND a.target_type = ? ", userId, targetType);
	}
}
