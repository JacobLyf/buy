package com.buy.model.product;

import java.math.BigDecimal;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class ProBackSort extends Model<ProBackSort>{
	private static final long serialVersionUID = 6158141935755316649L;
	
	/**
	 * 状态：无效
	 */
	public  static final int STATUS_DISABLE = 0;
	/**
	 * 状态：有效
	 */
	public  static final int STATUS_ENABLE = 1;
	/**
	 * 状态：已删除
	 */
	public  static final int STATUS_DELETE = 2;

	/**
	 * 级别：一级
	 */
	public  static final int FIRST_LEVEL = 1;
	
	/**
	 * 级别：二级
	 */
	public  static final int SECOND_LEVEL = 2;
	
	/**
	 * 级别：三级
	 */
	public  static final int THIRD_LEVEL = 3;
	
	public static final ProBackSort dao = new ProBackSort();
	
	/**
	 * 获取一级分类信息
	 * 
	 * @param sortGroupId
	 *            分类组合Id
	 * @author Chengyb
	 * @return
	 */
	public List<ProBackSort> findFirstGradeSortList(Integer sortGroupId) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append(" t.id,");
	    sql.append(" t.parent_id,");
	    sql.append(" t.`name`,");
	    sql.append(" t.sort_num,");
	    sql.append(" t.logo,");
    	sql.append(" t.`code`");
	    sql.append(" FROM");
	    sql.append(" t_pro_sort t"); // 商品分类表（前台）
	    sql.append(" WHERE");
	    sql.append(" t.parent_id = 0"); // 父级类型id，0表示无父级
	    sql.append(" AND");
	    sql.append(" t.`status` = ?"); // 状态：（0：无效，1：有效，2：已删除）
	    sql.append(" AND");
	    sql.append(" t.is_show = ?"); // 是否前台展示（1： 是，0：否）
	    sql.append(" AND");
	    sql.append(" t.id");
	    sql.append(" IN");
	    sql.append(" (SELECT s.sort_id FROM t_sort_group_map s WHERE s.group_id = ? ORDER BY s.sort_num ASC)");
	    sql.append(" ORDER BY");
	    sql.append(" ISNULL(t.sort_num) ASC, t.sort_num ASC");
	    
		return dao.find(sql.toString(), STATUS_ENABLE, sortGroupId);
	}
	
	/**
	 * 获取二级分类信息
	 * 
	 * @param sortGroupId
	 *            分类组合Id
	 * @author Chengyb
	 * @return
	 */
	public List<ProBackSort> findSecondGradeSortList(Integer parentSortId) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append(" t.id,");
	    sql.append(" t.parent_id,");
	    sql.append(" t.`name`,");
	    sql.append(" t.sort_num,");
	    sql.append(" t.logo,");
    	sql.append(" t.`code`");
	    sql.append(" FROM");
	    sql.append(" t_pro_sort t"); // 商品分类表（前台）
	    sql.append(" WHERE");
	    sql.append(" t.parent_id = ?"); // 父级类型id，0表示无父级
	    sql.append(" AND");
	    sql.append(" t.`status` = ?"); // 状态：（0：无效，1：有效，2：已删除）
	    sql.append(" AND");
	    sql.append(" t.is_show = ?"); // 是否前台展示（1： 是，0：否）
	    sql.append(" ORDER BY");
	    sql.append(" ISNULL(t.sort_num) ASC, t.sort_num ASC");
	    
		return dao.find(sql.toString(), parentSortId, STATUS_ENABLE);
	}
	
	/**
	 * 根据商品ID获取二级后台分类佣金率
	 * @param productId 商品ID
	 * @return
	 * @author Jacob
	 * 2015年12月25日下午5:55:39
	 */
	public BigDecimal getCommissionRate(Integer productId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	a.commission_rate ");
		sql.append(" FROM ");
		sql.append(" 	t_pro_back_sort a ");
		sql.append(" LEFT JOIN t_pro_back_sort b ON b.parent_id = a.id ");
		sql.append(" LEFT JOIN t_product p ON p.sort_id = b.id ");
		sql.append(" WHERE ");
		sql.append(" 	p.id = ? ");
		BigDecimal ommissionRate = Db.queryBigDecimal(sql.toString(),productId);
		//佣金扣点除以100等于佣金率
		ommissionRate = ommissionRate.divide(new BigDecimal("100"));
		return ommissionRate;
	}
	/**
	 * 根据商品ID获取二级后台分类佣金率
	 * @param sortId
	 * @return
	 */
	public BigDecimal getCommissionRateBySortId(Integer sortId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	b.commission_rate ");
		sql.append(" FROM ");
		sql.append(" 	t_pro_back_sort a ");
		sql.append(" LEFT JOIN t_pro_back_sort b ON a.parent_id = b.id ");
		sql.append(" WHERE ");
		sql.append(" 	a.id = ? ");
		BigDecimal ommissionRate = Db.queryBigDecimal(sql.toString(),sortId);
		//佣金扣点除以100等于佣金率
		ommissionRate = ommissionRate.divide(new BigDecimal("100"));
		return ommissionRate;
	}

}
