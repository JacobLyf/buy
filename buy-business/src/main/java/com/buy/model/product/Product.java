package com.buy.model.product;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.model.account.Account;
import com.buy.model.efun.Efun;
import com.buy.model.efun.EfunProduct;
import com.buy.model.efun.EfunSku;
import com.buy.model.efun.EfunSkuApply;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.order.OrderDetail;
import com.buy.model.productApply.ImportantUpdateApply;
import com.buy.model.productApply.O2oProUpdateApply;
import com.buy.model.productApply.PublicSkuUpdateApply;
import com.buy.model.shop.O2OProductApply;
import com.buy.model.shop.O2OProductUnshelveApply;
import com.buy.model.shop.Shop;
import com.buy.model.store.Store;
import com.buy.model.store.StoreSkuMap;
import com.buy.plugin.event.product.event.ProductSalesCountEvent;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.EventKit;

public class Product extends Model<Product>{
	
	/**
	 * 状态：下架
	 */
	public final static int STATUS_UNSHELVE = 0;
	/**
	 * 状态：上架
	 */
	public final static int STATUS_SHELVE = 1;
	/**
	 * 状态：删除
	 */
	public final static int STATUS_DELETE = 2;
	/**
	 * 锁定状态：正常
	 */
	public final static int LOCK_STATUS_ENABLE = 1;
	/**
	 * 锁定状态：已冻结
	 */
	public final static int LOCK_STATUS_DISABLE = 0;
	
	/**
	 * 是否包邮：是
	 */
	public final static int FREE_POSTAGE = 1;
	
	/**
	 * 是否包邮：否
	 */
	public final static int NO_FREE_POSTAGE = 0;
	
	/**
	 * 商品来源:专卖
	 */
	public final static int SOURCE_EXCLUSIVE = 1;
	/**
	 * 商品来源:自营专卖
	 */
	public final static int SOURCE_SELF_EXCLUSIVE = 2;
	/**
	 * 商品来源:自营公共
	 */
	public final static int SOURCE_SELF_PUBLIC = 3;
	/**
	 * 商品来源:e趣代售
	 */
	public final static int SOURCE_FACTORY = 4;
	/**
	 * 商品来源:厂家自发
	 */
	public final static int SOURCE_FACTORY_SEND = 5;

	/**
	 * 审核状态：未审核
	 */
	public final static int AUDIT_STATUS_UNCHECK = 0;
	/**
	 * 审核状态：审核通过
	 */
	public final static int AUDIT_STATUS_SUCCESS = 1;
	/**
	 * 审核状态：未审核失败
	 */
	public final static int AUDIT_STATUS_FAILEE = 2;

	/**
	 * 是否店铺推荐：是
	 */
	public final static int SHOP_COMMEND = 1;
	
	/**
	 * 是否店铺推荐：否
	 */
	public final static int SHOP_NOT_COMMEND = 0; 
	
	/**
	 * 商品编号前缀
	 */
	public final static String NO_PREFIX = "PRO";
	
	/**
	 * app内嵌页数据初始化12条
	 */
	public static final int APP_INIT_PRO_NUM = 12;

	private static final long serialVersionUID = 1L;
	public static final Product dao = new Product();
	/**
	 * 商品是否有上架云店
	 * @param id
	 * @return
	 * @author huangzq
	 */
	public boolean isO2o(Integer id){
		long count = Db.queryLong("SELECT  count(1)  FROM 	t_o2o_sku_map p WHERE p.product_id = ? ", id);
		if(count>0){
			return true;
		}
		return false;
	}
	/**
	 * 后取商品来源
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public Integer getSource(Integer productId){
		return dao.findByIdLoadColumns(productId, "source").getInt("source");
	}
	
	/**
	 * 根据商品编号获取商品
	 * @param productNo
	 * @return
	 * @author Jacob
	 * 2016年1月13日下午8:15:16
	 */
	public Product getProductByNo(String productNo){
		String sql = " select * from t_product where no = ? ";
		return dao.findFirst(sql,productNo);
	}
	
	/**
	 * 根据商品编号获取商品来源
	 * @param productNo
	 * @return
	 * @author Jacob
	 * 2016年1月14日下午3:00:02
	 */
	public Integer getSourceByNo(String productNo){
		String sql = " select source from t_product where no = ? ";
		return Db.queryInt(sql,productNo);
	}
	
	/**
	 * 根据商品编号获取商品上架状态
	 * @param productNo
	 * @return
	 * @author Jacob
	 * 2016年1月14日下午3:00:02
	 */
	public Integer getStatusByNo(String productNo){
		String sql = " select status from t_product where no = ? ";
		return Db.findFirst(sql,productNo).getInt("status");
	}
	
	/**
	 * 根据商品编号获取是否包邮
	 * @param productNo
	 * @return
	 * @author Jacob
	 * 2016年3月16日下午2:11:19
	 */
	public Integer getIsFreePostageByNo(String productNo){
		String sql = " select is_free_postage from t_product where no = ? ";
		return Db.queryInt(sql,productNo);
	}
	
	/**
	 * 根据商品ID判断是否为冻结店铺的商品
	 * @param productId 
	 * @return
	 * @author Jacob
	 * 2016年2月18日上午11:44:03
	 */
	public Boolean isFreezeShopProduct(Integer productId){
		String sql = " SELECT s.forbidden_status FROM t_shop s,t_product p WHERE p.shop_id = s.id AND p.id = ? ";
		if(null == Db.queryInt(sql,productId)||Db.queryInt(sql,productId)==Shop.FORBIDDEN_STATUS_NORMAL){
			return false;
		}
		return true;
	}
	
