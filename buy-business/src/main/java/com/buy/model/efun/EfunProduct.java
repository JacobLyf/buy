package com.buy.model.efun;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.product.ProductSku;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 幸运一折购商品表.
 */
public class EfunProduct extends Model<EfunProduct>{
	
	private static final long serialVersionUID = -5023722819833224774L;
	
	public static final EfunProduct dao = new EfunProduct();
	
	/**
	 * 获取幸运一折购商品访问次数.
	 * 
	 * @param productId
	 *            商品Id
	 * @return
	 */
	public Integer getViewsCount(Integer productId){
		String sql = "SELECT views_count FROM t_efun_product WHERE product_id = ?";
		return Db.queryInt(sql, productId);
	}
	
	/**
	 * 根据商品id获取相应的幸运一折购跟商品关联记录
	 * @param productId 商品ID
	 * @return
	 * @author Jacob
	 * 2016年5月30日下午2:56:41
	 */
	public EfunProduct getEfunProductByProductId(Integer productId){
		String sql = " SELECT * FROM t_efun_product where product_id = ?  ";
		return dao.findFirst(sql,productId);
	}
	
	/**
	 * 批量新增幸运一折购跟商品关联
	 * @author Jacob
	 * 2016年5月30日下午1:28:25
	 */
	public void batchAdd(String[] skuCodes){
		List<Object> efunProductParams = new ArrayList<Object>();
		// 批量新增幸运一折购跟商品关联的sql.
		StringBuffer efunProductSql = new StringBuffer();
		efunProductSql.append(" INSERT INTO t_efun_product ");
		efunProductSql.append(" ( ");
		efunProductSql.append(" 	`product_id`, ");
		efunProductSql.append(" 	`join_time`, ");
		efunProductSql.append(" 	`update_time` ");
		efunProductSql.append(" ) ");
		efunProductSql.append(" VALUES ");
		// 标识新增幸运一折购跟商品SKU关联条数.
		int num = 0;
		List<Integer> productIdList = ProductSku.dao.findProductIdList(skuCodes);
		for(Integer productId : productIdList){
			// 先判断当前商品是否加入过幸运一折购.
			EfunProduct efunProduct = EfunProduct.dao.getEfunProductByProductId(productId);
			if(efunProduct!=null){
				// 幸运一折购详情访问次数得分(重新加入幸运一折购商品初始化使用).
				EfunProductScore.dao.score4ViewsCount(efunProduct.getInt("product_id"));
				// 当当前商品加入过幸运一折购时，直接更新原先的记录即可（因为原先的记录有浏览次数）.
				efunProduct.set("is_first", BaseConstants.NO); // 改成非首次加入.
				efunProduct.set("is_quit", BaseConstants.NO);
				efunProduct.set("join_time", new Date());
				efunProduct.set("update_time", new Date());
				efunProduct.update();
			}else{
				//【批量新增幸运一折购跟商品SKU关联的sql】.
				efunProductSql.append(" 	( ");
				efunProductSql.append(" ?, ");
				efunProductSql.append(" ?, ");
				efunProductSql.append(" ? ");
				efunProductSql.append(" 	),");
				efunProductParams.add(productId);
				efunProductParams.add(new Date());
				efunProductParams.add(new Date());
				num++;
			}
		}
		// 新增条数大于0才执行插入.
		if(num>0){
			// 新增幸运一折购跟商品关联.
			Db.update(efunProductSql.toString().substring(0, efunProductSql.toString().length()-1),efunProductParams.toArray());
		}
	}
	
	/**
	 * 退出幸运一折购
	 * @param efunSkuId 商品SKU跟幸运一折购关联ID
	 * @author Jacob
	 * 2016年5月30日下午5:13:42
	 */
	@Before(Tx.class)
	public void quitEfun(Integer efunSkuId){
		//判断当前退出幸运一折购的商品SKU所属的商品是否所有的SKU都退出了幸运一折购
		int productId = EfunSku.dao.findByIdLoadColumns(efunSkuId, "product_id").getInt("product_id");
		String sql = " SELECT COUNT(es.id) FROM t_efun_sku es WHERE es.is_quit = 0 AND es.product_id = ? ";
		long count = Db.queryLong(sql, productId);
		if(count==0){
			//逻辑删除
			String efunSql = " UPDATE t_efun_product SET is_quit = 1,is_recommend = 0,update_time = NOW() WHERE product_id = ?  ";
			Db.update(efunSql,productId);
		}
	}
	
	/**
	 * 更新浏览次数
	 * @param productId
	 * @author Jacob
	 * 2016年5月30日下午5:59:55
	 */
	public void viewEfunProduct(Integer productId) {
		String sql = "UPDATE t_efun_product  t SET t.views_count = t.views_count + 1 WHERE t.product_id = ?";
		Db.update(sql.toString(), productId);
	}
	
	/**
	 * 更新幸运一折购商品推荐标识
	 * @param isRecommend 推荐表示：0.否；1.是
	 * @param efunSkuId id
	 * @author Jacob
	 * 2016年1月23日下午1:27:26
	 */
	public void updateRecommend(Integer isRecommend,Integer productId){
		String sql = " update t_efun_product set is_recommend = ?,recommend_time = ? where product_id = ? ";
		Db.update(sql,isRecommend,new Date(),productId);
	}
	
	/**
	 * 根据店铺ID获取该店铺所有参加幸运一折购的商品列表
	 * @param shopId
	 * @return
	 * @author Jacob
	 * 2016年7月20日下午7:46:00
	 */
	public Page<Record> searchPage(Page<?> page, String shopId, String keyWord){
		List<Object> params = new ArrayList<Object>();
		StringBuffer selectSql = new StringBuffer();
		StringBuffer whereSql = new StringBuffer();
		selectSql.append(" SELECT ");
		selectSql.append(" 	p.id productId, ");
		selectSql.append(" 	p.`name` productName, ");
		selectSql.append(" 	p.product_img productImg, ");
		selectSql.append(" 	p.`no` productNo ");
		whereSql.append(" FROM ");
		whereSql.append(" 	t_efun_product ep, ");
		whereSql.append(" 	t_product p ");
		whereSql.append(" WHERE ");
		whereSql.append(" 	ep.product_id = p.id ");
		whereSql.append(" AND ep.is_quit = ? ");
		params.add(BaseConstants.NO);
		whereSql.append(" AND p.shop_id = ? ");
		params.add(shopId);
		if(StringUtil.notNull(keyWord)){
			whereSql.append(" AND (p.`name` LIKE CONCAT(CONCAT('%', ?),'%') OR p.`no` LIKE CONCAT(CONCAT('%', ?),'%')) ");
			params.add(keyWord);
			params.add(keyWord);
		}
		whereSql.append(" ORDER BY ");
		whereSql.append(" 	ep.join_time DESC ");
		return Db.paginate(page.getPageNumber(), page.getPageSize(), selectSql.toString(), whereSql.toString(),params.toArray());
	}
	
	/**
	 * 判断是否参加过幸运一折购的商品
	 * @param proId
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年6月3日上午12:15:47
	 */
	public boolean isEfunProduct(int proId){
		String sql = " SELECT count(1) FROM t_efun_product t where t.product_id = ? and t.is_quit = ? ";
		if(Db.queryLong(sql,proId, BaseConstants.NO)>0){
			return true;
		}
		return false;
	}
	
	
}
