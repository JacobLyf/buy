package com.buy.model.supplier;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class Supplier extends Model<Supplier>{
	
	private static final long serialVersionUID = 1L;
	
	public final static Supplier dao = new Supplier();
	
	public final static String INITPASSWORD = "000000";
	public final static String INITPAYPASSWORD = "123456";
	
	/**
	 * 代理商是否存在
	 * @param supplierId	代理商ID
	 * @param supplierNo	代理商编号
	 * @return			true 存在； false 不存在
	 * @author			Sylveon
	 */
	public boolean existSupplier(String supplierId, String supplierNo) {
		String sql = "SELECT id FROM t_supplier WHERE 1 = 1 AND id != ? AND no = ?";
		Supplier supplier = Supplier.dao.findFirst(sql, supplierId, supplierNo);
		boolean result = supplier!=null ? true : false;
		return result;
	}
	
	/**
	 * 根绝代理商编号查找ID
	 * @param supplierNo	代理商编号
	 * @return			代理商ID
	 * @author			Sylveon
	 */
	public String getIdByNo(String supplierNo) {
		String sql = "SELECT id FROM t_supplier WHERE no = ?";
		String result = Db.queryStr(sql, supplierNo);
		return result;
	}
	
	/**
	 * 手机是否存在
	 * @param mobile	手机
	 * @return			true 存在, false 不存在
	 * @author Sylveon
	 */
	public boolean existMobile(String mobile) {
		return Db.queryLong("SELECT count(*) FROM t_supplier WHERE mobile = ?", mobile) > 0 ? true : false;
	}
	
	/**
	 * 根据用户ID查找手机号码
	 * @param supplierId	代理商ID
	 * @return			手机号码
	 * @author Sylveon
	 */
	public String getMobileBySupplierId(String supplierId) {
		return Db.queryStr("SELECT mobile FROM t_supplier WHERE id = ?", supplierId);
	}
	
	/**
	 * 根据供货商ID获取编号
	 * @author Sylveon
	 */
	public String getNoBySupplierId(String supplierId) {
		return Db.queryStr("SELECT no FROM t_supplier WHERE id = ?", supplierId);
	}

	
	/**
	 * 现金提取--验证支付密码是否正确
	 * @param payPassword
	 * @param supplierId
	 * @return
	 * @author chenhg
	 * 2016年7月25日 上午10:54:39
	 */
	public boolean checkUserPayPassword(String payPassword, String supplierId) {
		String sql = "SELECT id FROM t_supplier WHERE id = ? AND pay_password = ?";
		String result = Db.queryStr(sql, supplierId, payPassword);
		return StringUtil.notNull(result) ? true : false;
		
	}
}
