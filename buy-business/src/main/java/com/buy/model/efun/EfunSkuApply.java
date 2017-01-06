package com.buy.model.efun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.model.product.Product;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;


/**
 * 幸运一折购商品sku申请表
 */
public class EfunSkuApply extends Model<EfunSkuApply>{
	
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
	 * 商品类型-专卖商品
	 */
	public static final int PRODUCT_TYPE_EXCLUSIVE = 1;
	/**
	 * 商品类型-公共商品
	 */
	public static final int PRODUCT_TYPE_PUBLIC = 2;
	
	
	/**
	 * 幸运一折购价跟E趣价的比率
	 */
	public static final BigDecimal EFUN_PRICE_RATE = new BigDecimal("0.1");
	
	private static final long serialVersionUID = 1L;
	
	public static final EfunSkuApply dao = new EfunSkuApply();
	
	/**
	 * 保存商品SKU加入幸运一折购申请
	 * @param skuCode 商品SKU识别码
	 * @return
	 * @author Jacob
	 * 2016年1月7日下午5:04:48
	 */
	public boolean add(String skuCode){
		EfunSkuApply efunSku = new EfunSkuApply();
		efunSku.set("sku_code", skuCode);
		efunSku.set("status", STATUS_APPROVING);//审批状态
		efunSku.set("apply_time", new Date());//申请时间
		efunSku.set("update_time", new Date());
		return efunSku.save();
	}
	
	/**
	 * 获取商品SKU识别码
	 * @param efunSkuId 主键
	 * @return
	 * @author Jacob
	 * 2016年1月15日上午11:01:17
	 */
	public String getSkuCode(Integer efunSkuId){
		return EfunSkuApply.dao.findByIdLoadColumns(efunSkuId, "sku_code").getStr("sku_code");
	}
	
	/**
	 * 根据商品SKU识别码获取幸运一折购商品所需的关联信息
	 * @param skuCode
	 * @return
	 * @author Jacob
	 * 2016年3月1日下午3:48:55
	 */
	public Record getEfunProductInfo(String skuCode){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append("	ps.`code` skuCode, ");
		sql.append("	p.id productId, ");
		sql.append("	p.`no` productNo, ");
		sql.append("	p.`name` productName,");
		sql.append(" 	group_concat(	");
		sql.append(" 		DISTINCT `pv`.`propertyName`	");
		sql.append(" 		ORDER BY	");
		sql.append(" 			`pv`.`propertyId` ASC SEPARATOR ','	");
		sql.append(" 	) propertyNames,	");//商品属性名组合
		sql.append(" 	group_concat(	");
		sql.append(" 		DISTINCT `pv`.`value`	");
		sql.append(" 		ORDER BY	");
		sql.append(" 			`pv`.`propertyId` ASC SEPARATOR ','	");
		sql.append(" 	) propertyValues,	");//商品属性值组合
		sql.append(" 	ps.eq_price eqPrice, ");
		sql.append(" 	ROUND(CEIL(ps.eq_price * "+EfunSku.EFUN_PRICE_RATE+"*100)/100,2) efunPrice,	 ");
		sql.append(" 	p.source productSource, ");
		sql.append(" 	IF((p.source = 1) OR (p.source = 2), shop.id, supplier.id) merchantId, ");//商家ID（店铺/供货商ID）
		sql.append(" 	IF((p.source = 1) OR (p.source = 2), shop.no, supplier.no) merchantNo, ");//商家编号（店铺/供货商编号）
		sql.append(" 	IF((p.source = 1) OR (p.source = 2), shop.name, supplier.name) merchantName ");//商家名称（店铺/供货商名称）
		sql.append(" FROM	");
		sql.append(" 	t_pro_sku ps ");
		sql.append(" LEFT JOIN t_product p ON p.id = ps.product_id	");
		sql.append(" LEFT JOIN v_com_sku_property pv ON pv.skuCode = ps.`code`	");
		sql.append(" LEFT JOIN t_shop shop ON shop.id = p.shop_id	");
		sql.append(" LEFT JOIN t_supplier supplier ON supplier.id = p.supplier_id	");
		sql.append(" WHERE	");
		sql.append(" 	1 = 1	");
		sql.append(" AND ");
		sql.append(" 	ps.`code` = ? ");
		return Db.findFirst(sql.toString(),skuCode);
	}
	
