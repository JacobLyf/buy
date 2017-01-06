package com.buy.model.product;

import java.util.ArrayList;
import java.util.List;

import com.buy.model.img.Image;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 商品评价图片 
 */
public class ProEvaluateImg extends Model<ProEvaluateImg> {

	private static final long serialVersionUID = 1L;
	
	public static ProEvaluateImg dao = new ProEvaluateImg();
	
	/**
	 * 根据商品评论获取商品评论图片路径
	 * @param proEvaId
	 * @return
	 * @author Sylveon
	 */
	public List<String> findImgsByProEvaImgId(String proEvaId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append("	img_path imgPath");
		sql.append(" FROM");
		sql.append(" t_pro_evaluate_img");
		sql.append(" WHERE pro_eval_id = ?");
		sql.append(" ORDER BY create_time ASC");
		return Db.query(sql.toString(), proEvaId);
	}

	/**
	 * 商品评价图片批量添加
	 */
	public void addImgMany(List<ProEvaluateImg> addImgList) {
		// 评论图片添加
		List<String> sqlList2 = new ArrayList<String>();
		for (ProEvaluateImg pei : addImgList) {
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO t_pro_evaluate_img");
			sql.append("	(pro_eval_id, img_path, create_time)");
			sql.append(" VALUES ( ");
			sql.append("	'" + pei.getStr("pro_eval_id")	+ "', ");
			sql.append("	'" + pei.getStr("img_path")		+ "', ");
			sql.append("	NOW() ");
			sql.append(" )");
			sqlList2.add(sql.toString());
			//图片有效
			Image.dao.enable(pei.getStr("img_path"));
		}
		Db.batch(sqlList2, 50);
	}
}
