package com.buy.model.product;

import java.util.List;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 前台商品分类映射.
 * 
 * @author Chengyb
 *
 */
public class ProductFrontSortMap extends Model<ProductFrontSortMap>{
	private static final long serialVersionUID = 6158141935755316649L;
	
	/**
	 * 类型：PC
	 */
	public  static final int TYPE_PC = 1;
	
	/**
	 * 类型：App
	 */
	public  static final int TYPE_APP = 2;
	
	public static final ProductFrontSortMap dao = new ProductFrontSortMap();

	/**
	 * 获取前后台分类映射列表.
	 * 
	 * @param type
	 *            PC/App
	 * @author Chengyb
	 * @return
	 */
//	public List<Record> findAll(Integer type) {
//		StringBuffer sql = new StringBuffer();
//		sql.append("SELECT ");
//		sql.append(" s.front_id,");
//	    sql.append(" s.back_id");
//	    sql.append(" FROM");
//	    sql.append(" t_front_back_sort_map s"); // 前台分类后台分类映射表
//	    sql.append(" LEFT JION");
//	    sql.append(" t_pro_front_sort t"); // 前台分类表
//	    sql.append(" ON s.front_id = t.id");
//	    sql.append(" WHERE");
//	    sql.append(" t.type = ?");
//	    
//		return dao.find(sql.toString(), type);
//	}

}
