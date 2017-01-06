package com.buy.model.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.order.Order;
import com.buy.model.product.Product;
import com.buy.model.product.ProductScore;
import com.buy.model.product.ProductSku;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class ShopScore extends Model<ShopScore>{

	private static final long serialVersionUID = 1L;
	
	public final static ShopScore dao = new ShopScore();
	
	/*
	 * 是否同步搜索索引（0：否，1：是）
	 */
	public static final String NEED_ASYNC_SEARCH = "0";
	
	/**
	 * 新开店评分
	 * @param shopId
	 * @author Sylveon
	 */
	public void updateOpenLengthScore(String shopId) {
		new ShopScore().set("shop_id", shopId).set("new_shop_score", 15).update();	// 新开 +15
	}
	
	/**
	 * 新开店评分（定时） 
	 * @author Sylveon
	 */
	public void updateNewShopScoreAuto() {
		// 查询新开店
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT a.shopId, a.days, a.score FROM ( ");
		sql.append(	" SELECT ");
		sql.append(		" s.id shopId, ");
		sql.append(		" TIMESTAMPDIFF(DAY, acvtivate_time, NOW()) days, ");
		sql.append(		" ss.new_shop_score score ");
		sql.append(	" FROM t_shop s, t_shop_score ss ");
		sql.append(	" WHERE s.id = ss.shop_id AND s.status = " + Shop.STATUS_ACTIVATED);
		sql.append(" ) a WHERE a.days >= 0");
		List<Record> records = Db.find(sql.toString());
		
		// 处理评分SQL
		List<String> updateSqlList = new ArrayList<String>();
		for (Record r : records) {
			StringBuffer updateSql = new StringBuffer("UPDATE t_shop_score ");
			
			Long 	days 	= r.getLong("days");	days	= null == days ? 0 : days;
			Integer score 	= r.getInt("score");	score	= null == score ? 0 : score;
			
			// +10
			if (days > 3 && days <= 7 && score != 10)
				updateSql.append("SET new_shop_score = 10, is_async_search = " + BaseConstants.NO);
			
			// +5
			else if (days > 7 && days <= 30 && score != 5)
				updateSql.append("SET new_shop_score = 5, is_async_search = " + BaseConstants.NO);
			
			// +0
			else if (days > 30 && score != 0)
				updateSql.append("SET new_shop_score = 0, is_async_search = " + BaseConstants.NO);
			
			// other
			else
				continue;
			
			String shopId = r.getStr("shopId");
			updateSql.append(" WHERE shop_id = '" + shopId + "'");
			updateSqlList.add(updateSql.toString());
		}
		
		// 评分
		if (updateSqlList.size() > 0)
			Db.batch(updateSqlList, 50);
	}
	
	/**
	 * 开店时长评分（定时） 
	 * @author Sylveon
	 */
	public void updateOpenLengthScoreAuto() {
		// 查询店铺时长
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT a.shopId, a.months, a.score FROM (");
		sql.append(	" SELECT ");
		sql.append(		" s.id shopId, ");
		sql.append(		" TIMESTAMPDIFF(DAY, acvtivate_time, NOW()) months, ");
		sql.append(		" ss.open_duration_score score ");
		sql.append(	" FROM t_shop s, t_shop_score ss ");
		sql.append(	" WHERE s.id = ss.shop_id AND s.status = " + Shop.STATUS_ACTIVATED);
		sql.append(" ) a WHERE a.months >= 0");
		List<Record> records = Db.find(sql.toString());
		
		// 处理评分SQL
		List<String> sqlList = new ArrayList<String>();
		for (Record r : records) {
			StringBuffer updateSql = new StringBuffer("UPDATE t_shop_score ");
			
			Long 	months 	= r.getLong("months");	months	= null == months ? 0 : months;
			Integer score 	= r.getInt("score");	score	= null == score ? 0 : score;
			
			// +2
			if (months > 4 && score != 2)
				updateSql.append("SET open_duration_score = 2, is_async_search = " + BaseConstants.NO);
				
			// other
			else
				continue;
			
			String shopId = r.getStr("shopId");
			updateSql.append(" WHERE shop_id = '" + shopId + "'");
			sqlList.add(updateSql.toString());
		}
			
		// 评分
		if (sqlList.size() > 0)
			Db.batch(sqlList, 50);
	}
	
	/**
	 * 已上架商品数评分
	 * @param shopId
	 * @author Sylveon
	 */
	public void updateProReleaseScore(String shopId) {
		if (StringUtil.isNull(shopId))
			return;
		// 查询店铺已上架商品数
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	COUNT(1)");
		sql.append(" FROM t_pro_sku a");
		sql.append(" LEFT JOIN t_product b ON b.id = a.product_id");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND b.shop_id = ?");
		sql.append(" AND b.status = ?");
		long count = Db.queryLong(sql.toString(), shopId, Product.STATUS_SHELVE);
		
		// 评分
		int score = 0;
		if (count >= 10 && count < 20)			score = 2;	// 已上架商品 +2
		else if (count >= 20 && count < 50)		score = 4;	// 已上架商品 +4
		else if (count >= 50 && count < 100)	score = 6;	// 已上架商品 +6
		else if (count >= 100)					score = 8;	// 已上架商品 +8
		new ShopScore().set("shop_id", shopId).set("release_products_score", score).update();
	}
	
	/**
	 * 近一个月总销量评分	（定时）
	 * @param shopId
	 * @author Sylveon
	 */
	public void updateLastMonthSaleScoreAuto() {
		// 查询近一个月总销量
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT s.id shopId, o.saleCount , ss.sales_score score FROM ( ");
		sql.append("	SELECT merchant_id shopId, COUNT(1) saleCount FROM t_order");
		sql.append("	WHERE status BETWEEN " + Order.STATUS_WAIT_FOR_EVALUATION + " AND " + Order.STATUS_HAD_EVALUATION);
		sql.append(" 	AND trade_status = " + Order.TRADE_NORMAL);
		sql.append(" 	AND order_type IN (" + Order.TYPE_SHOP + "," + Order.TYPE_SELF_SHOP + ")");
		sql.append("	AND comfirm_time BETWEEN DATE_SUB(NOW(), INTERVAL 1 MONTH) AND NOW()");
		sql.append("	GROUP BY merchant_id");
		sql.append(" ) o, t_shop s, t_shop_score ss");
		sql.append(" WHERE o.shopId = s.id AND s.id = ss.shop_id AND o.saleCount > 0 AND s.status = " + Shop.STATUS_ACTIVATED);
		List<Record> sales = Db.find(sql.toString());
		
		// 处理评分SQL
		List<String> sqlList = new ArrayList<String>();
		for(Record s : sales) {
			StringBuffer updateSql = new StringBuffer("UPDATE t_shop_score");
			
			long count = s.getLong("saleCount");
			int score = s.getInt("score");
			
			// +2
			if (count >= 50 && count < 100 && score != 2)
				updateSql.append(" SET sales_score = 2, is_async_search = " + BaseConstants.NO);
			
			// +4
			else if (count >= 100 && count < 300 && score != 4)
				updateSql.append(" SET sales_score = 4, is_async_search = " + BaseConstants.NO);
			
			// +6
			else if (count >= 300 && count < 500 && score != 6)
				updateSql.append(" SET sales_score = 6, is_async_search = " + BaseConstants.NO);
			
			// +8
			else if (count >= 500 && score != 8)
				updateSql.append(" SET sales_score = 8, is_async_search = " + BaseConstants.NO);
			
			// other
			else
				continue;
			
			String shopId = s.getStr("shopId");
			updateSql.append(" WHERE shop_id = '" + shopId + "'");
			sqlList.add(updateSql.toString());
		}
		
		// 评分
		if (sqlList.size() > 0)
			Db.batch(sqlList, 50);
	}
	
	/**
	 * 店铺收藏次数评分
	 * @param shopId
	 * @author Sylveon
	 */
	public void updateShopFavScore(String shopId) {
		// 查询店铺收藏次数
		long count = Db.queryLong("SELECT COUNT(1) FROM t_shop_favs WHERE shop_id = ?", shopId);
		
		// 评分
		int score = 0;
		if (count >= 50 && count <200)			score = 1;	// 店铺收藏次数 +1
		else if (count >= 200 && count <500)	score = 3;	// 店铺收藏次数 +3
		else if (count >= 500)					score = 5;	// 店铺收藏次数 +5
		new ShopScore().set("shop_id", shopId).set("collect_count_score", score).update();
	}
	
	/**
	 * 店铺好评率评分
	 * @param orderId
	 * @author Sylveon
	 */
	public Record updateReputablyScore(String orderId) {
		// 查询店铺好评率
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	c.shopId,");
		sql.append(" 	count(1) well");
		sql.append(" FROM t_pro_evaluate a");
		sql.append(" LEFT JOIN t_order_detail b ON b.id = a.order_detail_id");
		sql.append(" LEFT JOIN v_web_order_shop_list c ON c.orderId = b.order_id");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND c.orderId = ?");
		Record totalRecord = Db.findFirst(sql.toString(), orderId);
		sql.append(" AND praise_radio >= 4");
		Record wellRecord = Db.findFirst(sql.toString(), orderId);
		
		// 评分
		String shopId = totalRecord.getStr("shopId");	// 店铺ID
		long total = totalRecord.getLong("well");		// 总评论数
		long well = wellRecord.getLong("well");			// 好评数
		double reputably = 0;
		if (total != 0 && well != 0) {
			BigDecimal bd = new BigDecimal((well * 1.00D) / (total * 1.00D) * 100.00D);
			reputably = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		int score = 0;
		if (reputably < 60)							score = -1;	// 店铺好评率 -1
		else if (reputably >= 80 && reputably < 90)	score = 1;	// 店铺好评率 +1
		else if (reputably >= 90 && reputably < 95)	score = 3;	// 店铺好评率 +3
		else if (reputably >= 95)					score = 5;	// 店铺好评率 +5
		new ShopScore().set("shop_id", shopId).set("reputably_score", score).update();
		
		// 返回店铺ID和好评率
		return new Record().set("shopId", shopId).set("reputably", reputably);
	}
	
	/**
	 * 保证金评分
	 * @param shopId
	 * @return
	 * @author Sylveon
	 */
	public void updateDepositScore(String shopId) {
		// 查询保证金
		String sql = "SELECT is_deposit FROM t_shop WHERE id = ?";
		int flag = Db.queryInt(sql, shopId);
		
		// 评分
		int score = 0;
		int productScore = 0;
		// 保证金评分 +12
		if(flag == Shop.DEPOSIT_YES){
			score = 12;
			productScore = 5;
		}
		new ShopScore().set("shop_id", shopId).set("deposit_score", score).update();
		//更新商品评分
		ProductScore.dao.updateDepositSore(shopId, productScore);
	}
	
	/**
	 * 店铺认证评分
	 * @param shopId
	 * @return
	 * @author Sylveon
	 */
	public Boolean updateCertificationScore(String shopId) {
		// 查询店铺认证
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append("	is_certification,");
		sql.append(" 	is_return_certification,");
		sql.append(" 	is_communication,");
		sql.append(" 	is_express_services,");
		sql.append(" 	is_traffic_safety,");
		sql.append(" 	is_responsibility,");
		sql.append(" 	is_peishi");
		sql.append(" FROM t_shop");
		sql.append(" WHERE id = ?");
		Record cer = Db.findFirst(sql.toString(), shopId);
		
		// 计算分数
		int score = 0;
		int productScore = 0;
		// 店铺认证 - 实名认证 +2
		if (1 == cer.getInt("is_certification")){		
			score += 2;	
			productScore += 2;	
		}
		// 店铺认证 - 七天退货 +2
		if (1 == cer.getInt("is_return_certification")){
			score += 2;	
			productScore += 2;	
		}
		// 店铺认证 - 通讯保障 +2
		if (1 == cer.getInt("is_communication")){
			score += 2;	
			productScore += 2;	
		}
		// 店铺认证 - 快速发货 +2
		if (1 == cer.getInt("is_express_services")){
			score += 2;	
			productScore += 2;	
		}
		// 店铺认证 - 货运安全及包装 + 2
		if (1 == cer.getInt("is_traffic_safety")){
			score += 2;	
			productScore += 2;	
		}
		// 店铺认证 - 卖家义务及违规处理 +2
		if (1 == cer.getInt("is_responsibility")){
			score += 2;	
			productScore += 2;	
		}
		// 店铺认证 - 假一赔十 +8
		if (1 == cer.getInt("is_peishi")){
			score += 8;	
		}
		// 评分
		new ShopScore().set("shop_id", shopId).set("shop_certification_score", score).update();
		//更新商品评分
		return ProductScore.dao.updateCertificationScore(shopId, productScore);
	}
	
	/**
	 * 进驻云店评分
	 * @author Sylveon
	 */
	public void updateEnterO2oScoreAuto() {
		// 查询店铺是否有商品加入云店
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT a.shopId, a.score, a.isO2o FROM ( ");
		sql.append(		"SELECT s.id shopId, ss.enter_o2o_score score, IFNULL(isO2o, 0) isO2o FROM t_shop s ");
		sql.append(		"LEFT JOIN t_shop_score ss ON s.id = ss.shop_id ");
		sql.append(		"LEFT JOIN (SELECT p.shop_id shopId, COUNT(1) isO2o FROM t_o2o_sku_map osm LEFT JOIN t_product p ON p.id = osm.product_id WHERE p.shop_id IS NOT NULL GROUP BY p.shop_id) t ON s.id = t.shopId ");
		sql.append(		"WHERE s.status = " + Shop.STATUS_ACTIVATED + " AND s.forbidden_status = " + Shop.FORBIDDEN_STATUS_NORMAL + " ");
		sql.append(") a ");
		sql.append("WHERE ((a.isO2o > 0 AND a.score = 0) OR (a.isO2o = 0 AND a.score > 0))");
		List<Record> recordList = Db.find(sql.toString());

		// 处理评分SQL
		List<String> sqlList = new ArrayList<String>();
		for (Record r : recordList) {
			StringBuffer updateSql = new StringBuffer("UPDATE t_shop_score");
			
			long isO2o = r.getLong("isO2o");
			int score = r.getInt("score");
			
			// +4
			if (isO2o > 0 && score == 0)
				updateSql.append(" SET enter_o2o_score = 4, is_async_search = " + BaseConstants.NO);
			
			// +0
			else if (isO2o == 0 && score > 0) {
				updateSql.append(" SET enter_o2o_score = 0, is_async_search = " + BaseConstants.NO);
			}
			
			// other
			else
				continue;
			
			String shopId = r.getStr("shopId");
			updateSql.append(" WHERE shop_id = '" + shopId + "'");
			sqlList.add(updateSql.toString());
		}
		
		// 评分
		if (sqlList.size() > 0)
			Db.batch(sqlList, 50);
	}
	
	/**
	 * 自营店评分
	 * @param shopId
	 * @param isBelongEfun
	 * @author Sylveon
	 */
	public void updateSelfScore(String shopId, int isBelongEfun) {
		int score = Shop.BELONG_EFUN == isBelongEfun ? 4 : 0;	// 自营店评分 + 4
		new ShopScore().set("shop_id", shopId).set("self_shop_score", score).update();
	}
	
	/**
	 * 支持幸运一折购评分
	 * @param shopId
	 * @author Sylveon
	 */
	public void updateEfunScore(String shopId) {
		if (StringUtil.isNull(shopId))
			return;
		// 查询是否支持幸运一折购
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	COUNT(1) FROM t_pro_sku a");
		sql.append(" LEFT JOIN t_product b ON b.id = a.product_id");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND b.shop_id = ?");
		sql.append(" AND a.is_efun = ?");
		long count = Db.queryLong(sql.toString(), shopId, ProductSku.IS_EFUN);
		
		// 评分
		int score = count > 0 ? 4 : 0;	// 支持幸运一折购评分 + 4
		new ShopScore().set("shop_id", shopId).set("support_efun_score", score).update();
	}
	
	/**
	 * 支持幸运一折购评分（根据商品sku的code）
	 * @param skuCodes	sku的code数组
	 * @author Sylveon
	 */
	public void updateEfunScoreBySkuCodes(String[] skuCodes) {
		// 查询店铺ID集合
		String codesStr = StringUtil.arrayToStringForSql(",", skuCodes);
		StringBuffer search = new StringBuffer();
		search.append(" SELECT");
		search.append(" 	b.shop_id shopId");
		search.append(" FROM t_pro_sku a");
		search.append(" LEFT JOIN t_product b ON b.id = a.product_id");
		search.append(" WHERE a.code IN (" + codesStr + ") ");
		List<String> shopIds = Db.query(search.toString());
		// 评分 - 支持幸运一折购评分 + 4
		if (StringUtil.notNull(shopIds)) {
			String shopIdsStr = StringUtil.listToStringForSql(",", shopIds);
			String update = "UPDATE t_shop_score SET support_efun_score = 4 WHERE shop_id IN (" + shopIdsStr + ")";
			Db.update(update);
		}
	}
	
	/**
	 * 获取需要更新索引的店铺列表.
	 * 
	 * @author Chengyb
	 */
	public Page<Record> fetchUpdateShop(Integer pageNumber, Integer pageSize) {
		return Db.paginate(pageNumber, pageSize, "SELECT shop_id ", "FROM t_shop_score s LEFT JOIN t_shop t ON s.shop_id = t.id WHERE t.status = ? AND t.forbidden_status = ? AND s.is_async_search = ?", Shop.STATUS_ACTIVATED, Shop.FORBIDDEN_STATUS_NORMAL, NEED_ASYNC_SEARCH);
	}
	
}
