package com.buy.model.account;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class BankAccount extends Model<BankAccount> {
	
	/** 目标类型 - 会员 */
	public static final int TARGET_TYPE_UESER = 1;
	/** 目标类型 - 店铺 */
	public static final int TARGET_TYPE_SHOP = 2;
	/** 目标类型 - 代理商 */
	public static final int TARGET_TYPE_AGENT = 3;
	/** 目标类型 - 供货商 */
	public static final int TARGET_TYPE_SUPPLIER = 4;
	

	/**
	 * 银行卡
	 */
	private static final long serialVersionUID = 1L;
	
	public static final BankAccount dao = new BankAccount();
	
	/**
	 * 供货商、店铺、代理商是否已经设置银行卡
	 * @param targetId
	 * @param targetType
	 * @return true：有；false：无
	 * @author chenhg
	 * 2016年7月25日 上午9:59:35
	 */
	public boolean hasSetBankCard(String targetId, int targetType){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT a.account_no ");
		sql.append(" FROM t_bank_account a ");
		sql.append(" LEFT JOIN t_bank b ON a.bank_id = b.id ");
		sql.append(" WHERE a.target_id = ? ");
		sql.append(" AND a.target_type = ? ");
		
		Record record = Db.findFirst(sql.toString(), targetId, targetType);
		if(null == record || StringUtil.isNull(record.getStr("account_no"))){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 获取账户银行卡信息
	 * @param targetId
	 * @param targetType
	 * @return
	 * @author chenhg
	 * 2016年7月25日 下午1:21:27
	 */
	public Record getBankCardMessage(String targetId, int targetType){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  a.account_name accountName, ");
		sql.append("  a.account_no accountNo,");
		sql.append("  b.`name` bankName");
		sql.append(" FROM ");
		sql.append("  t_bank_account a");
		sql.append(" LEFT JOIN t_bank b ON a.bank_id = b.id");
		sql.append(" WHERE ");
		sql.append("  a.target_id = ?");
		sql.append("  AND a.target_type = ?");
		
		return Db.findFirst(sql.toString(), targetId, targetType);
	}
}
