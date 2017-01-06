package com.buy.model.efun;

import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.product.Product;
import com.buy.model.product.ProductScore;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 幸运一折购商品评分表.
 */
public class EfunProductScore extends Model<EfunProductScore>{
	
	private static final long serialVersionUID = -656892104111509582L;

	public static final EfunProductScore dao = new EfunProductScore();
	
	/*
	 * 是否同步搜索索引（0：否，1：是）
	 */
	public static final String NEED_ASYNC_SEARCH = "0";
	
	/**
	 * 幸运一折购详情访问次数得分(每天自动检测使用).
	 * 
	 * @author Chengyb
	 */
	public void score4ViewsCountAuto() {
		StringBuffer sql = new StringBuffer();
		
		sql.append("UPDATE t_efun_pro_score s, ");
		sql.append(" ( ");
		sql.append("	SELECT ");
		sql.append("		t.product_id, ");
		sql.append("		CASE ");
		sql.append("	WHEN SUM(t.views_count) >= 1000 THEN ");
		sql.append("		4 ");
		sql.append("	WHEN SUM(t.views_count) >= 500 THEN ");
		sql.append("		2 ");
		sql.append("	WHEN SUM(t.views_count) >= 100 THEN ");
		sql.append("		1 ");
		sql.append("	ELSE ");
		sql.append("		0 ");
		sql.append("	END score ");
		sql.append("	FROM ");
		sql.append("		t_efun_product t ");
		sql.append("	GROUP BY ");
		sql.append("		t.product_id ");
		sql.append(") p ");
		sql.append("SET s.visit_times_score = p.score ");
		sql.append("WHERE ");
		sql.append("	s.product_id = p.product_id ");
		
		Db.update(sql.toString());
	}
	
	/**
	 * 参与总数得分（近一个月的参与次数，加分规则见SVN:...\doc\新版E趣商城\需求说明文档\幸运一折购商品默认排序权重的计算.jpg）
	 * @author Jacob
	 */
	public void score4AttendTimes() {
		StringBuffer sql = new StringBuffer();
		
		sql.append(" UPDATE t_efun_score s, ");
		sql.append("  ( ");
		sql.append("	SELECT  ");
		sql.append(" 		es.sku_code, ");
		sql.append(" 		CASE ");
		sql.append(" 	WHEN COUNT(euo.id) >= 1500 THEN 12 ");
		sql.append(" 	WHEN COUNT(euo.id) >= 1000 THEN 10 ");
		sql.append(" 	WHEN COUNT(euo.id) >= 500 THEN 8 ");
		sql.append(" 	WHEN COUNT(euo.id) >= 200 THEN 6 ");
		sql.append(" 	WHEN COUNT(euo.id) >= 100 THEN 4 ");
		sql.append(" 	WHEN COUNT(euo.id) >= 50 THEN 2 ");
		sql.append(" 	ELSE 0 ");
		sql.append(" 	END score ");
		sql.append(" 	FROM ");
		sql.append(" 		t_efun_sku es ");
		sql.append(" 	LEFT JOIN t_efun_user_order euo ON euo.sku_code = es.sku_code ");
		sql.append(" 	WHERE ");
		sql.append(" 		es.is_quit = 0 ");//未退出幸运一折购
		sql.append(" 	AND es.join_time >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) ");
		sql.append(" 	GROUP BY ");
		sql.append(" 		es.sku_code ");
		sql.append(" ) es ");
		sql.append(" SET s.order_times_score = es.score ");
		sql.append(" WHERE ");
		sql.append(" 	s.sku_code = es.sku_code ");
		
		Db.update(sql.toString());
	}
	