	/**
	 * 批量新增
	 * @param skuCodes 商品SKU识别码数组
	 * @param adminId 管理后台添加人员ID
	 * @author Jacob
	 * 2016年5月30日下午3:26:20
	 */
	public void batchAdd(String[] skuCodes,String adminId,int applyStatus,int optType){
		List<Object> params = new ArrayList<Object>();
		// 批量新增幸运一折购跟商品SKU关联申请记录的sql.
		StringBuffer sql = new StringBuffer();
		sql.append(" INSERT INTO t_efun_sku_apply ");
		sql.append(" ( ");
		sql.append(" 	`sku_code`, ");//商品sku识别码
		sql.append(" 	`status`, ");//审批状态
		sql.append(" 	`apply_time`, ");//申请时间
		sql.append(" 	`audit_time`, ");//审批时间
		if(StringUtil.notNull(adminId)){
			sql.append(" 	`admin_id`, ");//审批人（添加人）
			sql.append(" 	`is_admin_add`, ");//是否后台添加
		}
		sql.append(" 	`product_id`, ");//产品主键ID
		sql.append(" 	`product_no`, ");//产品编号
		sql.append(" 	`product_name`, ");//商品名称
		sql.append(" 	`product_names`, ");//商品销售属性(属性名称)
		sql.append(" 	`product_values`, ");//商品销售属性(属性值)
		sql.append(" 	`eq_price`, ");//商品SKU e趣价格
		sql.append(" 	`price`, ");//幸运一折购价（商品SKUe趣价的10%）
		sql.append(" 	`is_belong_efun`, ");//是否自营
		sql.append(" 	`product_type`, ");//商品类型（1:专卖商品；2：公共商品)
		sql.append(" 	`merchant_id`, ");//商家ID(店铺或供货商)（根据字段product_type商品类型区分）
		sql.append(" 	`merchant_no`, ");//商家编号
		sql.append(" 	`merchant_name` ");//商家名称
		sql.append(" ) ");
		sql.append(" VALUES ");
		for(String skuCode : skuCodes){
			// 根据商品SKU识别码获取幸运一折购商品所需的关联信息.
			Record record = EfunSkuApply.dao.getEfunProductInfo(skuCode);
			//【批量新增幸运一折购跟商品SKU关联申请记录的sql】.
			sql.append(" 	( ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			if(StringUtil.notNull(adminId)){
				sql.append(" ?, ");
				sql.append(" ?, ");
			}
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ?, ");
			sql.append(" ? ");
			sql.append(" 	),");
			params.add(skuCode);
			params.add(applyStatus);
			params.add(new Date());
			params.add(new Date());
			if(StringUtil.notNull(adminId)){
				params.add(adminId);
				params.add(optType);
			}
			params.add(record.get("productId"));
			params.add(record.get("productNo"));
			params.add(record.get("productName"));
			params.add(record.get("propertyNames"));
			params.add(record.get("propertyValues"));
			params.add(record.get("eqPrice"));
			params.add(record.get("efunPrice"));
			// 判断是否自营.
			if(record.getInt("productSource")==Product.SOURCE_SELF_EXCLUSIVE||record.getInt("productSource")==Product.SOURCE_SELF_PUBLIC){
				params.add(EfunSkuApply.IS_BELONG_EFUN_YES);
			}else{
				params.add(EfunSkuApply.IS_BELONG_EFUN_NO);
			}
			// 判断是否专卖商品.
			if(record.getInt("productSource")==Product.SOURCE_EXCLUSIVE || record.getInt("productSource")==Product.SOURCE_SELF_EXCLUSIVE){
				params.add(EfunSkuApply.PRODUCT_TYPE_EXCLUSIVE);
			}else{
				params.add(EfunSkuApply.PRODUCT_TYPE_PUBLIC);
			}
			params.add(record.get("merchantId"));
			params.add(record.get("merchantNo"));
			params.add(record.get("merchantName"));
			
		}
		// 新增幸运一折购跟商品SKU关联申请记录.
		Db.update(sql.toString().substring(0, sql.toString().length()-1),params.toArray());
	}
	
	public EfunSkuApply getNewestBySkuCode(String skuCode){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT * FROM t_efun_sku_apply a WHERE a.sku_code = ? ORDER BY a.apply_time DESC; ");
		return dao.findFirst(sql.toString(), skuCode);
	}
	
}
