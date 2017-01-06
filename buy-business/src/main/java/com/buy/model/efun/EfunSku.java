package com.buy.model.efun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.buy.common.BaseConstants;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.user.RecAddress;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
 * 幸运一折购商品sku申请表
 */
public class EfunSku extends Model<EfunSku>{
	
	/**
	 * 审批状态-审核中
	 */
	public static final int STATUS_APPROVING = 0;
	/**
	 * 审批状态-审核通过
	 */
	public static final int STATUS_PASS = 1;
	/**
	 * 审批状态-审核不通过
	 */
	public static final int STATUS_FAIL = 2;
	
	/**
	 * 是否后台添加-否
	 */
	public static final int IS_ADMIN_ADD_NO = 0;
	/**
	 * 是否后台添加-是
	 */
	public static final int IS_ADMIN_ADD_YES = 1;
	
	/**
	 * 是否自营-否
	 */
	public static final int IS_BELONG_EFUN_NO = 0;
	/**
	 * 是否自营-是
	 */
	public static final int IS_BELONG_EFUN_YES = 1;
	
	/**
	 * 幸运一折购价跟E趣价的比率
	 */
	public static final BigDecimal EFUN_PRICE_RATE = new BigDecimal("0.1");
	
	private static final long serialVersionUID = 1L;
	
	public static final EfunSku dao = new EfunSku();
	
	/**
	 * 获取商品SKU识别码
	 * @param efunSkuId 主键
	 * @return
	 * @author Jacob
	 * 2016年1月15日上午11:01:17
	 */
	public String getSkuCode(Integer efunSkuId){
		return EfunSku.dao.findByIdLoadColumns(efunSkuId, "sku_code").getStr("sku_code");
	}
	
	/**
	 * 根据skucode获取
	 * @param skuCode
	 * @return
	 * @author huangzq
	 * 2016年12月12日 下午3:45:28
	 *
	 */
	public EfunSku getBySkuCode(String skuCode){
		String sql = "select * from t_efun_sku where sku_code = ?";
		return EfunSku.dao.findFirst(sql,skuCode);
	}
	
	/**
	 * 退出幸运一折购
	 * @param efunSkuId
	 * @author Jacob
	 * 2016年1月15日下午1:55:59
	 */
	@Before(Tx.class)
	public String quitEfun(Integer efunSkuId){
		
		String skuCode = EfunSku.dao.getSkuCode(efunSkuId);
		
		//1.逻辑删除
		String efunSql = " UPDATE t_efun_sku SET is_quit = 1,is_recommend = 0,update_time = NOW() WHERE id = ?  ";
		Db.update(efunSql,efunSkuId);
		
		//2.更新对应的商品SKU加入幸运一折购标记
		String skuSql = " UPDATE t_pro_sku SET is_efun = 0 WHERE `code` = ? ";
		Db.update(skuSql,skuCode);
		
		//3.删除相关的幸运一折购得分记录
		String scoreSql = " DELETE s FROM t_efun_score s WHERE s.sku_code = ? ";
		Db.update(scoreSql,skuCode);
		
		//4.处理所属商品跟幸运一折购关联的退出幸运一折购标识
		EfunProduct.dao.quitEfun(efunSkuId);
		
		return skuCode;
	}
	
	/**
	 * 更新相同商品下幸运一折购商品SKU推荐标识
	 * @param isRecommend 推荐表示：0.否；1.是
	 * @param efunSkuId id
	 * @author Jacob
	 * 2016年1月23日下午1:27:26
	 */
	public void updateRecommend(Integer isRecommend,Integer productId){
		String sql = " update t_efun_sku set is_recommend = ?,recommend_time = ? where product_id = ? ";
		Db.update(sql,isRecommend,new Date(),productId);
	}
	
	/**
	 * 判断会员是否已参与了该期次该商品的幸运一折购
	 * @param userId
	 * @param efunId
	 * @param skuCode
	 * @return
	 * @author Jacob
	 * 2016年1月20日上午9:51:27
	 */
	public boolean isPartake(String userId,Integer efunId,Integer proId){
		String sql = " select count(id) from t_efun_user_order where efun_id = ? and product_id = ? and user_id = ? and (cash > 0 or use_integral > 0 or number is not null) ";
		Long count = Db.queryLong(sql,efunId,proId,userId);
		if(count>0){
			return true;
		}
		return false;
	}
	
	/**
	 * 删除未支付的记录(不包含在线支付)
	 * @param userId
	 * @param efunId
	 * @param skuCode
	 * @author Jacob
	 * 2016年1月23日下午3:00:33
	 */
	public void deleteNotPay(String userId,Integer efunId,String skuCode){
		String sql = " delete euo from t_efun_user_order euo where euo.efun_id = ? and euo.sku_code = ? and euo.user_id = ? and euo.cash = 0 and euo.use_integral = 0 and euo.number is not null ";
		Db.update(sql,efunId,skuCode,userId);
	}
	
