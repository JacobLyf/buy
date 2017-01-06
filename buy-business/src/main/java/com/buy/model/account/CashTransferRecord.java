package com.buy.model.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.model.shop.Shop;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.supplier.Supplier;
import com.buy.model.supplier.SupplierCashRecord;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRecord;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * Model - 现金转账
 */
public class CashTransferRecord extends Model<CashTransferRecord> {
	
	/**
	 * 用户类型 - 会员
	 */
	public static final int USER_TYPE_MEMBER = 1;
	/**
	 * 用户类型 - 店主
	 */
	public static final int USER_TYPE_SHOP = 2;
	/**
	 * 用户类型 - 代理商
	 */
	public static final int USER_TYPE_AGENT = 3;
	/**
	 * 用户类型 - 供货商
	 */
	public static final int USER_TYPE_SUPPLIER = 4;
	/**
	 * 编号前缀
	 */
	public static final String PREFIX_NO = "TX";

	private static final long serialVersionUID = 1L;
	
	
	public static final CashTransferRecord dao = new CashTransferRecord();
	
	
	/**
	 * 会员、店铺、供货商现金转账记录列表
	 * @author chenhg
	 */
	public Page<Record> getCashTransferPage(String userId,Page<Object> page, int Type){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		select.append(" SELECT *  ");
		where.append("  FROM  ");
		where.append("  (  ");
		where.append("   SELECT ");
		where.append("    '转入' type,");
		where.append("    date_format(ctr.transfer_date,'%Y-%m-%d') transferDate, "); 
		where.append("    ctr.out_no outNo, "); 
		where.append("    ctr.in_no inNo, "); 
		where.append("    ctr.id transferId, "); 
		where.append("    ctr.no transferNo, "); 
		where.append("    ctr.cash transferCash, "); 
		where.append("    ctr.create_time "); 
		where.append("    FROM t_cash_transfer_record ctr "); 
		where.append("  WHERE 1 = 1 "); 
		where.append("     AND ctr.in_user_type = ?"); 
		where.append("     AND ctr.in_user_id = ?"); 
		where.append("  UNION   "); 
		where.append("  SELECT "); 
		where.append("    '转出' type, "); 
		where.append("    date_format(ctr.transfer_date,'%Y-%m-%d') transferDate, "); 
		where.append("    ctr.out_no outNo, "); 
		where.append("    ctr.in_no inNo, "); 
		where.append("    ctr.id transferId, "); 
		where.append("    ctr.no transferNo, "); 
		where.append("    ctr.cash transferCash, "); 
		where.append("    ctr.create_time "); 
		where.append("    FROM t_cash_transfer_record ctr "); 
		where.append("    WHERE 1 = 1 "); 
		where.append("    AND ctr.out_user_type = ? "); 
		where.append("    AND ctr.out_user_id = ? "); 
		where.append(" ) tt    "); 
		where.append(" ORDER BY tt.create_time DESC    "); 
		
		paras.add(Type);
		paras.add(userId);
		paras.add(Type);
		paras.add(userId);
		
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(), paras.toArray());
	}
	
	
	/**
	 * 店铺现金转账
	 * @param accountNo
	 * @param money
	 * @param shopId
	 * @author huangzq
	 */
	@Before(Tx.class)
	public boolean transferByShop(String userName,BigDecimal money,String shopId){
		//更新商家账户
		Shop shop = Shop.dao.findById(shopId);
		Account shopAccount  = Account.dao.getAccountForUpdate(shopId, Account.TYPE_SHOP);
		BigDecimal cash = shopAccount.getBigDecimal("cash");
		if(cash.compareTo(money)>=0){
			shopAccount.set("cash", cash.subtract(money));
			shopAccount.update();
			//添加店铺现金记录
			BigDecimal freezeCashShop = shopAccount.getBigDecimal("freeze_cash");
			ShopCashRecord.dao.add(money.multiply(new BigDecimal(-1)), shopAccount.getBigDecimal("cash").add(freezeCashShop), "", ShopCashRecord.TYPE_TRANSFER, shopId, "现金转账");
			//更新会员账户
			User user = User.dao.findFirst("select u.* from t_user u where u.user_name = ?",userName);
			Account userAccount  = Account.dao.getAccountForUpdate(user.getStr("id"), Account.TYPE_USER);
			userAccount.set("cash", userAccount.getBigDecimal("cash").add(money));
			userAccount.update();
			//添加会员现金记录
			BigDecimal freezeCashUser = userAccount.getBigDecimal("freeze_cash");
			UserCashRecord.dao.add(money, userAccount.getBigDecimal("cash").add(freezeCashUser),  UserCashRecord.TYPE_TRANSFER, user.getStr("id"), "现金转账");
			
			//添加转账记录
			CashTransferRecord cashTransferRecord = new CashTransferRecord();
			Date now = new Date();
			cashTransferRecord.set("no", StringUtil.getUnitCode(CashTransferRecord.PREFIX_NO));
			cashTransferRecord.set("transfer_date", now);
			cashTransferRecord.set("cash", money);
			cashTransferRecord.set("out_no", shop.get("no"));
			cashTransferRecord.set("out_user_id", shopId);
			cashTransferRecord.set("out_user_type", CashTransferRecord.USER_TYPE_SHOP);
			cashTransferRecord.set("out_user_name", shop.get("name"));
			cashTransferRecord.set("in_no",user.getStr("user_name"));
			cashTransferRecord.set("in_user_id", user.getStr("id"));
			cashTransferRecord.set("in_user_name",user.getStr("user_name"));
			cashTransferRecord.set("in_user_type", CashTransferRecord.USER_TYPE_MEMBER);
			cashTransferRecord.set("create_time", now);
			cashTransferRecord.save();
			return true;
		}
		return false;
	}
	
	
	/**
	 * 现金转账
	 * @param userName
	 * @param money
	 * @param supplierId
	 * @author huangzq
	 */
	@Before(Tx.class)
	public boolean transferBySupplier(String userName,BigDecimal money,String supplierId){
		//更新供货商账户
		Supplier supplier = Supplier.dao.findById(supplierId);
		Account supplierAccount  = Account.dao.getAccountForUpdate(supplierId, Account.TYPE_SUPPLIER);
		BigDecimal cash = supplierAccount.getBigDecimal("cash");
		if(cash.compareTo(money)>=0){
			supplierAccount.set("cash", cash.subtract(money));
			supplierAccount.update();
			//添加供应商现金记录
			BigDecimal freezeCashSupplier = supplierAccount.getBigDecimal("freeze_cash");
			SupplierCashRecord.dao.add(money.multiply(new BigDecimal(-1)), supplierAccount.getBigDecimal("cash").add(freezeCashSupplier), "", SupplierCashRecord.TYPE_TRANSFER, supplierId, "现金转账");
			//更新会员账户
			User user = User.dao.findFirst("select u.* from t_user u where u.user_name = ?",userName);
			Account userAccount  = Account.dao.getAccountForUpdate(user.getStr("id"), Account.TYPE_USER);
			userAccount.set("cash", userAccount.getBigDecimal("cash").add(money));
			userAccount.update();
			//添加会员现金记录
			BigDecimal freezeCashUser = userAccount.getBigDecimal("freeze_cash");
			UserCashRecord.dao.add(money, userAccount.getBigDecimal("cash").add(freezeCashUser),  UserCashRecord.TYPE_TRANSFER, user.getStr("id"), "现金转账");
			
			//添加转账记录
			CashTransferRecord cashTransferRecord = new CashTransferRecord();
			Date now = new Date();
			cashTransferRecord.set("no", StringUtil.getUnitCode(CashTransferRecord.PREFIX_NO));
			cashTransferRecord.set("transfer_date", now);
			cashTransferRecord.set("cash", money);
			cashTransferRecord.set("out_no", supplier.get("no"));
			cashTransferRecord.set("out_user_id", supplierId);
			cashTransferRecord.set("out_user_type", CashTransferRecord.USER_TYPE_SUPPLIER);
			cashTransferRecord.set("out_user_name", supplier.get("name"));
			cashTransferRecord.set("in_no",user.getStr("user_name"));
			cashTransferRecord.set("in_user_id", user.getStr("id"));
			cashTransferRecord.set("in_user_name",user.getStr("user_name"));
			cashTransferRecord.set("in_user_type", CashTransferRecord.USER_TYPE_MEMBER);
			cashTransferRecord.set("create_time", now);
			cashTransferRecord.save();
			return true;
		}
		return false;
	
	}
}