	/**
	 * 参与时间得分
	 * 
	 * @author Chengyb
	 */
	public void score4JoinTime() {
		StringBuffer sql = new StringBuffer();
		
		sql.append("UPDATE t_efun_pro_score s, ");
		sql.append(" ( ");
		sql.append("	SELECT ");
		sql.append("		t.product_id, ");
		sql.append("		CASE ");
		sql.append("	WHEN MAX(t.join_time) <= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN ");
		sql.append("		4 ");
		sql.append("	WHEN MAX(t.join_time) <= DATE_SUB(CURDATE(), INTERVAL 15 DAY) THEN ");
		sql.append("		2 ");
		sql.append("	ELSE ");
		sql.append("		0 ");
		sql.append("	END score ");
		sql.append("	FROM ");
		sql.append("		t_efun_sku t ");
		sql.append("	WHERE ");
		sql.append("		t.is_quit = 0 ");
		sql.append("  GROUP BY t.product_id ");
		sql.append(") p ");
		sql.append("SET s.addtime_score = p.score ");
		sql.append("WHERE ");
		sql.append("	s.product_id = p.product_id ");
		
		Db.update(sql.toString());
	}
	
	/**
	 * 首次加入幸运一折购新品加分
	 * 
	 * @author Chengyb
	 */
	public void score4NewEfun() {
		StringBuffer sql = new StringBuffer();

		sql.append("UPDATE t_efun_pro_score s, ");
		sql.append(" ( ");
		sql.append("	SELECT ");
		sql.append("		t.product_id, ");
		sql.append("		CASE ");
		sql.append("	WHEN MIN(t.join_time) <= DATE_SUB(CURDATE(), INTERVAL 3 DAY) THEN ");
		sql.append("		12 ");
		sql.append("	WHEN MIN(t.join_time) > DATE_SUB(CURDATE(), INTERVAL 3 DAY) && MIN(t.join_time) <= DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN ");
		sql.append("		10 ");
		sql.append("	WHEN MIN(t.join_time) > DATE_SUB(CURDATE(), INTERVAL 7 DAY) && MIN(t.join_time) <= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN ");
		sql.append("		5 ");
		sql.append("	ELSE ");
		sql.append("		0 ");
	    sql.append("	END score ");
	    sql.append("	FROM ");
	    sql.append("		t_efun_sku t ");
	    sql.append("	WHERE ");
	    sql.append("		t.is_quit = 0 ");
	    sql.append("	AND t.is_first = 1 ");
	    sql.append("	GROUP BY ");
	    sql.append("		t.product_id ");
	    sql.append(") p ");
	    sql.append("SET s.new_efun_score = p.score ");
	    sql.append("WHERE ");
	    sql.append("	s.product_id = p.product_id ");
		
		Db.update(sql.toString());
	}
	
	/**
	 * 幸运一折购详情访问次数得分(重新加入幸运一折购商品初始化使用)
	 * 
	 * @param productId
	 * @author Chengyb
	 */
	public void score4ViewsCount(Integer productId) {
		// 获取幸运一折购商品访问次数.
		Integer viewsCount = EfunProduct.dao.getViewsCount(productId);
		EfunProductScore efunProductScore = findById(productId);
		if (viewsCount >= 1000) {
			efunProductScore.set("visit_times_score", 4);
			efunProductScore.update();
		} else if (viewsCount >= 500) {
			efunProductScore.set("visit_times_score", 2);
			efunProductScore.update();
		} else if (viewsCount >= 100) {
			efunProductScore.set("visit_times_score", 1);
			efunProductScore.update();
		} else {

		}
	}
	
	/**
	 * 批量初始化幸运一折购得分
	 * 
	 * @param skuCodes
	 *            Integer[] 商品Id数组
	 * @return
	 * @author Chengyb
	 */
	public void batchInit(Integer[] productIds) {
		if (null != productIds && productIds.length > 0) {
			List<Object> params = new ArrayList<Object>();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append(" INSERT INTO t_efun_pro_score ");
			sql.append(" ( ");
			sql.append(" 	product_id ");
			sql.append(" ) ");
			sql.append(" VALUES ");
			// 标识新增幸运一折购得分条数.
			int num = 0;
			for (Integer productId : productIds) {
				if (null == getEfunScoreByProductId(productId)) {
					sql.append(" 	( ");
					sql.append(" ? ");
					sql.append(" 	),");
					params.add(productId);
					num++;
				}
			}
			// 新增条数大于0才执行插入.
			if (num > 0)
				Db.update(sql.toString().substring(0, sql.toString().length() - 1), params.toArray());
			
			// 商品信息完整度评分.
			score4ProductInfoIntegrity(productIds);
			
			// e趣自营得分.
			score4SelfEfun(productIds);
			
			// 进驻O2O商品SKU得分.
			score4O2oSku(productIds);
		}
	}
	