	/**
	 * 根据多个商品ID判断是否有为冻结店铺的商品
	 * @param productIds
	 * @return
	 * @author Jacob
	 * 2016年2月18日下午3:14:47
	 */
	public Boolean isFreezeShopProductMore(Integer[] productIds){
		if(StringUtil.notNull(productIds)){
			for(Integer id : productIds){
				if(this.isFreezeShopProduct(id)){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 上架
	 * @param id
	 * @param status
	 * @author huangzq
	 */
	public Boolean shelveProduct(Integer id){
		Date now = new Date();
		Product product = Product.dao.findByIdLoadColumns(id, "is_first_shelve");
		product.set("id", id);
		product.set("status", Product.STATUS_SHELVE);
		product.set("update_time", now);
		product.set("last_release_time", now);
		if(product.getInt("is_first_shelve")==null){
			product.set("is_first_shelve", BaseConstants.YES);
		}else{
			product.set("is_first_shelve", BaseConstants.NO);
		}
		return product.update();
	}
	/**
	 * 下架
	 * @param id
	 * @param status
	 * @author huangzq
	 */
	public Boolean unshelveProduct(Integer id){
		Date now = new Date();
		Product product = new Product();
		product.set("id", id);
		product.set("status", Product.STATUS_UNSHELVE);
		product.set("update_time", now);
		return product.update();
	}
	/**
	 * 获取商品其他业务状态
	 * @param productId
	 * @return（1：重要属性修改审核中，2：sku改价审核中，3：云店上架申请中，4：云店下架申请中，5：幸运一折购申请中,6:处于云店商品修改审核中,7:处于幸运一折购）
	 * @author huangzq
	 */
	public int getBusinessStatus(Integer productId){
		long flag = 0;
		//重要属性修改更新审核中
		String sql = "select if(count(*)>0,1,0) from t_important_update_apply p where p.audit_status = ? and p.product_id = ?";
		flag = Db.queryLong(sql,ImportantUpdateApply.AUDIT_STATUS_UNCHECK,productId);
		if(flag==1){
			return 1;
		}
		//商品sku改价更新审核中
		/*sql = "select if(count(*)>0,1,0) from t_shop_sku_update_apply p where p.audit_status = ? and p.product_id = ?";
		flag = Db.queryLong(sql,ShopSkuUpdateApply.AUDIT_STATUS_UNCHECK,productId);
		if(flag==1){
			return 2;
		}*/
		//是否处于云店上架申请中
		sql = "select if(count(*)>0,1,0) from t_o2o_pro_apply p where p.status = ? and p.product_id = ?";
		flag = Db.queryLong(sql,O2OProductApply.STATUS_WAIT,productId);
		if(flag==1){
			return 3;
		}
		sql = "select if(count(*)>0,1,0) from t_o2o_pro_apply p where p.status = ? and p.o2o_status = ? and p.product_id = ? ";
		flag = Db.queryLong(sql,O2OProductApply.STATUS_PASS,O2OProductApply.O2O_STATUS_NO,productId);
		if(flag==1){
			return 3;
		}
		//是否处于云店下架申请中
		sql = "select if(count(*)>0,1,0) from t_o2o_pro_unshelve_apply p where p.`status` = ? and p.product_id = ?";
		flag = Db.queryLong(sql,O2OProductUnshelveApply.AUDIT_STATUS_WAIT,productId);
		if(flag==1){
			return 4;
		}
		//是否处于幸运一折购申请中
		sql = "select if(count(*)>0,1,0) from t_efun_sku_apply p where p.`status` = ? and p.product_id = ?";
		flag = Db.queryLong(sql,EfunSkuApply.STATUS_APPROVING,productId);
		if(flag==1){
			return 5;
		}
		//是否处于云店商品修改审核中
		sql = "select if(count(*)>0,1,0) from t_o2o_pro_update_apply p where p.`audit_status` = ? and p.product_id = ?";
		flag = Db.queryLong(sql,O2oProUpdateApply.AUDIT_STATUS_UNCHECK,productId);
		if(flag==1){
			return 6;
		}
		// 公共商品sku改价更新审核中
		sql = "select if(count(*)>0,1,0) from t_public_sku_update_apply p where p.audit_status = ? and p.product_id = ?";
		flag = Db.queryLong(sql, PublicSkuUpdateApply.AUDIT_STATUS_UNCHECK, productId);
		if (flag == 1) {
			return 8;
		}
		//是否处于幸运一折购
		sql = "select if(count(*)>0,1,0) from t_pro_sku  s where s.is_efun = ? and s.product_id = ?";
		flag = Db.queryLong(sql,BaseConstants.YES,productId);
		if(flag==1){
			return 7;
		}
		return 0;
	}
	/**
	 * 查询加入O2O的skuCode
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public List<String> getO2oSkuCode(Integer productId){
		String sql = "select distinct sku_code from t_o2o_sku_map where product_id = ?";
		return Db.query(sql,productId);
	}
	
	/**
	 * 获取商品销售属性的数量
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public Long getSellPropertyCount(Integer productId){
		String sql ="SELECT count(*) FROM v_mag_property p where p.isSell = ? and p.productId = ?";
		return  Db.queryLong(sql.toString(),BaseConstants.YES,productId);
	}
	
	/**
	 * 更新加销量和结算（普通订单）
	 * @author Jacob
	 * 2016年3月21日下午3:01:16
	 * Modify By Sylveon 调整为更新加销量和结算
	 */
	public void plusSalesSettle(String orderId, boolean isSettle) {
		List<OrderDetail> orderDetailList = OrderDetail.dao.findOrderDetailSaleCount(orderId);
		for (OrderDetail od : orderDetailList) {
			Integer count = od.getInt("count");
			
			Integer proId = od.getInt("product_id");
			ProductSalesCount.dao.updateBySaleSettle(proId, count, isSettle);
			
			//=====================================
			// 商品销量更新事件. @author Chengyb
			//=====================================
			if(null != proId) {
				EventKit.postEvent(new ProductSalesCountEvent(proId));
			}
			
			String skuCode = od.getStr("sku_code");
			if(ProductSku.dao.isExist(skuCode)&&ProductSku.dao.isExist(skuCode)){
				SkuSalesCount.dao.updateBySaleSettle(skuCode, count, isSettle);
			}
		}
	}
	
	/**
	 * 更新结算（普通订单）
	 * @param orderId
	 */
	public void plusSettle(String orderId) {
		List<OrderDetail> orderDetailList = OrderDetail.dao.findOrderDetailSaleCount(orderId);
		for (OrderDetail od : orderDetailList) {
			Integer count = od.getInt("count");
			
			Integer proId = od.getInt("product_id");
			ProductSalesCount.dao.updateBySettle(proId, count);
			
			String skuCode = od.getStr("sku_code");
			if(ProductSku.dao.isExist(skuCode)){
				SkuSalesCount.dao.updateBySettle(skuCode, count);
			}
		}
	}
	
	/**
	 * 更新加销量和结算(一折购)
	 */
	public void plusSalesSettleByEfun(Integer proId, String skuCode, boolean isSettle) {
		ProductSalesCount.dao.updateBySaleSettle(proId, 1, isSettle);
		if(ProductSku.dao.isExist(skuCode)){
			SkuSalesCount.dao.updateBySaleSettle(skuCode, 1, isSettle);
		}
	}
	
	
	
	/**
	 * 计算商品价格
	 * @param productId
	 * @author huangzq
	 */
	public void calculationPrice(Integer productId){
		Product product = Product.dao.findById(productId);//new Product();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append("	min(sku.eq_price) AS minEqPrice,");
		sql.append("	min(sku.market_price) AS minMarketPrice,");
		sql.append("	MAX(sku.market_price) AS maxMarketPrice");
		sql.append(" FROM");
		sql.append("	t_pro_sku sku");
		sql.append(" WHERE");
		sql.append("	sku.product_id = ?");
		Record price = Db.findFirst(sql.toString(), productId);
		
		String marketPrice = "";
		//不相等
		if(price.getBigDecimal("minMarketPrice").compareTo(price.getBigDecimal("maxMarketPrice"))!=0){
			marketPrice = price.getBigDecimal("minMarketPrice").toString()+"-"+price.getBigDecimal("maxMarketPrice").toString();
		}else{
			marketPrice = price.getBigDecimal("minMarketPrice").toString();
		}
		
		if (StringUtil.notNull(product.getStr("shop_id")) || StringUtil.notNull(product.getStr("supplier_id"))) {
			product.set("eq_price", price.getBigDecimal("minEqPrice"));
		}
		product.set("market_price", marketPrice);
		product.set("update_time", new Date());
		product.update();
	}
	
	/**
	 * 获取专卖商品编号获取商品状态(审核状态、冻结状态、上下架状态)
	 * @param shopId
	 * @param productNo
	 * @return 
	 * @author Eriol
	 * 2016年5月31日 下午7:23:05
	 */
	public Record getProductStatus(String shopId,String productNo){
		Record record = Db.findFirst("select id,audit_status,lock_status,status from t_product where shop_id = ? AND `no` = ? ",shopId,productNo);
		return record;
	}
	
	/**
	 * O根据店铺id和商品id获取商品状态信息
	 * @param shopId
	 * @param productId
	 * @return 
	 * @author chenhg
	 * 2016年3月3日 下午5:16:18
	 */
	public Record getProductStatusById(String shopId, String productId){
		StringBuffer selectSql = new StringBuffer();
		
		selectSql.append(" select a.id,a.name,a.audit_status,a.lock_status,status ");
		selectSql.append(" from t_product a ");
		selectSql.append(" where a.shop_id = ? ");
		selectSql.append(" AND a.id = ? ");
		
		Record record = Db.findFirst(selectSql.toString(), shopId, productId);
		return record;
	}
	
	/**
	 * 根据商品编号查找商品ID
	 * @param productNo
	 * @return
	 */
	public Integer getIdByProductNo(String productNo) {
		return Db.queryInt("SELECT id FROM t_product WHERE no = ?", productNo);
	}
	/**
	 * 
	 *获取商品真实库存
	 * @author huangzq 
	 * @date 2016年7月28日 下午5:33:17
	 * @param productId
	 * @return
	 */
	public Integer getRealCount(Integer productId){
		return Db.queryBigDecimal("select IFNULL(p.realCount,0) from v_mag_product p where p.id = ?", productId).intValue();
	}
	
	/**
	 * 获取排名前面的上架状态且库存不为0的非线下热卖商品
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年7月29日下午6:57:11
	 */
	/* 此方法若启动，需要重新库存的判断
	 * public Page<Record> hotSellRecommendOnline(Page<Object> page){
		StringBuffer seleSql = new StringBuffer();
		StringBuffer whereSql = new StringBuffer();
		
		seleSql.append(" SELECT ");
		seleSql.append(" t1.id, ");
		seleSql.append(" t1.eq_price price ,");
		seleSql.append(" t1.`name` productName, ");
		seleSql.append(" t1.product_img productImg ");
		whereSql.append(" FROM t_product t1 INNER JOIN ");
		whereSql.append("  (SELECT count(a.id) num,a.product_id productId ");
		whereSql.append("  FROM t_order_detail a ");
		whereSql.append("  LEFT JOIN t_order b ON a.order_id = b.id  ");
		whereSql.append("  WHERE 1 = 1 ");
		whereSql.append("  AND b.`status` >= ? ");
		whereSql.append("  	AND b.delivery_type != ? ");
		whereSql.append("  GROUP BY a.product_id ) t2 ON t1.id = t2.productId AND t1.status = ?, ");
		whereSql.append("  (SELECT ");
		whereSql.append(" 	t.product_id ");
		whereSql.append(" FROM ");
		whereSql.append(" 	t_pro_sku t  ");
		whereSql.append(" GROUP BY 	t.product_id ");
		whereSql.append(" HAVING sum( ");
		whereSql.append("		if(t.real_count is null,0,t.real_count) + t.virtual_count ");
		whereSql.append("	)  >0 ) t3 WHERE t1.id = t3.product_id ");
		whereSql.append(" ORDER BY t2.num DESC ");
		return Db.paginate(page.getPageNumber(),page.getPageSize(),seleSql.toString(),whereSql.toString(),Order.STATUS_WAIT_FOR_EVALUATION,Order.DELIVERY_TYPE_STORE_SALE,Product.STATUS_SHELVE);
	}*/

	/***
	 * 热卖推荐--改为商品标签手动添加
	 * @Author: Jekay
	 * @Date:   2016/8/5 9:27
	 ***/
	public Page<Record> hotSellRecommendOnline(Page<Object> page){
		StringBuffer sql = new StringBuffer();
		StringBuffer where = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	a.product_id id, ");
		sql.append(" 	b.eq_price price, ");
		sql.append(" 	b.`name` productName,  ");
		sql.append(" 	b.product_img productImg ");
		where.append(" FROM ");
		where.append(" 	 t_pro_tag_map a ");
		where.append(" 	 LEFT JOIN t_product b ON a.product_id = b.id ");
		where.append(" WHERE 1=1");
		where.append(" 	 AND a.tag_type = ? ");
		where.append(" 	 ORDER BY a.sort_num,a.create_time DESC ");
		Page<Record> pageRecord = Db.paginate(page.getPageNumber(),page.getPageSize(),sql.toString(),where.toString(), ProductSignboardMap.SIGNTYPE_HOTSALE);
		//增加是否一折购商品的标识
		for(Record r : pageRecord.getList()){
			if(EfunProduct.dao.isEfunProduct(r.getInt("id"))){
				r.set("isEfun",1);
			}else{
				r.set("isEfun",0);
			}
		}
		return pageRecord;
	}

	/***
	 * e趣云店热销榜
	 * @Author: Jekay
	 * @Date:   2016/8/5 9:58
	 ***/
	public Page<Record> efunStoreHotSale(Page<Object> page){
		StringBuffer sql = new StringBuffer();
		StringBuffer where = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	a.product_id id, ");
		sql.append(" 	b.eq_price price, ");
		sql.append(" 	b.product_img productImg, ");
		sql.append(" 	b.`name` productName ");
		where.append(" FROM ");
		where.append(" 	 t_pro_tag_map a ");
		where.append(" 	 LEFT JOIN t_product b ON a.product_id = b.id ");
		where.append(" WHERE 1=1");
		where.append(" 	 AND a.tag_type = ? ");
		where.append(" 	 ORDER BY a.sort_num,a.create_time DESC ");
		Page<Record> pageRecord = Db.paginate(page.getPageNumber(),page.getPageSize(),sql.toString(),where.toString(), ProductSignboardMap.SIGNTYPE_HOTSTORE);
		//增加是否一折购商品的标识
		for(Record r : pageRecord.getList()){
			if(EfunProduct.dao.isEfunProduct(r.getInt("id"))){
				r.set("isEfun",1);
			}else{
				r.set("isEfun",0);
			}
		}
		return pageRecord;
	}
	
	/**
	 * 根据产品ID删除
	 * @param id
	 * @return
	 */
	public boolean deleteProductById (Integer id) {
		return new Product().set("id", id)
							.set("status", Product.STATUS_DELETE)
							.set("update_time", new Date())
							.update();
	}

	
	/**
	 * 根据商品ID获取店铺ID
	 */
	public String getShopIdByProId(Integer proId) {
		if (StringUtil.isNull(proId))
			return null;
		
		String source = Product.SOURCE_EXCLUSIVE + "," + SOURCE_SELF_EXCLUSIVE;
		String sql = "SELECT shop_id FROM t_product WHERE source IN (" + source + ") AND id = ?";
		
		return Db.queryStr(sql, proId);
	}
	
	/**
	 * 根据商品ID获取店铺ID
	 */
	public List<String> findShopIdByProIds(Integer[] proIds) {
		if (StringUtil.isNull(proIds))
			return null;
		
		List<Integer> proIdList = new ArrayList<Integer>();
		for (Integer id : proIds)
			proIdList.add(id);
		
		return findShopIdByProIds(proIdList);
	}
	
	/**
	 * 根据商品ID获取店铺ID
	 */
	public List<String> findShopIdByProIds(List<Integer> proIdList) {
		if (StringUtil.isNull(proIdList))
			return null;
		
		String source = Product.SOURCE_EXCLUSIVE + "," + SOURCE_SELF_EXCLUSIVE;
		String proIdStr = StringUtil.listToString(",", proIdList);
		StringBuffer sql = new StringBuffer(" SELECT shop_id FROM t_product ");
		sql.append(" WHERE source IN (" + source + ") ");
		sql.append(" AND id IN (" + proIdStr + ") ");
		
		return Db.query(sql.toString());
	}
	
	/**
	 * 是否可删除商品
	 */
	public JsonMessage isDeleteProduct(String shopId, List<Integer> proIdList) {
		String proIdsStr = StringUtil.listToString(",", proIdList);		// 页面选择商品ID
		
		/*
		 * 查询商品删除信息
		 */
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" p.id, ");
		sql.append(" p.audit_status, ");		// 商品审核状态
		sql.append(" p.lock_status, ");			// 商品冻结状态
		// 重要属性修改更新审核中
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_important_update_apply a WHERE a.product_id = p.id AND a.audit_status = " + ImportantUpdateApply.AUDIT_STATUS_UNCHECK + ") a_status, ");
		// 是否处于云店上架申请中I
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_o2o_pro_apply b WHERE b.product_id = p.id AND b.status = " + O2OProductApply.STATUS_WAIT + ") b_status, ");
		// 是否处于云店上架申请中II
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_o2o_pro_apply c WHERE c.product_id = p.id AND c.status = " + O2OProductApply.STATUS_PASS + " AND c.o2o_status = " + O2OProductApply.O2O_STATUS_NO + ") c_status, ");
		// 是否处于云店下架申请中
		sql.append(" (SELECT if(count(*)> 0, 1, 0) FROM t_o2o_pro_unshelve_apply d WHERE d.product_id = p.id AND d.status = " + O2OProductUnshelveApply.AUDIT_STATUS_WAIT + ") d_status, ");
		// 是否云店上架
		sql.append(" (select COUNT(*) from v_com_sku e WHERE e.product_id = p.id AND e.is_o2o = " + BaseConstants.YES + ") e_status, ");
		// 是否存在真实库存
		sql.append(" (SELECT SUM(IFNULL(f.real_count, 0)) FROM t_pro_sku f where f.product_id = p.id) f_status ");
		
		String source = SOURCE_EXCLUSIVE + "," + SOURCE_SELF_EXCLUSIVE;
		sql.append(" FROM t_product p ");
		sql.append(" WHERE p.source IN (" + source + ") ");
		sql.append(" AND p.shop_id = ? ");
		sql.append(" AND p.id IN (" + proIdsStr + ") ");
		List<Record> productList = Db.find(sql.toString(), shopId);
		
		/*
		 * 处理删除商品信息
		 */
		String msg = "";
		List<Integer> delIds = new ArrayList<Integer>();
		if (StringUtil.notNull(productList)) {
			for (Record p : productList) {
				// 商品审核状态
				Integer audit_status = p.getInt("audit_status");
				if (AUDIT_STATUS_SUCCESS != audit_status) {
					msg = msg.length() > 0 ? msg : "商品正在审核中，无法删除";
					continue;
				}
				// 商品冻结状态
				Integer lock_status = p.getInt("lock_status");
				if (LOCK_STATUS_ENABLE != lock_status) {
					msg = msg.length() > 0 ? msg : "商品已冻结，暂不支持操作";
					continue;
				}
				// 重要属性修改更新审核中
				Integer a_status = p.getNumber("a_status").intValue();
				if (a_status == 1) {
					msg = msg.length() > 0 ? msg : "商品重要属性的修改正在审核中，暂不支持删除";
					continue;
				}
				// 是否处于云店上架申请中
				Integer b_status = p.getNumber("b_status").intValue();
				Integer c_status = p.getNumber("c_status").intValue();
				if (b_status == 1 || c_status == 1) {
					msg = msg.length() > 0 ? msg : "商品正在申请进驻O2O云店，暂不支持删除";
					continue;
				}
				// 是否处于云店下架申请中
				Integer d_status = p.getNumber("d_status").intValue();
				if (d_status == 1) {
					msg = msg.length() > 0 ? msg : "商品正在申请退驻O2O云店，暂不支持删除";
					continue;
				}
				// 是否云店上架
				Integer e_status = p.getNumber("e_status").intValue();
				if (e_status > 0) {
					msg = msg.length() > 0 ? msg : "商品部分Sku已进驻e趣云店，请先申请退驻e趣云店";
					continue;
				}
				// 是否存在真实库存
				Integer f_status = p.getNumber("f_status").intValue();
				if (f_status > 0) {
					msg = msg.length() > 0 ? msg : "商品部分存在云店或仓库，暂不支持删除";
					continue;
				}
				// 设置删除商品ID
				Integer proId = p.getInt("id");
				delIds.add(proId);
			}
		}
		
		JsonMessage result = new JsonMessage();
		if (StringUtil.notNull(delIds))
			return result.setData(delIds);
		else
			return result.setStatusAndMsg("1", msg);
	}
	
