package com.buy.model.sort;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class SortGroup extends Model<SortGroup>{
	private static final long serialVersionUID = 9006195786215586611L;
	
	/**
	 * 类型：PC
	 */
	public  static final int PC_GROUP_TYPE = 1;
	/**
	 * 类型：APP
	 */
	public  static final int APP_GROUP_TYPE = 2;
	/**
	 * 类型：PC精选
	 */
	public  static final int PC_SIFT_GROUP_TYPE = 3;
	/**
	 * 类型：APP精选
	 */
	public  static final int APP_SIFT_GROUP_TYPE = 4;
	
	public static final SortGroup dao = new SortGroup();
	
	/**
	 * 获取分类组合列表
	 * 
	 * @param type
	 *            类型（1：PC，2：APP, 3：PC精选，4：APP精选）
	 * @return
	 */
	public List<SortGroup> getSortGroupList(Integer type) {
		StringBuffer sql = new StringBuffer("SELECT * FROM t_sort_group");
		if(null != type) {
			sql.append(" WHERE type = ").append(type);
		}
		sql.append(" ORDER BY ISNULL(type) ASC, type ASC, ISNULL(sort_num) ASC, sort_num ASC");
		return dao.find(sql.toString());
	}
	
	/**
	 * 获取分类组合下的广告信息.
	 * @param sortGroupId
	 *            分类组合Id
	 * @param sortGrouptype
	 *            分类组合类型
	 * @return
	 */
	public List<Record> findSortGroupAd(Integer sortGroupId, Integer sortGrouptype) {
		StringBuffer sql = new StringBuffer("SELECT");
		if (sortGrouptype == PC_GROUP_TYPE || sortGrouptype == PC_SIFT_GROUP_TYPE) { // 表【t_ad】
			sql.append(" q.url,");
		} else if (sortGrouptype == APP_GROUP_TYPE || sortGrouptype == APP_SIFT_GROUP_TYPE) { // 表【t_ad_app】
			//sql.append(" t_ad_app q");
		}
		sql.append(" q.img_path FROM t_sort_group t LEFT JOIN t_expand_sort o ON t.id = o.sort_groub_id LEFT JOIN t_expand_sort_detail p ON o.id = p.expand_id LEFT JOIN ");
		if (sortGrouptype == PC_GROUP_TYPE || sortGrouptype == PC_SIFT_GROUP_TYPE) { // 表【t_ad】
			sql.append(" t_ad q");
		} else if (sortGrouptype == APP_GROUP_TYPE || sortGrouptype == APP_SIFT_GROUP_TYPE) { // 表【t_ad_app】
			sql.append(" t_ad_app q");
		}
		sql.append(" ON p.target_id = q.id WHERE t.type = ? AND o.sort_groub_id = ? AND p.type = 3 ORDER BY ISNULL(q.sort_num) ASC, q.sort_num ASC");
		return Db.find(sql.toString(), sortGrouptype, sortGroupId);
	}

}