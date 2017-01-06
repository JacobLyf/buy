package com.buy.model.extendSort;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ExtendSort extends Model<ExtendSort>{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态：无效
	 */
	public  static final int STATUS_DISABLE = 0;
	/**
	 * 状态：有效
	 */
	public  static final int STATUS_ENABLE = 1;
	
	/**
	 * 类型：分类
	 */
	public  static final int SORT_TYPE = 1;
	/**
	 * 类型：品牌
	 */
	public  static final int BRAND_TYPE = 2;
	/**
	 * 类型：广告
	 */
	public  static final int AD_TYPE = 3;

	public static final ExtendSort dao = new ExtendSort();
	
	/**
	 * 获取分类组合下的扩展分类的品牌.
	 * 
	 * @param sortGroupId
	 *            分类组合Id
	 * @author Chengyb
	 * @return 
	 */
	public List<Record> findSortGroupBrand(Integer sortGroupId) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append(" q.*");
	    sql.append(" FROM");
	    sql.append(" t_expand_sort o"); // 扩展分类表
	    sql.append(" LEFT JOIN t_expand_sort_detail p"); // 扩展分类明细表.
	    sql.append(" ON");
	    sql.append(" o.id = p.expand_id");
	    sql.append(" LEFT JOIN t_pro_brand q"); // 品牌表.
	    sql.append(" ON");
	    sql.append(" p.target_id = q.id");
	    sql.append(" WHERE");
	    sql.append(" o.sort_groub_id = ?"); // 分类组合Id.
	    sql.append(" AND");
	    sql.append(" o.type = ?"); // 类型：（1：分类，2：品牌，3：广告）
	    sql.append(" AND");
	    sql.append(" o.status = ?"); // 状态（0： 无效，1：有效）
	    sql.append(" ORDER BY");
	    sql.append(" ISNULL(o.sort_num) ASC, o.sort_num ASC");
	    
		return Db.find(sql.toString(), sortGroupId, BRAND_TYPE, STATUS_ENABLE);
	}

}