	/**
	 * 根据商品Id获取幸运一折购商品得分.
	 * 
	 * @param productId
	 * @return
	 * @author Chengyb
	 */
	public EfunProductScore getEfunScoreByProductId(Integer productId) {
		String sql = "SELECT * FROM t_efun_pro_score WHERE product_id = ?";
		return dao.findFirst(sql, productId);
	}
	
	/**
	 * 商品信息完整度加分
	 * 
	 * @param productIds
	 * @author Chengyb
	 */
	public void score4ProductInfoIntegrity(Integer[] productIds) {
		if(null != productIds && productIds.length > 0) {
			for (int i = 0; i < productIds.length; i++) {
				Integer productId = productIds[i];
				
				EfunProductScore efunProductScore = getEfunScoreByProductId(productId);
				// 获取相应的商品得分记录.
				ProductScore productScore = ProductScore.dao.findByIdLoadColumns(productId, "data_integrity_score");
				// 直接使用商品信息完整度得分的分值.
				efunProductScore.set("data_integrity_score",
						productScore!=null ? productScore.getInt("data_integrity_score") : 0);
				efunProductScore.update();
			}
		}
	}
	
	/**
	 * e趣自营得分.
	 * 
	 * @param productIds
	 *            商品Id数组
	 * @author Chengyb
	 */
	public void score4SelfEfun(Integer[] productIds) {
		if (null != productIds && productIds.length > 0) {
			List<Object> params = new ArrayList<Object>();
			StringBuffer sql = new StringBuffer();
				
			sql.append("UPDATE t_efun_pro_score m,");
			sql.append("	(");
			sql.append("	SELECT");
			sql.append("		s.product_id,");
			sql.append("	    IF (t.source = " + Product.SOURCE_SELF_EXCLUSIVE + " OR t.source = " + Product.SOURCE_SELF_PUBLIC + ", 8, 0) score");
			sql.append("	FROM");
			sql.append("		t_pro_sku s");
			sql.append("	LEFT JOIN t_product t ON s.product_id = t.id");
			sql.append("	WHERE");
			sql.append("		s.product_id IN (");
				
			for (Integer productId : productIds) {
				sql.append(" 	?,");
				params.add(productId);
			}
			sql = sql.deleteCharAt(sql.length() - 1);
			sql.append("		)");
				
			sql.append(") n ");
			sql.append("SET m.self_efun_score = n.score ");
			sql.append("WHERE ");
			sql.append("	m.product_id = n.product_id");
				
			Db.update(sql.toString(), params.toArray());
		}
	}
	
	/**
	 * 进驻O2O商品SKU得分
	 * 
	 * @param productIds
	 *            Integer[] 商品Id数组
	 * @author Chengyb
	 */
	public void score4O2oSku(Integer[] productIds){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		
		sql.append("UPDATE t_efun_pro_score s, ");
		sql.append(" v_product_count_solr t ");
		sql.append("SET s.o2o_sku_score = 6 ");
		sql.append("WHERE ");
		sql.append("	s.product_id = t.product_id ");
		sql.append("AND t.isO2o = " + BaseConstants.YES + " ");
		sql.append("AND s.product_id IN (");
		
		for(Integer productId : productIds){
			sql.append(" 	?,");
			params.add(productId);
		}
		sql = sql.deleteCharAt(sql.length()-1);
		sql.append(")");
		
		Db.update(sql.toString(),params.toArray());
	}
	
	/**
	 * 获取需要更新索引的幸运一折购列表.
	 * 
	 * @author Chengyb
	 */
	public Page<Record> fetchUpdateEfun(Integer pageNumber, Integer pageSize) {
		return Db.paginate(pageNumber, pageSize, "SELECT product_id ", "FROM t_efun_pro_score t WHERE t.is_async_search = ?", NEED_ASYNC_SEARCH);
	}
	
}
