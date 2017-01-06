package com.buy.model.product;

import com.jfinal.plugin.activerecord.Model;

/** 
 * @author wangy
 * @date 2015年9月30日 下午1:20:06 
 */
public class ProductSignboard extends Model<ProductSignboard> {

	/**
	 * 商品标识
	 */
	private static final long serialVersionUID = 1L;
	public static final ProductSignboard dao = new ProductSignboard();
	
	/**
	 * 判断标识名是否已存在
	 * @param name	标志名称
	 * @return
	 * boolean	返回true表示已存在
	 * @author wangy
	 * @date 2015年10月9日 下午1:44:22
	 */
	public boolean nameExist(String name){
		String sql = "SELECT id FROM t_pro_signboard WHERE name=?";
		ProductSignboard productSignboard = findFirst(sql,name);
		return null!=productSignboard;
	}

	/**
	 * 判断标志编号是否存在
	 * @param no	标志编号
	 * @return
	 * boolean	返回true表示标志编号已存在
	 * @author wangy
	 * @date 2015年10月10日 上午10:15:07
	 */
	public boolean noExist(String no){
		String sql = "SELECT id FROM t_pro_signboard WHERE no=?";
		ProductSignboard productSignboard = findFirst(sql,no);
		return null!=productSignboard;
	}
	
}
