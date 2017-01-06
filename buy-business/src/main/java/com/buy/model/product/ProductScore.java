package com.buy.model.product;

import java.math.BigDecimal;
import com.buy.common.BaseConstants;
import com.buy.model.order.Order;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class ProductScore extends Model<ProductScore>{
	
	private static final long serialVersionUID = 1L;
	public static final ProductScore dao = new ProductScore();
	
	/*
	 * 是否同步搜索索引（0：否，1：是）
	 */
	public static final String NEED_ASYNC_SEARCH = "0";
	
	/**
	 * 添加商品得分
	 * @param productId
	 * @author huangzq
	 */
	public void add(Integer productId){
		ProductScore productScore = dao.findById(productId);
		if(productScore==null){
			productScore = new ProductScore();
			productScore.set("pro_id", productId);
			productScore.save();
		}
	}
	/**
	 * 更新资料完整度得分
	 * @param productId
	 * @author huangzq
	 */
	public void updateDataIntegrityScore(Integer productId){
		Product product = Product.dao.findByIdLoadColumns(productId, "introduce,keyword,detail");
		//基本资料
		int score = 0;
		if(StringUtil.notNull(product.getStr("introduce"))
				&&StringUtil.notNull(product.getStr("keyword"))
				&&StringUtil.notNull(product.getStr("detail"))){
			score+=1;
			
		}
		long imgCount = Db.queryLong("select count(*) from t_pro_img where product_id = ?",productId); 
		if(imgCount>=4){
			score+=1;
		}
		ProductScore productScore = new ProductScore();
		productScore.set("pro_id", productId);
		productScore.set("data_integrity_score", score);
		productScore.update();
	}
	/**
	 * 更新利润率得分
	 * @param productId
	 * @author huangzq
	 */
	public void updateProfitScore(Integer productId){
		Product product = Product.dao.findByIdLoadColumns(productId, "sort_id,source");
		int source = product.getInt("source");
		Record sku = Db.findFirst("select max(eq_price) as eqPrice, max(supplier_price) as supplierPrice from t_pro_sku s where s.product_id = ?",productId);
		//利润
		BigDecimal profit  = new BigDecimal(0);
		//利润率
		BigDecimal profitRate =  new BigDecimal(0);
		//得分
		int score = 0;
		//专卖
		if(source==Product.SOURCE_EXCLUSIVE||source==Product.SOURCE_SELF_EXCLUSIVE){
			//佣金扣点
			profitRate = Db.queryBigDecimal("select commission_rate from t_pro_back_sort where id = ?",product.getInt("sort_id"));
			profit = sku.getBigDecimal("eqPrice").multiply(profitRate).divide(new BigDecimal(100),2);
			
		}else{
			profit = sku.getBigDecimal("eqPrice").subtract(sku.getBigDecimal("supplierPrice")).setScale(2);
			profitRate = profit.multiply(new BigDecimal(100)).divide(sku.getBigDecimal("eqPrice"),2);
		}
		if(profitRate.compareTo(new BigDecimal(5))>=0){
			if(profit.compareTo(new BigDecimal(10))>=0){
				score = 2;
			}else{
				score = 1;
			}
		}else{
			if(profit.compareTo(new BigDecimal(10))>=0){
				score = 1;
			}
		}
		ProductScore productScore = new ProductScore();
		productScore.set("pro_id", productId);
		productScore.set("profit_score", score);
		productScore.update();
		
		
	}
	/**
	 * 新品扶持（定时器调用）
	 * 
	 * @author huangzq
	 */
	public void updateNewProScore(){
		StringBuffer sql = new StringBuffer();
		sql.append(" UPDATE t_product_score ps");
		sql.append(" SET ps.new_product_score = ?");
		sql.append(" WHERE");
		sql.append(" ps.pro_id IN (");
		sql.append(" SELECT");
		sql.append("			p.id");
		sql.append("		FROM");
		sql.append("			t_product p");
		sql.append("		WHERE");
		sql.append("			p.is_first_shelve = ?");
		sql.append("		AND TIMESTAMPDIFF( DAY,p.last_release_time,now())>=? )");
		//7天以外首次上架加0
		Db.update(sql.toString(),0,BaseConstants.YES,7);
		sql = sql.deleteCharAt(sql.length()-1);
		sql.append("		AND TIMESTAMPDIFF( DAY,p.last_release_time,now())<? )");
		//1天内首次上架加15
		Db.update(sql.toString(),15,BaseConstants.YES,0,1);
		//1到3天内首次上架加10
		Db.update(sql.toString(),10,BaseConstants.YES,1,3);
		//3到7天内首次上架加2
		Db.update(sql.toString(),2,BaseConstants.YES,3,7);
		
	}
	/**
	 * 新店扶持（定时器调用）
	 * 
	 * @author huangzq
	 */
	public void updateNewShopScore(){
		StringBuffer sql = new StringBuffer();
		sql.append(" UPDATE t_product_score ps");
		sql.append(" SET ps.new_shop_score = ?");
		sql.append(" WHERE");
		sql.append("	ps.pro_id IN (");
		sql.append("		SELECT");
		sql.append("			p.id");
		sql.append("		FROM");
		sql.append("			t_product p");
		sql.append("		INNER JOIN t_shop s ON p.shop_id = s.id");
		sql.append("		WHERE");
		sql.append("			p.is_first_shelve = ?");
		sql.append("		AND TIMESTAMPDIFF(DAY, s.acvtivate_time, now()) >= ?)");
		//30天以外首次上架加0
		Db.update(sql.toString(),0,BaseConstants.YES,30);
		sql = sql.deleteCharAt(sql.length()-1);
		sql.append("		AND TIMESTAMPDIFF(DAY, s.acvtivate_time, now()) < ?)");

		//3天内首次上架加12
		Db.update(sql.toString(),12,BaseConstants.YES,0,3);
		//3到7天内首次上架加10
		Db.update(sql.toString(),10,BaseConstants.YES,1,3);
		//7到30天内首次上架加5
		Db.update(sql.toString(),2,BaseConstants.YES,7,30);
	}
	/**
	 * 更新o2o商品评分
	 * @param productId
	 * @author huangzq
	 */
	public void updateO2oScore(Integer productId){
		int score = 0;
		//sku个数
		long skuCount = Db.queryLong("SELECT count(*) from t_pro_sku sku where sku.product_id = ?",productId);
		//o2o个数
		long o2oCount = Db.queryLong("SELECT count(*) from t_o2o_sku_map sku where sku.product_id = ?",productId);
		if(0<o2oCount&&o2oCount<skuCount){
			score = 4;
		}else if(o2oCount==skuCount){
			score = 8;
		}
		if(score>0){
			ProductScore productScore = new ProductScore();
			productScore.set("pro_id", productId);
			productScore.set("o2o_product_score", score);
			productScore.update();
		}
	}
	/**
	 * 更新幸运一折购商品评分
	 * @param productId
	 * @author huangzq
	 */
	public void updateEfunScore(Integer productId){
		int score = 0;
		String sql = "SELECT count(*) from t_pro_sku sku where sku.product_id = ?";
		//sku个数
		long skuCount = Db.queryLong(sql,productId);
		sql+=" and sku.is_efun = ?";
		//幸运一折购个数
		long efunCount = Db.queryLong(sql,productId,BaseConstants.YES);
		if(0<efunCount&&efunCount<skuCount){
			score = 4;
		}else if(efunCount==skuCount){
			score = 8;
		}
		if(score>0){
			ProductScore productScore = new ProductScore();
			productScore.set("pro_id", productId);
			productScore.set("efun_product_score", score);
			productScore.update();
		}
	}
	/**
	 * 更新假一赔十分数
	 * 
	 * @author huangzq
	 */
	public void updatePeishiSore(Integer productId){
		int score = 0;
		String peishiSql = "select p.is_peishi from t_product p.id = ?";
		int isPeishi = Db.queryInt(peishiSql.toString(), productId);
		if(isPeishi==BaseConstants.YES){
			score = 5;
		}
		String sql ="update t_product_score p set p.dentification_score = ? where p.pro_id = ?";
		Db.update(sql,score,productId);
	
	}
	/**
	 * 更新店铺保证金分数
	 * 
	 * @author huangzq
	 */
	public void updateDepositSore(String shopId,int score){

		String sql ="update t_product_score ps set ps.shop_deposit_score = ? where ps.pro_id in (select p.id from t_product p where p.shop_id = ?)";
		Db.update(sql,score,shopId);
	
	}

	/**
	 * 店铺认证评分
	 * @param shopId
	 * @return
	 * @author 
	 */
	public Boolean updateCertificationScore(String shopId,int score) {
		// 查询店铺认证
		String sql ="update t_product_score ps set ps.dentification_score = ? where ps.pro_id in (select p.id from t_product p where p.shop_id = ?)";
		return Db.update(sql,score,shopId) > 0 ? true : false;
	}
	/**
	 * 更新月均人气分数
	 * 
	 * @author huangzq
	 */
	public void updatePopularityPerMonthScore(){
		StringBuffer sql = new StringBuffer();
		sql.append(" UPDATE t_product_score ps");
		sql.append(" SET ps.popularity_per_month_score = ?");
		sql.append(" WHERE");
		sql.append("	ps.pro_id IN (");
		sql.append("		SELECT");
		sql.append("			p.id");
		sql.append("		FROM");
		sql.append("			t_product p");
		sql.append("		WHERE");
		sql.append("			DATE_ADD(");
		sql.append("				p.last_release_time,");
		sql.append("				INTERVAL 1 MONTH");
		sql.append("			) < now()");
		sql.append("		AND (");
		sql.append("			p.view_count / TIMESTAMPDIFF(");
		sql.append("				DAY,");
		sql.append("				p.last_release_time,");
		sql.append("				now())) * 30 >= ?	)");
		//人气大于3000加10
		Db.update(sql.toString(),10,3000);
		sql = sql.deleteCharAt(sql.length()-1);
		sql.append("		AND (");
		sql.append("			p.view_count / TIMESTAMPDIFF(");
		sql.append("				DAY,");
		sql.append("				p.last_release_time,");
		sql.append("				now())) * 30 < ?	)");
		//人气大于1500小于3000加8
		Db.update(sql.toString(),8,1500,3000);
		//人气大于1000小于1500加5
		Db.update(sql.toString(),5,1000,1500);
		//人气大于500小于1000加2
		Db.update(sql.toString(),2,500,1000);
	}
	/**
	 * 更新近一个月成交量评分（定时器）
	 * 
	 * @author huangzq
	 */
	public void updateTurnoverScore(){
		StringBuffer sql = new StringBuffer();
		//订单详情
		StringBuffer orderDetailSql = new StringBuffer();
		orderDetailSql.append("	SELECT");
		orderDetailSql.append("		rd.product_id");
		orderDetailSql.append("	FROM");
		orderDetailSql.append("		t_order_detail rd");
		orderDetailSql.append("	INNER JOIN t_order r ON rd.order_id = r.id");
		orderDetailSql.append("	WHERE");
		orderDetailSql.append("		r.`status` >= ?");
		orderDetailSql.append("	AND rd.create_time BETWEEN DATE_SUB(now(), INTERVAL 1 MONTH)");
		orderDetailSql.append("	AND now()");
		orderDetailSql.append("	GROUP BY");
		orderDetailSql.append("		rd.product_id");
		orderDetailSql.append("	HAVING");
		orderDetailSql.append("		sum(rd.count) >= ?");
		sql.append(" UPDATE t_product_score ps,");
		sql.append(" (");
		sql.append(orderDetailSql.toString());	
		sql.append(") rp");
		sql.append(" SET ps.turnover_score = ps.turnover_score+?");
		sql.append(" WHERE");
		sql.append(" ps.pro_id = rp.product_id");
		
		//大于500加10
		Db.update(sql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,500,10);
		sql = new StringBuffer();
		sql.append(" UPDATE t_product_score ps,");
		sql.append(" (");
		orderDetailSql.append("	and sum(rd.count) < ?	");
		sql.append(orderDetailSql.toString());	
		sql.append(") rp");
		sql.append(" SET ps.turnover_score = ?");
		sql.append(" WHERE");
		sql.append(" ps.pro_id = rp.product_id");
		
		//300到500加8
		Db.update(sql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,300,500,8);
		//100到300加5
		Db.update(sql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,100,300,5);
		//50到100加2
		Db.update(sql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,50,100,2);
	}
	
	/**
	 * 更新好评率评分（定时器）
	 * 
	 * @author huangzq
	 */
	public void updatePositiveScore(){
		StringBuffer badSql = new StringBuffer();
		StringBuffer goodSql = new StringBuffer();
		goodSql.append(" UPDATE t_product_score ps,");
		goodSql.append(" (");
		goodSql.append("	SELECT");
		goodSql.append("		rd.product_id");
		goodSql.append("	FROM");
		goodSql.append("		t_order_detail rd");
		goodSql.append("	INNER JOIN t_order r ON rd.order_id = r.id");
		goodSql.append("	WHERE");
		goodSql.append("		r.`status` >= ?");
		goodSql.append("	AND rd.create_time BETWEEN DATE_SUB(now(), INTERVAL 1 MONTH)");
		goodSql.append("	AND now()");
		goodSql.append("	GROUP BY");
		goodSql.append("		rd.product_id");
		goodSql.append("	HAVING");
		goodSql.append("		sum(rd.count) > 30");
		goodSql.append(" ) AS rp");
		goodSql.append(" SET ps.positive_ratio = positive_ratio+?");
		goodSql.append(" WHERE ");
		goodSql.append(" rp.product_id = ps.pro_id ");
		goodSql.append("  AND ps.pro_id IN (");
		goodSql.append("	SELECT");
		goodSql.append("		pe.product_id");
		goodSql.append("	FROM");
		goodSql.append("		t_pro_evaluate pe");
		goodSql.append("	GROUP BY");
		goodSql.append("		pe.product_id");
		goodSql.append("	HAVING");
		badSql.append(goodSql);
		
		
		goodSql.append("		sum(");
		goodSql.append("			CASE pe.praise_radio");
		goodSql.append("			WHEN 5 THEN	1");
		goodSql.append("			ELSE 0");
		goodSql.append("			END");
		goodSql.append("		) / count(1)*100>=?)");
	
		//100
		Db.update(goodSql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,10,100);
		goodSql = goodSql.deleteCharAt(goodSql.length()-1);
		goodSql.append("		and sum(");
		goodSql.append("			CASE pe.praise_radio");
		goodSql.append("			WHEN 5 THEN	1");
		goodSql.append("			ELSE 0");
		goodSql.append("			END");
		goodSql.append("		)/count(1)*100<?)");
		//90到100加5
		Db.update(goodSql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,5,90,100);
		badSql.append("	      	sum(");
		badSql.append("				CASE pe.praise_radio");
		badSql.append("			WHEN 1 THEN");
		badSql.append("				1");
		badSql.append("			WHEN 2 THEN");
		badSql.append("				1");
		badSql.append("			ELSE");
		badSql.append("				0");
		badSql.append("			END");
		badSql.append("		) / count(1) * 100 >= ?)");
		//50得-5
		Db.update(badSql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,-5,50);
		badSql = badSql.deleteCharAt(badSql.length()-1);
		badSql.append("		and sum(");
		badSql.append("				CASE pe.praise_radio");
		badSql.append("			WHEN 1 THEN");
		badSql.append("				1");
		badSql.append("			WHEN 2 THEN");
		badSql.append("				1");
		badSql.append("			ELSE");
		badSql.append("				0");
		badSql.append("			END");
		badSql.append("		) / count(1) * 100 < ?)");
		//30到50得-2
		Db.update(badSql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,-2,30,50);
	}
	
	/**
	 * 获取需要更新索引的商品列表.
	 * 
	 * @author Chengyb
	 */
	public Page<Record> fetchUpdateProduct(Integer pageNumber, Integer pageSize) {
		return Db.paginate(pageNumber, pageSize, "SELECT pro_id ", "FROM t_product_score t WHERE t.is_async_search = ? ORDER BY pro_id ASC", NEED_ASYNC_SEARCH);
	}
	
}