	/**
	 * 根据商品Sku识别码获取相应的幸运一折购跟商品Sku关联记录
	 * @param skuCode 商品sku识别码
	 * @return
	 * @author Jacob
	 * 2016年2月15日下午4:23:33
	 */
	public EfunSku getEfunSkuBySkuCode(String skuCode){
		String sql = " SELECT * FROM t_efun_sku where sku_code = ?  ";
		return dao.findFirst(sql,skuCode);
	}
	
	/**
	 * 判断会员是否是第一次参与幸运一折购
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年4月11日上午11:38:40
	 */
	public boolean isFirstApply(String shopId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	esa.id ");
		sql.append(" FROM ");
		sql.append(" 	t_efun_sku_apply esa ");
		sql.append(" LEFT JOIN t_product p ON esa.product_id = p.id ");
		sql.append(" WHERE ");
		sql.append(" 	p.shop_id = ?");
		return null == Db.findFirst(sql.toString(), shopId)?true:false;
	}
	
	/**
	 * 获取幸运一折购商品SKU信息
	 * @param skuCode 商品SKU识别码
	 * @param addressId 收货地址id
	 * @param count 购买数量
	 * @return
	 * @author Jacob
	 * 2016年1月15日下午5:15:39
	 */
	public Record getEfunSkuInfo(String skuCode, Integer addressId, Integer count){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	ps.`code` skuCode, ");
		sql.append(" 	IFNULL(ps.sku_img,p.product_img) productImg, ");
		sql.append(" 	p.`name` productName, ");
		sql.append(" 	ps.property_decs propertyValues, ");
		sql.append(" 	ROUND(ps.eq_price,2) eqPrice, ");
		sql.append("    ROUND(CEIL(ps.eq_price * "+EfunSku.EFUN_PRICE_RATE+"*100)/100,2) efunPrice, ");
		sql.append("    p.id productId, ");
		sql.append("    p.source productSource, ");
		sql.append("    p.is_free_postage isFreePostage, ");//是否包邮
		sql.append("    ps.supplier_price supplierPrice, ");
		sql.append("    s.id merchantId, ");
		sql.append("    s.`no` merchantNo, ");
		sql.append("    s.`name` merchantName ");
		sql.append(" FROM ");
		sql.append(" 	t_pro_sku ps ");
		sql.append(" LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append(" LEFT JOIN t_shop s ON s.id = p.shop_id ");
		sql.append(" WHERE ");
		sql.append("  `p`.`source` IN ("+Product.SOURCE_EXCLUSIVE+", "+Product.SOURCE_SELF_EXCLUSIVE+") ");
		sql.append(" AND ps.`code` = ? ");
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" 	ps.`code` skuCode, ");
		sql.append(" 	IFNULL(ps.sku_img,p.product_img) productImg, ");
		sql.append(" 	p.`name` productName, ");
		sql.append(" 	ps.property_decs propertyValues, ");
		sql.append(" 	ROUND(ps.eq_price,2) eqPrice, ");
		sql.append("    ROUND(CEIL(ps.eq_price * "+EfunSku.EFUN_PRICE_RATE+"*100)/100,2) efunPrice, ");
		sql.append("    p.id productId, ");
		sql.append("    p.source productSource, ");
		sql.append("    p.is_free_postage isFreePostage, ");//是否包邮
		sql.append("    ps.supplier_price supplierPrice, ");
		sql.append("    s.id merchantId, ");
		sql.append("    s.`no` merchantNo, ");
		sql.append("    s.`name` merchantName ");
		sql.append(" FROM ");
		sql.append(" 	t_pro_sku ps ");
		sql.append(" LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append(" LEFT JOIN t_supplier s ON s.id = p.supplier_id ");
		sql.append(" WHERE ");
		sql.append("  `p`.`source` IN ("+Product.SOURCE_SELF_PUBLIC+", "+Product.SOURCE_FACTORY+", "+Product.SOURCE_FACTORY_SEND+") ");
		sql.append(" AND ps.`code` = ? ");
		Record efunSkuInfo = Db.findFirst(sql.toString(),skuCode,skuCode);;
		//运费计算所需map
		Map<String,Integer> sukCodeCountMap = new HashMap<String,Integer>();
		sukCodeCountMap.put(skuCode, count);
		//初始化订单运费
		BigDecimal freight = new BigDecimal("0");
		if(addressId!=null){
			RecAddress recAddress = RecAddress.dao.findByIdLoadColumns(addressId, "province_code, city_code, area_code");
			//计算该组运费
			if(recAddress!=null){
				freight = FreightTemplate.dao.calculate(sukCodeCountMap, recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
				freight = freight.setScale(2,BigDecimal.ROUND_CEILING);
			}
		}
		efunSkuInfo.set("freight", freight);
		return efunSkuInfo;
	}
	
	/**
	 * 根据商品SKU识别码判断幸运一折购与商品SKU管理记录是否存在
	 * @param skuCode 商品SKU识别码
	 * @return
	 * @author Jacob
	 * 2016年2月17日下午6:25:27
	 */
	public boolean isEfunSkuExist(String skuCode){
		String sql = " SELECT * FROM t_efun_sku where is_quit = ? AND sku_code = ? ";
		EfunSku efunSku = EfunSku.dao.findFirst(sql,BaseConstants.NO,skuCode);
		if(efunSku!=null){
			return true;
		}
		return false;
	}
	
	/**
	 * 批量新增幸运一折购跟商品SKU关联
	 * @author Jacob
	 * 2016年5月30日下午1:28:25
	 */
	public void batchAdd(String[] skuCodes,String adminId,int isAdmin){
		List<Object> efunSkuParams = new ArrayList<Object>();
		// 批量新增幸运一折购跟商品SKU关联的sql.
		StringBuffer efunSkuSql = new StringBuffer();
		efunSkuSql.append(" INSERT INTO t_efun_sku ");
		efunSkuSql.append(" ( ");
		efunSkuSql.append(" 	`sku_code`, ");
		efunSkuSql.append(" 	`product_id`, ");
		efunSkuSql.append(" 	`join_time`, ");
		if(StringUtil.notNull(adminId)){
			efunSkuSql.append(" 	`admin_id`, ");
			efunSkuSql.append(" 	`is_admin_add`, ");
		}
		efunSkuSql.append(" 	`update_time` ");
		efunSkuSql.append(" ) ");
		efunSkuSql.append(" VALUES ");
		// 标识新增幸运一折购跟商品SKU关联条数.
		int num = 0;
		// Sku对应的商品ID列表.
		HashSet<Integer> list = new HashSet<Integer>(); 
		for(String skuCode : skuCodes){
			// 根据商品SKU识别码获取商品ID.
			int productId = ProductSku.dao.findByIdLoadColumns(skuCode, "product_id").getInt("product_id");
			// 先判断当前商品SKU是否加入过幸运一折购.
			EfunSku efunSku = EfunSku.dao.getEfunSkuBySkuCode(skuCode);
			if(efunSku!=null){
				if(null != efunSku.getInt("product_id")) {
					// 幸运一折购详情访问次数得分(重新加入幸运一折购商品初始化使用).
					EfunProductScore.dao.score4ViewsCount(efunSku.getInt("product_id"));
					
					list.add(efunSku.getInt("product_id"));
				}
				// 当当前商品SKU加入过幸运一折购时，直接更新原先的记录即可（因为原先的记录有浏览次数）.
				efunSku.set("is_first", BaseConstants.NO); // 改成非首次加入.
				efunSku.set("is_quit", BaseConstants.NO);
				efunSku.set("join_time", new Date());
				efunSku.set("update_time", new Date());
				efunSku.update();
			}else{
				//【批量新增幸运一折购跟商品SKU关联的sql】.
				efunSkuSql.append(" 	( ");
				efunSkuSql.append(" ?, ");
				efunSkuSql.append(" ?, ");
				efunSkuSql.append(" ?, ");
				if(StringUtil.notNull(adminId)){
					efunSkuSql.append(" ?, ");
					efunSkuSql.append(" ?, ");
				}
				efunSkuSql.append(" ? ");
				efunSkuSql.append(" 	),");
				efunSkuParams.add(skuCode);
				efunSkuParams.add(productId);
				efunSkuParams.add(new Date());
				if(StringUtil.notNull(adminId)){
					efunSkuParams.add(adminId);
					efunSkuParams.add(isAdmin);
				}
				efunSkuParams.add(new Date());
				num++;
				list.add(productId);
			}
		}
		// 新增条数大于0才执行插入.
		if(num>0){
			// 新增幸运一折购跟商品SKU关联.
			Db.update(efunSkuSql.toString().substring(0, efunSkuSql.toString().length()-1),efunSkuParams.toArray());
		}
		// 批量初始化幸运一折购得分.
		EfunProductScore.dao.batchInit((Integer[]) list.toArray(new Integer[list.size()]));
	}
	
	/**
	 * 根据
	 * @param productId
	 * @return
	 * @author Jacob
	 * 2016年7月20日下午8:01:47
	 */
	public List<Record> findList(Integer productId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	IFNULL(ps.property_decs,'单SKU商品'), ");
		sql.append(" 	es.join_time ");
		sql.append(" FROM ");
		sql.append(" 	t_efun_sku es, ");
		sql.append(" 	t_pro_sku ps ");
		sql.append(" WHERE ");
		sql.append(" 	ps.`code` = es.sku_code ");
		sql.append(" AND es.is_quit = ? ");
		sql.append(" AND es.product_id = ? ");
		sql.append(" ORDER BY ");
		sql.append(" 	es.join_time DESC ");
		return Db.find(sql.toString(), BaseConstants.NO, productId);
	}
	
}