	/**
	 * 删除店铺商品
	 */
	public int deleteProductByShop(String shopId, List<Integer> proIdList) {
		if (StringUtil.notNull(proIdList)) {
			// 删除商品
			String delIds = StringUtil.listToString(",", proIdList);
			String source = SOURCE_EXCLUSIVE + "," + SOURCE_SELF_EXCLUSIVE;
			StringBuffer sql = new StringBuffer(" UPDATE t_product ");
			sql.append(" SET status = ?, update_time = NOW() ");
			sql.append(" WHERE source IN (" + source + ") ");
			sql.append(" AND shop_id = ? ");
			sql.append(" AND id IN (" + delIds + ") ");
			return Db.update(sql.toString(), STATUS_DELETE, shopId);
		} else {
			return 0;
		}
	}
	
	public JsonMessage isUnshelveBySupplier(String supplierId, Integer[] proIdList) {
		String proIdsStr = StringUtil.arrayToString(",", proIdList);		// 页面选择商品ID
		/*
		 * 查询商下架除信息
		 */
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" p.id, ");
		// 是否处于云店上架申请中I
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_o2o_pro_apply b WHERE b.product_id = p.id AND b.status = " 
						+ O2OProductApply.STATUS_WAIT + ") b_status, ");
		// 是否处于云店上架申请中II
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_o2o_pro_apply c WHERE c.product_id = p.id AND c.status = " 
						+ O2OProductApply.STATUS_PASS + " AND c.o2o_status = " 
						+ O2OProductApply.O2O_STATUS_NO + ") c_status, ");
		// 是否处于云店下架申请中
		sql.append(" (SELECT if(count(*)> 0, 1, 0) FROM t_o2o_pro_unshelve_apply d WHERE d.product_id = p.id AND d.status = " 
						+ O2OProductUnshelveApply.AUDIT_STATUS_WAIT + ") d_status, ");
		
		// 是否云店上架
		sql.append(" (select COUNT(*) from v_com_sku e WHERE e.product_id = p.id AND e.is_o2o = " 
						+ BaseConstants.YES + ") e_status ");
		String source = SOURCE_FACTORY_SEND + "," + SOURCE_FACTORY;
		sql.append(" FROM t_product p ");
		sql.append(" WHERE p.source IN (" + source + ") ");
		sql.append(" AND p.supplier_id = ? ");
		sql.append(" AND p.id IN (" + proIdsStr + ") ");
		
		List<Record> productList = Db.find(sql.toString(), supplierId);
		
		return handleUnshelveMessage(productList);
	}
	
	/**
	 * 是否可下架商品
	 */
	public JsonMessage isUnshelveByShop(String shopId, Integer[] proIdList) {
		String proIdsStr = StringUtil.arrayToString(",", proIdList);		// 页面选择商品ID
		
		/*
		 * 查询商下架除信息
		 */
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" p.id, ");
		// 是否处于云店上架申请中I
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_o2o_pro_apply b WHERE b.product_id = p.id AND b.status = " + O2OProductApply.STATUS_WAIT + ") b_status, ");
		// 是否处于云店上架申请中II
		sql.append(" (SELECT if(count(*) > 0, 1, 0) FROM t_o2o_pro_apply c WHERE c.product_id = p.id AND c.status = " + O2OProductApply.STATUS_PASS + " AND c.o2o_status = " + O2OProductApply.O2O_STATUS_NO + ") c_status, ");
		// 是否处于云店下架申请中
		sql.append(" (SELECT if(count(*)> 0, 1, 0) FROM t_o2o_pro_unshelve_apply d WHERE d.product_id = p.id AND d.status = " + O2OProductUnshelveApply.AUDIT_STATUS_WAIT + ") d_status, ");
		// 是否云店上架
		sql.append(" (select COUNT(*) from v_com_sku e WHERE e.product_id = p.id AND e.is_o2o = " + BaseConstants.YES + ") e_status ");
		
		String source = SOURCE_EXCLUSIVE + "," + SOURCE_SELF_EXCLUSIVE;
		sql.append(" FROM t_product p ");
		sql.append(" WHERE p.source IN (" + source + ") ");
		sql.append(" AND p.shop_id = ? ");
		sql.append(" AND p.id IN (" + proIdsStr + ") ");
		List<Record> productList = Db.find(sql.toString(), shopId);
		
		return handleUnshelveMessage(productList);
	}
	
	public JsonMessage handleUnshelveMessage(List<Record> productList) {
		/*
		 * 处理下架商品信息
		 */
		String msg = "";
		List<Integer> unshelveList = new ArrayList<Integer>();
		if (StringUtil.notNull(productList)) {
			for (Record p : productList) {
				// 是否处于云店上架申请中
				Integer b_status = p.getNumber("b_status").intValue();
				Integer c_status = p.getNumber("c_status").intValue();
				if (b_status == 1 || c_status == 1) {
					msg = msg.length() > 0 ? msg : "商品处于云店上架申请中，审核期间不支持商品下架";
					continue;
				}
				// 是否处于云店下架申请中
				Integer d_status = p.getNumber("d_status").intValue();
				if (d_status == 1) {
					msg = msg.length() > 0 ? msg : "商品处于云店下架申请中，审核期间不支持商品下架";
					continue;
				}
				// 是否云店上架
				Integer e_status = p.getNumber("e_status").intValue();
				if (e_status > 0) {
					msg = msg.length() > 0 ? msg : "商品处于上架云店中，期间不支持商品下架";
					continue;
				}
				// 设置下架商品ID
				Integer proId = p.getInt("id");
				unshelveList.add(proId);
			}
		}
		
		JsonMessage result = new JsonMessage();
		if (StringUtil.notNull(unshelveList)) {
			int size = unshelveList.size();
			Integer[] arr = new Integer[size];
			for (int i = 0; i < size; i++)
				arr[i] = unshelveList.get(i);
			result.setData(arr);
			return result;
		} else {
			result.setStatusAndMsg("1", msg);
			return result;
		}
	}
	
	
	/**
	 * 根据商品ID获取店铺NO
	 */
	public String findShopNoByProId(Integer proId) {
		if (StringUtil.isNull(proId))
			return null;
		
		String source = Product.SOURCE_EXCLUSIVE + "," + SOURCE_SELF_EXCLUSIVE;
		StringBuffer sql = new StringBuffer(" SELECT shop_id FROM t_product ");
		sql.append(" WHERE source IN (" + source + ") ");
		sql.append(" AND id = ? ");
		
		String shopId = Db.queryStr(sql.toString(), proId);
		if (StringUtil.isNull(shopId))
			return null;
		
		return Shop.dao.getShopNoByShopId(shopId);
	}
	
	/**
	 * 计算供货商公共商品e趣价
	 * @param marketPrice 市场价
	 * @param settlePrice 结算价
	 * @return
	 */
	public BigDecimal calculationEqPrice(BigDecimal marketPrice, BigDecimal settlePrice) {
		BigDecimal EV = marketPrice.multiply(new BigDecimal("0.07"));
		BigDecimal eqPrice = settlePrice.add(EV).add((marketPrice.subtract(settlePrice)).divide(new BigDecimal("2")));
		return eqPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	/**
	 * 根据某个sku获取所属商品信息
	 * @param skuCode
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年9月27日下午3:08:01
	 */
	public Product getProduct(String skuCode){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("	t.* ");
		sql.append("FROM ");
		sql.append("	t_product t,");
		sql.append("	t_pro_sku t2 ");
		sql.append("WHERE ");
		sql.append("	t2.`code` = ? ");
		sql.append("AND t.id = t2.product_id ");
		return dao.findFirst(sql.toString(),skuCode);
	}


	/**
	 * 根据某个sku获取所属商品是否是幸运一折吃商品
	 * @param skuCode
	 * @return
	 * @throws
	 * @author jekay
	 * @date 2016年12月12日下午21:08:01
	 */
	public boolean isEfunEat(String skuCode){
		Product pro = getProduct(skuCode);
		if(StringUtil.notNull(pro)){
			String shopId = pro.getStr("shop_id");
			if(StringUtil.notNull(shopId)){
				int type = Shop.dao.findByIdLoadColumns(shopId, "type").getInt("type");
				if(type == Shop.TYPE_EAT){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * O根据商品id获取商品相关状态信息
	 * @param productId
	 * @return 
	 */
	public Record getProductStatusById(Integer productId){
		StringBuffer selectSql = new StringBuffer();
		
		selectSql.append(" select a.id,a.name,a.audit_status,a.lock_status,status ");
		selectSql.append(" from t_product a ");
		selectSql.append(" where a.id = ? ");
		
		Record record = Db.findFirst(selectSql.toString(), productId);
		return record;
	}
	/**
	 * 获取店铺或供货商所有在云店的SKU商品列表
	 * @param merchantType
	 * @param merchantId
	 * @param page
	 * @return
	 */
	public Page<Record> getAllProductInStore(Integer merchantType, String merchantId, Page<Object> page) {
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		StringBuffer where = new StringBuffer();
		sql.append(" SELECT  p.`no` as productNo, p.`name` as productName, "
				         + " sm.sku_code as skuCode, ps.property_decs as propertyDecs, "
				         + " ps.deliver_rule as deliverRule, p.id as productId, "
				         + " ps.appoint_store_no as appointStoreNo, "
				         + " s.name as appointStoreName, "
				         + " ps.virtual_count - ps.lock_count as selfCount ");
		
		where.append(" FROM (SELECT DISTINCT sku_code from t_o2o_sku_map) sm ");
		where.append(" LEFT JOIN t_pro_sku ps ON ps.code = sm.sku_code ");
		where.append(" LEFT JOIN t_product p ON ps.product_id = p.id ");
		where.append(" LEFT JOIN t_store s ON ps.appoint_store_no = s.`no` ");
		where.append(" WHERE 1 = 1 ");
		if (merchantType == Account.TYPE_SHOP) {
			where.append(" and p.shop_id = ?");
			params.add(merchantId);
			where.append(" and p.source in ( ? ) ");
			params.add(Product.SOURCE_EXCLUSIVE);
		} else if (merchantType == Account.TYPE_SUPPLIER) {
			where.append(" and p.supplier_id = ?");
			params.add(merchantId);
			where.append(" and p.source in ( ? ) ");
			params.add(Product.SOURCE_FACTORY_SEND);
		}
		where.append(" ORDER BY	ps.create_time DESC");
		return Db.paginate(page.getPageNumber(), page.getPageSize(), sql.toString(), where.toString(), params.toArray());
	}
	
	/**
	 * 获取卖家所有在云店的SKU商品详情列表
	 * @param merchantType
	 * @param merchantId
	 * @param page
	 * @return
	 */
	public Page<Record> allProInStoreDetailByMerchant(Integer merchantType, String merchantId, Page<Object> page) {
		Page<Record> result = dao.getAllProductInStore(merchantType, merchantId, page);
		
		StringBuffer sql = new StringBuffer();
		sql.append(" select SUM(pro_count - store_lock_count) from t_store_sku_map skm");
		sql.append(" LEFT JOIN t_o2o_sku_map sm ON skm.store_no = sm.store_no AND skm.sku_code = sm.sku_code");
		sql.append(" LEFT JOIN t_store s ON skm.store_no = s.`no`");
		sql.append(" where skm.sku_code = ?");
		sql.append(" AND (s.type <> ? OR (s.type = ? AND sm.sku_code IS NOT NULL))");
		
		for (Record r : result.getList()) {
			String skuCode = r.get("skuCode");
			int storeType = Store.TYPE_CLOUD;
			BigDecimal sumCount = Db.queryBigDecimal(sql.toString(), skuCode, storeType, storeType);
			if (StringUtil.isNull(sumCount)) {
				r.set("sumCount", 0);
			} else {
				r.set("sumCount", sumCount.intValue());
			}
			
			String appointStoreNo = r.getStr("appointStoreNo");
			if (StringUtil.notNull(appointStoreNo)) {
				r.set("appointStoreCount", StoreSkuMap.dao.getStoreRealCount(skuCode, appointStoreNo));
			}
		}
		return result;
	}
	
	/**
	 * 获取库存(商品)
	 * @param code
	 * @return
	 * @author chenhg
	 */
	public Integer getCount(int proId){
		
		Record record = Db.findFirst("SELECT IFNULL(sum(IFNULL(real_count,0) + virtual_count - lock_count - store_lock_count),0) AS total FROM t_pro_sku WHERE product_id = ? ",proId);
		if(record == null || StringUtil.isNull(record.getNumber("total"))){
			return 0;
		}else{
			return record.getNumber("total").intValue();
		}
	}
	
	/**
	 * 判断是一个商品是否有库存
	 * @param proId
	 * @return
	 */
	public boolean hasCount(int proId){
		int count = Product.dao.getCount(proId);
		if(count > 0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 查询商品商家信息（店主或者供货商一起查出来）
	 * @author chenhj
	 * @param proId
	 * @return
	 */
	public Record productMerchantInfo(Integer proId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ")
		   .append("   p.id proId,")
		   .append("   s.id shopId,")
		   .append("   s.`no` shopNo,")
		   .append("   s.`name` shopName,")
		   .append("   sp.id supplierId,")
		   .append("   sp.`no` supplierNo,")
		   .append("   sp.`name` supplierName")
		   .append(" FROM t_product p")
		   .append(" LEFT JOIN t_shop s ON p.shop_id = s.id")
		   .append(" LEFT JOIN t_supplier sp ON sp.id = p.supplier_id")
		   .append(" WHERE p.id = ?");
		return Db.findFirst(sql.toString(), proId);
	}
	
	/**
	 * 根据商品ID获取店铺Id/供货商Id.
	 * 
	 * @param productId
	 *        商品Id.
	 * @author Chengyb
	 * @return 以下字段<br>
	 *         shop_id : 店铺Id<br>
	 *         supplier_id : 供货商Id<br>
	 *         status : 状态(0:下架,1:上架,2:已删除)<br>
	 *         lock_status : 冻结状态(0:已冻结,1:正常)
	 */
	public Record findProduct4EfunOrderByProId(Integer productId) {
		if (StringUtil.isNull(productId))
			return null;
		
		String sql = "SELECT shop_id, supplier_id, status, lock_status FROM t_product WHERE id=?";
		
		return Db.findFirst(sql, productId);
	}
	
	/**
	 * 更新商品浏览次数.
	 * 
	 * @param productId
	 *            商品Id.
	 * @author Chengyb
	 */
	public void viewProduct(Integer productId) {
		String sql = "UPDATE t_product  t SET t.view_count = t.view_count + 1 WHERE t.id = ?";
		Db.update(sql.toString(), productId);
	}
	
	/**
	 * 获取商品详情信息
	 * @param proId
	 * @return
	 */
	public Record getProdcutDetailMessage(Integer proId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT    ");
		sql.append("   b.id productId , ");
		sql.append("   b.`name` productName, ");
		sql.append("   b.product_img productImg, ");
		sql.append("   b.`status` , ");
		sql.append("   b.`lock_status` , ");
		sql.append("   b.is_peishi , ");
		sql.append("   b.view_count , ");
		sql.append("   a.is_efun , ");
		sql.append("   b.product_img, ");
		sql.append("   MIN(a.eq_price) efunPrice, ");
		sql.append("   '1' prizeNum, ");
		sql.append("   '0' join_count ");
		sql.append(" FROM t_pro_sku a ");
		sql.append("   LEFT JOIN t_product b ON a.product_id = b.id ");
		sql.append(" WHERE a.product_id = ?  ");
		sql.append(" AND a.is_efun = ?  ");
		Record record = Db.findFirst(sql.toString(), proId, BaseConstants.YES);
		//处理幸运一折购价格
		String eqPriceStr = record.get("efunPrice").toString();
		BigDecimal eqPrice = new BigDecimal(eqPriceStr);
		//保留两位小数，向上取整
		BigDecimal efunPrice = eqPrice.multiply(EfunSku.EFUN_PRICE_RATE, new MathContext(eqPriceStr.length()-2, RoundingMode.CEILING));
		
		/**
		 * 处理奖区数、参与人数
		 */
		//得到最新的开奖期次、开奖时间
		Efun efunRecord = Efun.dao.getNewestEfun();
		String efunId = efunRecord.get("id").toString();
		record.set("lottery_time", efunRecord.get("lottery_time"));//设置开奖时间
		
		int joinCount = EfunUserOrder.dao.countJoin(proId, efunId);
		
		float num = (float)joinCount/28 + 1.0f;
		int prizeNum = (int)Math.floor(num);
		
		record.set("prizeNum", prizeNum);
		record.set("join_count", joinCount);
		// e趣价
		record.set("eq_price", record.get("efunPrice"));
		record.set("eqPrice", record.get("efunPrice"));
		// 一折购价格
		record.set("efunPrice", efunPrice);
		record.set("newestEfunId", efunId);
		return record;
	}
	
	/**
	 * 获取商品的状态
	 * 4：找不到商品
	 * 5：商品已下架
	 * 6：商品已被锁定
	 * 7：商品未审核通过
	 * 8：商品对应店铺已冻结
	 * 0：正常
	 * @param skuCode
	 * @return
	 * @author chenhj
	 */
	public int getProdcutStatusById(Integer productId){
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("	p.lock_status, ");
		sql.append("	p.audit_status,");
		sql.append("	p.`status`,");
		sql.append("	ifnull(s.forbidden_status,0) shopStatus ");
		sql.append("FROM ");
		sql.append("	t_product p");
		sql.append("	LEFT JOIN t_shop s ON p.shop_id = s.id  ");
		sql.append("WHERE ");
		sql.append("	p.`id` = ? ");
		Record product =  Db.findFirst(sql.toString(), productId);
		//找不到对应的商品
		if(product == null){
			return 4;
		//商品已下架
		} else if (product.getInt("status") != Product.STATUS_SHELVE) {
			return 5;
		//商品已被锁定
		} else if (product.getInt("lock_status") != Product.LOCK_STATUS_ENABLE) {
			return 6;
		//商品未审核通过
		} else if (product.getInt("audit_status") != Product.AUDIT_STATUS_SUCCESS) {
			return 7;
		//商品对应店铺已冻结
		} else if (product.getNumber("shopStatus").intValue() != Shop.FORBIDDEN_STATUS_NORMAL) {
			return 8;
		}
		
		return 0;
	}
}