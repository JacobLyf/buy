package com.buy.model.product;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 前台商品分类.
 * 
 * @author Chengyb
 *
 */
public class ProductFrontSort extends Model<ProductFrontSort>{
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
	 * 类型：PC
	 */
	public  static final int TYPE_PC = 1;
	
	/**
	 * 类型：App
	 */
	public  static final int TYPE_APP = 2;
	
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
	
	public static final ProductFrontSort dao = new ProductFrontSort();

	/**
	 * 获取前台第N级分类列表.
	 * 
	 * @param type
	 *            PC/App
	 * @param level
	 *            级别
	 * @param parentId
	 *            父级Id
	 * @author Chengyb
	 * @return
	 */
	public List<ProductFrontSort> findLevelList(Integer type, Integer level, Integer parentId) {
		List<Integer> params = new ArrayList<Integer>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append(" t.id,");
	    sql.append(" t.parent_id,");
	    sql.append(" t.level,");
	    sql.append(" t.`name`,");
	    sql.append(" t.sort_num,");
	    sql.append(" t.logo,");
    	sql.append(" t.`code`,");
    	sql.append(" t.`status`");
	    sql.append(" FROM");
	    sql.append(" t_pro_front_sort t"); // 商品分类表（前台）
	    sql.append(" WHERE");
	    
		if (null != parentId) {
			sql.append(" t.parent_id = ?"); // 父级类型id，0表示无父级
			sql.append(" AND");
			
			params.add(parentId);
		}
		
	    sql.append(" type = ?");
	    sql.append(" AND");
	    sql.append(" level = ?"); // 一级
	    sql.append(" AND");
	    sql.append(" status = ");
	    sql.append(ProductFrontSort.STATUS_ENABLE);
	    sql.append(" ORDER BY");
	    sql.append(" ISNULL(t.sort_num) ASC, t.sort_num ASC");
	    
	    params.add(type);
	    params.add(level);
	    
		return dao.find(sql.toString(), params.toArray());
	}

	/**
	 * 获取前台三级分类对应的Facet Field数据.
	 * 
	 * @param type
	 *            PC/App
	 * @param level
	 *            级别
	 * @author Chengyb
	 * @return
	 */
	public List<Record> findBackSortSearchPropertyList(Integer type, Integer level) {
		StringBuffer sql = new StringBuffer();

		sql.append(" SELECT ");
		sql.append(" s.id AS front_id,");
		sql.append(" s.`name` AS front_name,");
		sql.append(" p.`name` AS property_name,");
		sql.append(" p.id AS property_id");
		sql.append(" FROM ");
		sql.append(" t_pro_front_sort s");
		sql.append(" LEFT JOIN ");
		sql.append(" t_front_back_sort_map t");
		sql.append(" ON s.id = t.front_id");
		sql.append(" LEFT JOIN ");
		sql.append(" t_pro_property p");
		sql.append(" ON ");
		sql.append(" t.back_id = p.sort_id");
		sql.append(" WHERE ");
		sql.append(" s.type = ?");
		sql.append(" AND ");
		sql.append(" s.level = ?");
		sql.append(" AND ");
		sql.append(" p.is_search = 1");

		return Db.find(sql.toString(), type, level);
	}
	
	/**
	 * 获取PC/App前台三级分类对应的后台三级分类数据.
	 * 
	 * @param type
	 *            PC/App
	 * @param level
	 *            级别
	 * @author Chengyb
	 * @return
	 */
	public List<Record> findBackSortList(Integer type, Integer firstFrontSortId, Integer secondFrontSortId,
			Integer thirdFrontSortId) {
		List<Integer> params = new ArrayList<Integer>();
		
		StringBuffer sql = new StringBuffer();

		sql.append(" SELECT id"); // 后台三级分类Id.
		sql.append(" FROM v_web_sort_mapping");
		sql.append(" WHERE");
		sql.append(" type = ?"); // 类型【1：PC，2：App】
		
		params.add(type);
		
		if(null != firstFrontSortId) { // 一级分类Id.
			sql.append(" AND");
			sql.append(" first_front_id = ?");
			
			params.add(firstFrontSortId);
		}
		
		if(null != secondFrontSortId) { // 二级分类Id.
			sql.append(" AND");
			sql.append(" second_front_id = ?");
			
			params.add(secondFrontSortId);
		}
		
		if(null != thirdFrontSortId) { // 三级分类Id.
			sql.append(" AND");
			sql.append(" third_front_id = ?");
			
			params.add(thirdFrontSortId);
		}

		return Db.find(sql.toString(), params.toArray());
	}
	
	/**
	 * 根据Id获取前台分类名称
	 * @param id
	 * @return
	 * @author Jacob
	 * 2016年8月20日下午2:48:43
	 */
	public String getName(Integer id){
		return dao.findByIdLoadColumns(id, "name").getStr("name");
	}

}