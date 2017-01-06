package com.buy.model.product;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.buy.common.BaseConfig;
import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.encryption.MD5Builder;
import com.buy.model.efun.EfunSku;
import com.buy.model.order.Order;
import com.buy.model.order.OrderDetail;
import com.buy.model.shop.Shop;
import com.buy.model.store.StoreSkuMap;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ProductSku extends Model<ProductSku> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final ProductSku dao = new ProductSku();
	private Logger L = Logger.getLogger(ProductSku.class);

	/**
	 * 进驻云店
	 */
	public static final int IS_O2O = 1;

	/**
	 * 未进驻云店
	 */
	public static final int IS_NOT_O2O = 0;

	/**
	 * 是幸运一折购商品
	 */
	public static final int IS_EFUN = 1;
	/**
	 * 发货规则:1.自动分配
	 */
	public static final int RULE_AUTO = 1;
	/**
	 * 发货规则:2.指定发货
	 */
	public static final int RULE_APPOINT = 2;
	/**
	 * 发货规则:3.自行发货
	 */
	public static final int RULE_SELF = 3;
	/**
	 * 发货规则数量
	 */
	public static final int RULE_COUNT = 3;

	/**
	 * 生成skuCode
	 * 
	 * @param propertyValue
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public String generateCode(String propertyValue, Integer productId) {

		String result = "";
		if (StringUtil.notNull(propertyValue)) {
			List<String> values = new ArrayList<String>();
			for (String v : propertyValue.split("#@!@#")) {
				values.add(v);
			}
			Collections.sort(values);
			for (String v : values) {
				result += v;
				result += "#@!@#";
			}
			result = result.substring(0, result.lastIndexOf("#@!@#"));
			result += "-" + productId;
		} else {
			result += productId;
		}
		return MD5Builder.getMD5Str(result);
	}

	/**
	 * 生成属性描述
	 * 
	 * @param code
	 * @return
	 * @author huangzq
	 */
	public String generatePropertyDecs(String code) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT	IFNULL(");
		sql.append("		GROUP_CONCAT(pv.`value` ORDER BY pv.property_id asc),");
		sql.append("		''");
		sql.append("	)");
		sql.append(" FROM");
		sql.append("	t_sku_value_map m");
		sql.append(" LEFT JOIN t_pro_property_value pv ON m.value_id = pv.id");
		sql.append(" WHERE");
		sql.append("	m.sku_code = ?");
		sql.append(" GROUP BY	m.sku_code");

		return Db.queryStr(sql.toString(), code);
	}

	/**
	 * 商品是否有上架云店
	 * 
	 * @param id
	 * @return
	 * @author huangzq
	 */
	public boolean isO2o(String code) {
		long count = Db.queryLong("SELECT  count(1)  FROM t_o2o_sku_map osm WHERE osm.sku_code = ?", code);
		if (count > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 清理商品sku
	 * 
	 * @param productId
	 * @param noDeleteIds
	 * @author huangzq
	 */
	public List<String> clearSku(int productId, List<String> noDeleteCodes) {
		String deleteCodeSql = "select code from t_pro_sku where product_id = ? and code not in(";
		deleteCodeSql += StringUtil.listToStringForSql(",", noDeleteCodes) + ")";
		List<String> deleteCodes = Db.query(deleteCodeSql, productId);
		if (StringUtil.notNull(deleteCodes)) {
			// 删除sku
			String deleteSkuSql = "delete from t_pro_sku where code in(";
			deleteSkuSql += StringUtil.listToStringForSql(",", deleteCodes) + ")";
			Db.update(deleteSkuSql);
			// 删除库存
			String storeSql = "delete from t_store_sku_map where sku_code in(";
			storeSql += StringUtil.listToStringForSql(",", deleteCodes) + ")";
			Db.update(storeSql);
			Date now = new Date();
			// 添加删除记录
			for (String code : deleteCodes) {
				Record r = new Record();
				r.set("sku_code", code);
				r.set("update_time", now);
				Db.save("t_sku_delete_record", r);
			}
		}
		// 清除无效的映射
		SkuValueMap.dao.clearDisableMap();

		return deleteCodes;
	}

	/**
	 * 根据商品SKU识别码获取订单SKU（包含商家信息、商品信息、商品sku信息、商品属性及属性值信息）
	 * 
	 * @param skuCode
	 *            商品SKU识别码
	 * @return
	 * @author Jacob 2016年3月4日下午4:15:44
	 */
	public Record getOrderSku(String skuCode) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	`p`.`shop_id` AS `target_id`, ");
		sql.append(" 	`s`.`name` AS `target_name`, ");
		sql.append(" 	`s`.`no` AS `target_no`, ");
		sql.append(" 	`ps`.`code` AS `sku_code`, ");
		sql.append(" 	`ps`.`product_id` AS `product_id`, ");
		sql.append(" 	`p`.`no` AS `product_no`, ");
		sql.append(" 	`p`.`name` AS `product_name`, ");
		sql.append(" 	`p`.`source` AS `source`, ");
		sql.append(" 	IFNULL(`ps`.`sku_img`,`p`.`product_img`) product_img, ");
		sql.append(" 	ABS(`ps`.`eq_price`) AS `sku_price`, ");
		sql.append(" 	ABS(`ps`.`market_price`) AS `market_price`, ");
		sql.append(" 	ABS(`ps`.`supplier_price`) AS `supplier_price`, ");
		sql.append(" 	`ps`.`property_decs` AS `properties`, ");
		sql.append(" 	`p`.`is_recycle` AS `is_recycle` ");
		sql.append(" FROM ");
		sql.append(" 	`t_pro_sku` `ps` ");
		sql.append(" LEFT JOIN `t_product` `p` ON `ps`.`product_id` = `p`.`id` ");
		sql.append(" LEFT JOIN `t_shop` `s` ON `s`.`id` = `p`.`shop_id` ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND `p`.`source` IN (" + Product.SOURCE_EXCLUSIVE + ", " + Product.SOURCE_SELF_EXCLUSIVE + ") ");
		sql.append(" AND ps.`code` = ? ");
		sql.append(" UNION ");
		sql.append(" SELECT ");
		sql.append(" 	`p`.`supplier_id` AS `target_id`, ");
		sql.append(" 	`s`.`name` AS `target_name`, ");
		sql.append(" 	`s`.`no` AS `target_no`, ");
		sql.append(" 	`ps`.`code` AS `sku_code`, ");
		sql.append(" 	`ps`.`product_id` AS `product_id`, ");
		sql.append(" 	`p`.`no` AS `product_no`, ");
		sql.append(" 	`p`.`name` AS `product_name`, ");
		sql.append(" 	`p`.`source` AS `source`, ");
		sql.append(" 	IFNULL(`ps`.`sku_img`,`p`.`product_img`) product_img, ");
		sql.append(" 	ABS(`ps`.`eq_price`) AS `sku_price`, ");
		sql.append(" 	ABS(`ps`.`market_price`) AS `market_price`, ");
		sql.append(" 	ABS(`ps`.`supplier_price`) AS `supplier_price`, ");
		sql.append(" 	`ps`.`property_decs` AS `properties`, ");
		sql.append(" 	`p`.`is_recycle` AS `is_recycle` ");
		sql.append(" FROM ");
		sql.append(" 	`t_pro_sku` `ps` ");
		sql.append(" LEFT JOIN `t_product` `p` ON `ps`.`product_id` = `p`.`id` ");
		sql.append(" LEFT JOIN `t_supplier` `s` ON `s`.`id` = `p`.`supplier_id` ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND `p`.`source` IN (" + Product.SOURCE_SELF_PUBLIC + ", " + Product.SOURCE_FACTORY + ", "
				+ Product.SOURCE_FACTORY_SEND + ") ");
		sql.append(" AND ps.`code` = ? ");
		return Db.findFirst(sql.toString(), skuCode, skuCode);
	}

	/**
	 * 获取sku属性描述
	 * 
	 * @param code
	 * @return
	 * @author huangzq
	 */
	public String getPropertyDecs(String code) {
		String sql = "select GROUP_CONCAT(value) from v_com_sku_property where skuCode = ? group by skuCode";
		return Db.queryFirst(sql, code);
	}

	/**
	 * 生成wap商品skuUrl
	 * 
	 * @param productId
	 * @return
	 * @throws @author
	 *             Eriol
	 * @date 2016年4月5日下午8:16:11
	 */
	public String generateWapUrl(Integer productId) {
		String productUrl = BaseConfig.globalProperties.get("wap.product.url");
		return productUrl + productId;
	}

	/**
	 * 获取商品SKU图片
	 * 
	 * @param skuCodesStr
	 *            商品SKU识别码数组字符串（“,”连接）
	 * @return
	 * @author Jacob 2016年4月9日下午5:11:48
	 */
	public List<String> findImgList(String skuCodesStr) {
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	IFNULL(ps.sku_img,p.product_img) img ");// 商品图片
		sql.append(" FROM ");
		sql.append(" 	t_pro_sku ps ");
		sql.append(" LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append(" WHERE ");
		sql.append(" 	ps.`code` IN (");
		for (String skuCode : skuCodesStr.split(",")) {
			sql.append("?,");
			params.add(skuCode);
		}
		sql = sql.deleteCharAt(sql.length() - 1);
		sql.append("	) ");
		return Db.query(sql.toString(), params.toArray());
	}

	/**
	 * 根据skuCode获取proId
	 * 
	 * @return
	 */
	public Integer getProIdBySku(String skuCode) {
		String sql = "SELECT product_id productId FROM t_pro_sku ps WHERE ps.`code` = ?";
		return Db.findFirst(sql, skuCode).getInt("productId");
	}

	/**
	 * 根据proId获取skuCode
	 * 
	 * @return
	 */
	public String getSkuByProId(int proId) {
		String sql = "SELECT ps.`code` FROM t_pro_sku ps WHERE ps.product_id = ? AND ps.is_efun = ?";
		List<Record> lists = Db.find(sql, proId, IS_EFUN);
		if (lists.size() == 1) {
			return lists.get(0).getStr("code");
		} else {
			return null;
		}
	}

	/**
	 * 更新商品SKU是否加入幸运一折购标识
	 * 
	 * @param skuParams
	 * @author Jacob 2016年5月30日下午1:21:54
	 */
	public void updateIsEfun(String[] skuCodes, int shopOradmin) {
		List<Object> skuParams = new ArrayList<Object>();
		// 更新商品SKU是否加入幸运一折购标识.
		StringBuffer skuSql = new StringBuffer(" update t_pro_sku set is_efun = " + shopOradmin + " where code in ( ");
		for (String skuCode : skuCodes) {
			// 【更新商品SKU是否加入幸运一折购标识】.
			skuSql.append("?,");
			skuParams.add(skuCode);
		}
		// 更新商品SKU加入幸运一折购标记.
		skuSql = skuSql.deleteCharAt(skuSql.length() - 1);
		skuSql.append(") ");
		Db.update(skuSql.toString(), skuParams.toArray());
	}

	/**
	 * 根据skuCode获取商品状态
	 * 
	 * @param skuCode
	 * @return
	 * @throws @author
	 *             Eriol
	 * @date 2016年5月31日下午4:42:40
	 */
	public Record getProductStatusBySkuCode(String skuCode) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("	p.id productId, ");
		sql.append("	p.`no` productNo, ");
		sql.append("	p.lock_status, ");
		sql.append("	p.audit_status,");
		sql.append("	p.`status`");
		sql.append("FROM ");
		sql.append("	t_product p,");
		sql.append("	t_pro_sku ps ");
		sql.append("WHERE ");
		sql.append("	ps.product_id = p.id ");
		sql.append("AND ps.`code` = ? ");
		return Db.findFirst(sql.toString(), skuCode);
	}

	/**
	 * 获取商品的状态 4：找不到商品 5：商品已下架 6：商品已被锁定 7：商品未审核通过 8：商品对应店铺已冻结 0：正常
	 * 
	 * @param skuCode
	 * @return
	 * @author chenhg
	 */
	public int getProdcutStatusBySku(String skuCode) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("	p.lock_status, ");
		sql.append("	p.audit_status,");
		sql.append("	p.`status`,");
		sql.append("	ifnull(s.forbidden_status,0) shopStatus ");
		sql.append("FROM ");
		sql.append("	t_pro_sku ps");
		sql.append("	LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append("	LEFT JOIN t_shop s ON	p.shop_id = s.id  ");
		sql.append("WHERE ");
		sql.append("	ps.`code` = ? ");
		Record product = Db.findFirst(sql.toString(), skuCode);
		// 找不到对应的商品
		if (product == null) {
			return 4;
			// 商品已下架
		} else if (product.getInt("status") != Product.STATUS_SHELVE) {
			return 5;
			// 商品已被锁定
		} else if (product.getInt("lock_status") != Product.LOCK_STATUS_ENABLE) {
			return 6;
			// 商品未审核通过
		} else if (product.getInt("audit_status") != Product.AUDIT_STATUS_SUCCESS) {
			return 7;
			// 商品对应店铺已冻结
		} else if (product.getNumber("shopStatus").intValue() != Shop.FORBIDDEN_STATUS_NORMAL) {
			return 8;
		}

		return 0;
	}

	/**
	 * 检查商品SKU的商品状态
	 * 
	 * @param skuCode
	 * @return
	 * @author Jacob 2016年6月1日下午4:50:30
	 */
	public JsonMessage checkProductStatus(String skuCode) {
		JsonMessage jsonMessage = new JsonMessage();
		Record product = this.getProductStatusBySkuCode(skuCode);
		if (product.getInt("status") != Product.STATUS_SHELVE) {
			jsonMessage.setData(skuCode);
			jsonMessage.setStatusAndMsg("34", "部分商品已下架，请到购物车修改");
			return jsonMessage;
		}
		if (product.getInt("lock_status") != Product.LOCK_STATUS_ENABLE) {
			jsonMessage.setData(skuCode);
			jsonMessage.setStatusAndMsg("35", "部分商品已锁定，请到购物车修改");
		}
		return jsonMessage;
	}

	/**
	 * 获取商品ID（去重）
	 * 
	 * @param skuCodes
	 *            商品SKU识别码数组
	 * @return
	 * @author Jacob 2016年6月2日下午7:25:06
	 */
	public List<Integer> findProductIdList(String[] skuCodes) {
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT DISTINCT ");
		sql.append(" 	ps.product_id ");
		sql.append(" FROM ");
		sql.append(" 	t_pro_sku ps ");
		sql.append(" WHERE ");
		sql.append(" 	ps.`code` IN ( ");
		for (String skuCode : skuCodes) {
			sql.append("?,");
			params.add(skuCode);
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(" ) ");
		return Db.query(sql.toString(), params.toArray());
	}

	/**
	 * 单个生成真实使用的条形码
	 * 
	 * @param barCode
	 * @param isChange
	 * @return
	 * @throws @author
	 *             Eriol
	 * @date 2016年7月20日下午3:46:11
	 */
	public synchronized String generateRealBarcode(String barCode, boolean isChange) {
		if (StringUtil.notNull(barCode)) {
			long count = 0;
			// 如果不是直接修改则进行匹配
			if (!isChange) {
				// 比对code是否存在--增加限制检查未删除商品的sku
				String sql = "SELECT count(1) FROM 	t_pro_sku t,t_product t2 where t.product_id = t2.id AND t2.`status` !=? AND t.real_barcode = ?";
				count = Db.queryLong(sql, Product.STATUS_DELETE, barCode);
			}
			// 判断是否含有重复code或者是直接修改
			if (isChange || count > 0) {
				barCode = "e" + (new Date().getTime() - 1300000000000L);// 保持一个字母+12位数字
			}
		} else {
			return null;
		}
		return barCode;
	}

	/**
	 * 根据商品Id获取skuCode列表.
	 * 
	 * @return
	 */
	public List<Record> getSkuCodesByProductId(Integer productId) {
		String sql = "SELECT t.`code` FROM t_pro_sku t WHERE t.product_id = ?";
		return Db.find(sql, productId);
	}

	/**
	 * 对比数据获取sku销售表待删除的sku
	 * 
	 * @param productId
	 * @return
	 * @throws @author
	 *             Eriol
	 * @date 2016年9月7日下午5:22:44
	 */
	public List<Record> getDeleteSku4SaleCount(Integer productId) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("	t.sku_code ");
		sql.append("FROM ");
		sql.append("	( ");
		sql.append("		SELECT ");
		sql.append("			t1.sku_code, ");
		sql.append("			t2.`code` ");
		sql.append("		FROM ");
		sql.append("			t_sku_sales_count t1 ");
		sql.append("		LEFT JOIN t_pro_sku t2 ON t1.sku_code = t2.`code` ");
		sql.append("		WHERE t1.product_id = ? ");
		sql.append("	) t WHERE t.`code` is NULL ");
		return Db.find(sql.toString(), productId);
	}

	/**
	 * 获取sku销售表待增加的sku
	 * 
	 * @param productId
	 * @return
	 * @throws @author
	 *             Eriol
	 * @date 2016年9月7日下午7:14:28
	 */
	public List<Record> getAddSku4SaleCount(Integer productId) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("	t.`code` sku_code ");
		sql.append("FROM ");
		sql.append("	( ");
		sql.append("		SELECT ");
		sql.append("			t2.sku_code, ");
		sql.append("			t1.`code` ");
		sql.append("		FROM ");
		sql.append("			t_pro_sku t1 ");
		sql.append("		LEFT JOIN t_sku_sales_count t2 ON t2.sku_code = t1.`code` ");
		sql.append("		WHERE t1.product_id = ? ");
		sql.append("	) t WHERE t.sku_code is NULL ");
		return Db.find(sql.toString(), productId);
	}

	/**
	 * 根据商品SKU识别码判断商品SKU是否存在
	 * 
	 * @param skuCode
	 * @return
	 * @author Jacob 2016年10月5日下午2:58:03
	 */
	public boolean isExist(String skuCode) {
		String sql = "SELECT t.`code` FROM t_pro_sku t WHERE t.`code` = ?";
		return StringUtil.notNull(Db.find(sql, skuCode));
	}

	public boolean isEfun(String skuCode) {
		String sql = "SELECT t.`is_efun` FROM t_pro_sku t WHERE t.`code` = ?";
		int isEfun = Db.queryInt(sql, skuCode);
		if (isEfun == IS_EFUN) {
			return true;
		}
		return false;
	}

	/**
	 * 清除商品的真是条码
	 * 
	 * @author Eriol
	 * @date 2016年10月15日下午4:30:06
	 * @param proId
	 */
	public void clearSkuRealBarCode(Integer proId) {
		String sql = "UPDATE t_pro_sku t SET t.real_barcode = null WHERE t.product_id = ? ";
		Db.update(sql, proId);
	}

	/**
	 * 获取某个sku价格
	 * 
	 * @param skuCode
	 * @return
	 */
	public BigDecimal getSkuMarketPriceByCode(String skuCode) {
		return Db.queryBigDecimal("select market_price from t_pro_sku where code = ? ", skuCode);
	}

	/**
	 * 获取某个sku的发货规则
	 * 
	 * @param skuCode
	 * @return
	 */
	public Integer getDeliverRuleByCode(String skuCode) {
		return Db.queryInt("select deliver_rule from t_pro_sku where code = ?", skuCode);
	}

	/**
	 * 获取某个SKU的指定发货云店编号
	 * 
	 * @return
	 */
	public String getAppointStoreNo(String skuCode) {
		return Db.queryStr("select appoint_store_no from t_pro_sku where code = ?", skuCode);
	}

	/**
	 * 判断显示库存是否充足（下单）
	 * 
	 * @param storeNo
	 * @param count
	 *            下单数量
	 * @return
	 */
	public boolean enoughCount(String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		ProductSku productSku = dao.findById(skuCode);
		if (productSku == null) {
			return false;
		} else {
			int realCount = StringUtil.isNull(productSku.getInt("real_count")) ? 0 : productSku.getInt("real_count");
			// 显示库存 = 虚拟库存 + 真实库存 - 商品锁定库存 - 仓库(云店)锁定库存
			int skuCount = productSku.getInt("virtual_count") + realCount - productSku.getInt("lock_count")
					- productSku.getInt("store_lock_count");
			if (skuCount < count) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 判断实际库存是否充足（发货）
	 * 
	 * @param storeNo
	 * @param count
	 *            下单数量
	 * @return
	 */
	public boolean enoughRealCountForSend(String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		ProductSku productSku = dao.findById(skuCode);
		if (productSku == null) {
			return false;
		} else {
			int realCount = StringUtil.isNull(productSku.getInt("real_count")) ? 0 : productSku.getInt("real_count");
			if (realCount < count) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 判断虚拟库存是否充足（发货）
	 * 
	 * @param storeNo
	 * @param count
	 *            下单数量
	 * @return
	 */
	public boolean enoughVirtualCountForSend(String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		ProductSku productSku = dao.findById(skuCode);
		if (productSku == null) {
			return false;
		} else {
			if (productSku.getInt("virtual_count") < count) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 调用： 1、购物车提交订单 增加锁定库存 2、未完全支付，跳到第三方支付，确定支付时，增加锁定库存
	 * 
	 * @param deliveryType：1：快递
	 *            ；2：自提
	 * @param storeNo
	 * @param orderId
	 * @return
	 */
	public JsonMessage addLockCountForOrderId(int deliveryType, String storeNo, String orderId) {
		JsonMessage jsonMessage = new JsonMessage();
		// 获取订单明细的商品SKU识别码和购买数量列表
		List<Record> skuList = OrderDetail.dao.findSkuCodeList(orderId);
		List<Record> skuInventory = new ArrayList<Record>();
		boolean flag = false;
		// 自提：增加仓库锁定存数
		if (deliveryType == Order.DELIVERY_TYPE_SELF) {// 自提，增加仓库锁定库存
			for (Record sku : skuList) {
				String skuCode = sku.getStr("skuCode");
				int count = sku.getInt("count");
				if (!addStoreLockCount(storeNo, skuCode, count)) {
					flag = true;
					skuInventory.add(ProductSku.dao.getSkuInventoryMessageForPickUp(skuCode, storeNo));
				}
			}
			// 快递：增加商品锁定库存数
		} else {
			for (Record sku : skuList) {
				String skuCode = sku.getStr("skuCode");
				int count = sku.getInt("count");
				if (!addLockCount(skuCode, count)) {
					flag = true;
					skuInventory.add(ProductSku.dao.getSkuInventoryMessage(skuCode));
				}
			}
		}
		if (flag) {
			jsonMessage.setData(skuInventory);
			jsonMessage.setStatusAndMsg("10", "库存不足");
		}
		return jsonMessage;
	}

	/**
	 * 取消订单，扣减锁定库存
	 * 
	 * @param deliveryType
	 * @param storeNo
	 * @param orderId
	 * @return
	 * @author chenhg 2016年11月18日 下午2:04:11
	 */
	public JsonMessage subtractLockCountForOrderId(int deliveryType, String storeNo, String orderId) {
		JsonMessage jsonMessage = new JsonMessage();
		StringBuffer skuCodesStr = new StringBuffer();
		// 获取订单明细的商品SKU识别码和购买数量列表
		List<Record> skuList = OrderDetail.dao.findSkuCodeList(orderId);
		// 自提：增加仓库锁定存数
		if (deliveryType == Order.DELIVERY_TYPE_SELF) {// 自提，增加仓库锁定库存
			for (Record sku : skuList) {
				String skuCode = sku.getStr("skuCode");
				int count = sku.getInt("count");
				if (!subtractStoreLockCount(storeNo, skuCode, count)) {
					skuCodesStr.append(skuCode);
					skuCodesStr.append(",");
				}
			}
			// 快递：增加商品锁定库存数
		} else {
			for (Record sku : skuList) {
				String skuCode = sku.getStr("skuCode");
				int count = sku.getInt("count");
				if (!subtractLockCount(skuCode, count)) {
					skuCodesStr.append(skuCode);
					skuCodesStr.append(",");
				}
			}
		}
		if (StringUtil.notBlank(skuCodesStr.toString())) {
			jsonMessage.setData(skuCodesStr.toString().substring(0, skuCodesStr.length() - 1));
			jsonMessage.setStatusAndMsg("10", "扣减失败");
		}
		return jsonMessage;
	}

	/**
	 * 增加：商品锁定库存
	 * 
	 * @param skuCode
	 * @param count
	 * @return
	 */
	public boolean addLockCount(String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		boolean result = false;
		while (!result) {
			ProductSku productSku = dao.findById(skuCode);
			if (productSku == null) {
				return false;
			} else {
				int realCount = StringUtil.isNull(productSku.getInt("real_count")) ? 0
						: productSku.getInt("real_count");
				// 显示库存 = 虚拟库存 + 真实库存 - 商品锁定库存 - 仓库(云店)锁定库存
				int skuCount = productSku.getInt("virtual_count") + realCount - productSku.getInt("lock_count")
						- productSku.getInt("store_lock_count");
				if (skuCount >= count) {
					StringBuffer updateSql = new StringBuffer();
					updateSql.append(" UPDATE t_pro_sku SET lock_count = lock_count + ? ,");
					updateSql.append(" version = version + 1,update_time = now()");
					updateSql.append(" WHERE");
					updateSql.append(" version = ?");
					updateSql.append(" AND code = ?");
					int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), skuCode);
					// 更新成功
					if (resultCount != 0) {
						result = true;
					}
				} else {
					return false;
				}
			}
		}

		return result;
	}

	/**
	 * 减少：商品锁定库存
	 * 
	 * @param skuCode
	 * @param count
	 * @return
	 */
	public boolean subtractLockCount(String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		boolean result = false;
		while (!result) {
			ProductSku productSku = dao.findById(skuCode);
			if (productSku == null) {
				return false;
			} else {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET lock_count = lock_count - ? ,");
				updateSql.append(" version = version + 1,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), skuCode);
				// 更新成功
				if (resultCount != 0) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * 增加：仓库(云店)锁定库存
	 * 
	 * @param skuCode
	 * @param count
	 * @return
	 */
	public boolean addStoreLockCount(String storeNo, String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		// 增加云店
		boolean isOk = StoreSkuMap.dao.addStoreLockCount(storeNo, skuCode, count);
		if (!isOk) {
			return false;
		}
		boolean result = false;
		while (!result) {
			ProductSku productSku = dao.findById(skuCode);
			if (productSku == null) {
				return false;
			} else {
				int realCount = StringUtil.isNull(productSku.getInt("real_count")) ? 0 : productSku.getInt("real_count");
				// 真实库存 - 仓库(云店)锁定库存
				int skuCount = realCount - productSku.getInt("store_lock_count");
				if (skuCount >= count) {
					StringBuffer updateSql = new StringBuffer();
					updateSql.append(" UPDATE t_pro_sku SET store_lock_count = store_lock_count + ? ,");
					updateSql.append(" version = version + 1,update_time = now()");
					updateSql.append(" WHERE");
					updateSql.append(" version = ?");
					updateSql.append(" AND code = ?");
					int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), skuCode);
					// 更新成功
					if (resultCount != 0) {
						result = true;
					}
				} else {
					return false;
				}
			}
		}
		return result;
	}

	/**
	 * 减少：仓库(云店)锁定库存
	 * 
	 * @param skuCode
	 * @param count
	 * @return
	 */
	public boolean subtractStoreLockCount(String storeNo, String skuCode, int count) {
		// 防止非法操作
		if (count < 1) {
			return false;
		}
		// 减少云店
		boolean isOk = StoreSkuMap.dao.subtractStoreLockCount(storeNo, skuCode, count);
		if (!isOk) {
			return false;
		}
		boolean result = false;
		while (!result) {
			ProductSku productSku = dao.findById(skuCode);
			if (productSku == null) {
				return false;
			} else {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET store_lock_count = store_lock_count - ? ,");
				updateSql.append(" version = version + 1,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), skuCode);
				// 更新成功
				if (resultCount != 0) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * 增加仓库(云店)锁定库存，减去商品锁定库存（快递选择商城发货）
	 * 
	 * @param storeNo
	 * @param skuCodeCountList
	 * @return
	 * @throws SQLException
	 */
	public boolean transferLockCountForMany(String storeNo, List<Record> skuCodeCountList) throws SQLException {
		for (Record r : skuCodeCountList) {
			String skuCode = r.getStr("skuCode");
			Integer count = r.getInt("count");
			if (!transferLockCount(storeNo, skuCode, count)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 增加仓库(云店)锁定库存，减去商品锁定库存（快递选择商城发货）
	 * 
	 * @param storeNo
	 * @param skuCode
	 * @param count
	 * @return
	 * @throws SQLException
	 */
	public boolean transferLockCount(String storeNo, String skuCode, int count) throws SQLException {
		// 增加仓库(云店)锁定库存
		boolean isOk = addStoreLockCount(storeNo, skuCode, count);
		if (!isOk) {
			return false;
		}
		boolean result = false;
		while (!result) {
			ProductSku productSku = dao.findById(skuCode);
			if (productSku == null) {
				return false;
			} else {
				if (productSku.getInt("lock_count") <= 0) {
					return true;
				}
				if (productSku.getInt("lock_count") >= count) {
					StringBuffer updateSql = new StringBuffer();
					updateSql.append(" UPDATE t_pro_sku SET lock_count = lock_count - ? ,");
					updateSql.append(" version = version + 1,update_time = now()");
					updateSql.append(" WHERE");
					updateSql.append(" version = ?");
					updateSql.append(" AND code = ?");
					int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), skuCode);
					// 更新成功
					if (resultCount != 0) {
						result = true;
					}
				} else {
					return false;
				}
			}
		}
		return result;
	}

	/**
	 * 批量检查云店是否可以发货
	 * 
	 * @return
	 * @throws SQLException
	 */
	/*
	 * public boolean checkStoreDeliverForMany(String storeNo, List<Record>
	 * skuCodeCountList) throws SQLException { for(Record r :skuCodeCountList ){
	 * String skuCode = r.getStr("skuCode"); Integer count = r.getInt("count");
	 * if(!checkTransferLockCount(storeNo, skuCode, count)){ return false; } }
	 * return true; }
	 * 
	 * public boolean checkTransferLockCount(String storeNo, String skuCode, int
	 * count) { //增加仓库(云店)锁定库存 boolean isOk = addStoreLockCount(storeNo,
	 * skuCode, count); if(!isOk){ return false; } boolean result = false;
	 * while(!result){ ProductSku productSku = dao.findById(skuCode);
	 * if(productSku==null){ return false; }else{
	 * if(productSku.getInt("lock_count") >= count){ result = true; }else{
	 * return false; } } } return result; }
	 */

	/**
	 * 获取库存(sku)
	 * 
	 * @param code
	 * @return
	 * @author chenhg
	 */
	public Integer getCount(String code) {

		Record record = Db.findFirst(
				"SELECT IFNULL(real_count,0) + virtual_count - lock_count - store_lock_count AS total FROM t_pro_sku WHERE `code` = ?",
				code);
		if (record == null || StringUtil.isNull(record.getNumber("total"))) {
			return 0;
		} else {
			return record.getNumber("total").intValue();
		}
	}

	/**
	 * 添加真实库存
	 * 
	 * @param code
	 * @param count
	 * @param storeNo
	 * @return 成功返回true,sku不存在返回false
	 * @author huangzq
	 */
	public boolean plusRealCount(String code, Integer count, String storeNo) {
		// 添加对应的仓库库存
		StoreSkuMap.dao.plusCount(code, storeNo, count);
		// 添加对应sku库存
		boolean result = false;
		while (result == false) {
			ProductSku productSku = dao.findById(code);
			if (productSku != null) {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET  real_count = if(real_count is null ,0,real_count) + ? ,");
				updateSql.append(" version = version + 1 ,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), code);
				// 更新成功
				if (resultCount != 0) {
					result = true;
				}
			} else {
				L.error("sku已被删除，不需要增加真实库存");
				return true;
				// throw new RuntimeException("SKU不存在");
			}
		}
		return true;

	}

	/**
	 * 发货：减去实际库存，减去仓库锁定库存（单个sku）
	 * 
	 * @param storeNo
	 * @param skuCode
	 * @param count
	 * @return
	 * @throws SQLException
	 */
	public boolean subtractRealCount(String storeNo, String skuCode, int count) throws SQLException {

		boolean isSuccess = StoreSkuMap.dao.subsubtractCount(storeNo, skuCode, count);
		if (isSuccess == false) {
			return false;
		}

		boolean flag = false;
		while (flag == false) {
			ProductSku productSku = dao.findById(skuCode);
			if (productSku == null) {
				L.error("sku已被删除，不需要扣真实库存,skuCode:" + skuCode);
				return true;
			}
			if (productSku.getInt("real_count") != null && productSku.getInt("real_count") >= count) {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET real_count = real_count - ? ,");
				updateSql.append(" store_lock_count = store_lock_count - ?,");
				updateSql.append(" version = version + 1,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, count, productSku.getInt("version"), skuCode);
				// 更新成功
				if (resultCount != 0) {
					flag = true;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 发货：减去实际库存，减去仓库锁定库存（多个sku）
	 * 
	 * @param code
	 * @param count
	 * @param storeNo
	 * @return
	 * @author huangzq
	 * @throws SQLException
	 */
	public boolean subtractRealCountForMany(String storeNo, List<Record> skuCodeCountList) throws SQLException {

		boolean isSuccess = StoreSkuMap.dao.subtractCountForMany(storeNo, skuCodeCountList);
		if (isSuccess == false) {
			return false;
		}
		for (Record r : skuCodeCountList) {
			String skuCode = r.getStr("skuCode");
			Integer count = r.getInt("count");
			boolean flag = false;
			while (flag == false) {
				ProductSku productSku = dao.findById(skuCode);
				if (productSku == null) {
					L.error("sku已被删除，不需要扣真实库存,skuCode:" + skuCode);
					break;
				}
				if (productSku.getInt("real_count") != null && productSku.getInt("real_count") >= count) {
					StringBuffer updateSql = new StringBuffer();
					updateSql.append(" UPDATE t_pro_sku SET real_count = real_count - ? ,");
					updateSql.append(" store_lock_count = store_lock_count - ?,");
					updateSql.append(" version = version + 1,update_time = now()");
					updateSql.append(" WHERE");
					updateSql.append(" version = ?");
					updateSql.append(" AND code = ?");
					int resultCount = Db.update(updateSql.toString(), count, count, productSku.getInt("version"),
							skuCode);
					// 更新成功
					if (resultCount != 0) {
						flag = true;
					}
				} else {
					// 库存不足
					return false;
				}
			}
		}

		return true;

	}

	/**
	 * 添加虚拟库存
	 * 
	 * @param code
	 * @param count
	 * @param storeNo
	 * @return
	 * @author huangzq
	 */
	public boolean plusVirtualCount(String code, Integer count) {
		boolean result = false;
		while (result == false) {
			ProductSku productSku = dao.findById(code);
			if (productSku != null) {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET virtual_count = virtual_count + ? ,");
				updateSql.append(" version = version + 1");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), code);
				// 更新成功
				if (resultCount != 0) {
					result = true;
				}
			} else {
				// 数据已被删除
				L.error("sku已被删除，不需要增加虚拟库存");
				return true;
			}
		}
		return true;

	}

	/**
	 * 发货：减少虚拟库存，减少商品锁定库存（单个sku）--非超卖订单
	 * 
	 * @param code
	 * @param count
	 * @param storeNo
	 * @return
	 * @author huangzq
	 */
	public boolean subtractVirtualCount(String code, Integer count) throws SQLException {
		boolean result = false;
		while (result == false) {
			ProductSku productSku = dao.findById(code);
			if (productSku == null) {
				// 数据被删
				L.error("sku已被删除，不需要扣虚拟库存");
				return true;
			}
			if (productSku.getInt("virtual_count") >= count) {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET virtual_count = virtual_count - ? ,");
				updateSql.append(" lock_count = lock_count - ? ,");
				updateSql.append(" version = version + 1");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, count, productSku.getInt("version"), code);
				// 更新成功
				if (resultCount != 0) {
					result = true;
				}
			} else {
				// 库存不足
				return false;
			}
		}
		return true;

	}

	/**
	 * 发货：减少虚拟库存（单个sku）--超卖订单
	 * 
	 * @param code
	 * @param count
	 * @param storeNo
	 * @return
	 * @author huangzq
	 */
	public boolean subtractVirtualCountForOversold(String code, Integer count) throws SQLException {
		boolean result = false;
		while (result == false) {
			ProductSku productSku = dao.findById(code);
			if (productSku == null) {
				// 数据被删
				L.error("sku已被删除，不需要扣虚拟库存");
				return true;
			}
			if (productSku.getInt("virtual_count") >= count) {
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET virtual_count = virtual_count - ? ,");
				updateSql.append(" version = version + 1");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(), count, productSku.getInt("version"), code);
				// 更新成功
				if (resultCount != 0) {
					result = true;
				}
			} else {
				// 库存不足
				return false;
			}
		}
		return true;

	}

	/**
	 * 发货：减少虚拟库存，减少商品锁定库存（多个sku）--非超卖订单
	 * 
	 * @param skuCodeCountList
	 * @return
	 * @throws SQLException
	 */
	public boolean subtractVirtualCountForMany(List<Record> skuCodeCountList) throws SQLException {

		for (Record r : skuCodeCountList) {
			String skuCode = r.getStr("skuCode");
			Integer count = r.getInt("count");

			if (!subtractVirtualCount(skuCode, count)) {
				return false;
			}
		}

		return true;

	}

	/**
	 * 发货：减少虚拟库存（多个sku）--超卖订单
	 * 
	 * @param skuCodeCountList
	 * @return
	 * @throws SQLException
	 */
	public boolean subtractVirtualCountForManyAndOversold(List<Record> skuCodeCountList) throws SQLException {

		for (Record r : skuCodeCountList) {
			String skuCode = r.getStr("skuCode");
			Integer count = r.getInt("count");

			if (!subtractVirtualCountForOversold(skuCode, count)) {
				return false;
			}
		}

		return true;

	}

	/**
	 * 更新商品发货规则
	 * 
	 * @param skuCode
	 *            sku码
	 * @param rule
	 *            规则
	 * @param storeNo
	 *            云店编号
	 * @return
	 */
	public boolean updateProductDeliverRule(String skuCode, Integer rule, String storeNo) {
		ProductSku update = new ProductSku();
		if (rule == ProductSku.RULE_AUTO || rule == ProductSku.RULE_SELF) {
			update.set("code", skuCode).set("deliver_rule", rule);
			update.set("appoint_store_no", null);
		} else if (rule == ProductSku.RULE_APPOINT) {
			update.set("code", skuCode).set("deliver_rule", rule);
			update.set("appoint_store_no", storeNo);
		} else {
			return false;
		}
		return update.update();
	}

	/**
	 * 第三方确认付款时，扣库存失败，返回库存信息(非自提)
	 * 
	 * @param skuCode
	 * @return
	 * @author chenhg 2016年11月19日 上午10:08:00
	 */

	public Record getSkuInventoryMessage(String skuCode) {

		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  p.`name` proName, ");
		sql.append("  IFNULL(ps.property_decs,'') properties, ");
		sql.append("  p.id productId,");
		sql.append("  if(ps.virtual_count + IFNULL(ps.real_count,0) - ps.lock_count - ps.store_lock_count < 0,0,");
		sql.append("  ps.virtual_count + IFNULL(ps.real_count,0) - ps.lock_count - ps.store_lock_count) ");
		sql.append("  as storeCount");
		sql.append(" FROM t_pro_sku ps ");
		sql.append(" LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append(" WHERE ps.`code` = ?");

		return Db.findFirst(sql.toString(),skuCode);
	}

	/**
	 * 第三方确认付款时，扣库存失败，返回库存信息(自提)
	 * @param skuCode
	 * @param storeNo
	 * @return
	 * @author huangzq
	 * 2017年1月4日 下午4:58:47
	 *
	 */
	public Record getSkuInventoryMessageForPickUp(String skuCode, String storeNo) {

		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  p.`name` proName, ");
		sql.append("  IFNULL(ps.property_decs,'') properties, ");
		sql.append("  if(ssm.pro_count - ssm.store_lock_count < 0,0,ssm.pro_count - ssm.store_lock_count) storeCount ");
		sql.append(" FROM t_pro_sku ps "); 
		sql.append("  LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append("  LEFT JOIN t_store_sku_map ssm ON ps.`code` = ssm.sku_code ");
		sql.append(" WHERE ps.`code` = ? ");
		sql.append("  AND ssm.store_no = ? ");

		Record result = Db.findFirst(sql.toString(), skuCode, storeNo);
	
		return result;

	}

	/**
	 * 商家发货时，库存不足，返回（虚拟）库存信息
	 * 
	 * @param skuCode
	 * @return
	 * @author chenhg 2016年11月19日 上午10:08:00
	 */
	public Record getSkuInventoryMessageForSend(String skuCode) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  p.`name` proName, ");
		sql.append("  IFNULL(ps.property_decs,'无') proDescs, ");
		sql.append("  CONCAT(p.`name`,' ',IFNULL(ps.property_decs,'')) sku,");
		sql.append("  p.id productId,");
		sql.append("  p.sort_id sortId,");
		sql.append("  ps.virtual_count as total");
		sql.append(" FROM t_pro_sku ps ");
		sql.append(" LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append(" WHERE ps.`code` = ? ");

		return Db.findFirst(sql.toString(), skuCode);
	}

	/**
	 * 商家发货时，库存不足，返回云店库存信息
	 * 
	 * @param skuCode
	 * @return
	 * @author chenhg 2016年11月19日 上午10:08:00
	 */
	public Record getSkuInventoryMessageForSendAndPickUp(String skuCode, String storeNo) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  p.`name` proName, ");
		sql.append("  IFNULL(ps.property_decs,'无') proDescs, ");
		sql.append("  CONCAT(p.`name`,' ',IFNULL(ps.property_decs,'')) sku,");
		sql.append("  p.id productId,");
		sql.append("  p.sort_id sortId,");
		sql.append("  ssm.pro_count total ");
		sql.append(" FROM t_pro_sku ps ");
		sql.append("  LEFT JOIN t_product p ON ps.product_id = p.id ");
		sql.append("  LEFT JOIN t_store_sku_map ssm ON ps.`code` = ssm.sku_code ");
		sql.append(" WHERE ps.`code` = ? ");
		sql.append("  AND ssm.store_no = ? ");

		return Db.findFirst(sql.toString(), skuCode, storeNo);
	}

	/**
	 * 验证商品是否都有库存
	 * 
	 * @param skuCodes
	 * @return
	 * @author chenhg 2016年5月22日 上午10:06:52
	 */
	public boolean allHasCount(String[] skuCodes) {
		for (String skuCode : skuCodes) {
			int count = ProductSku.dao.getCount(skuCode);
			if (count < 1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 判断某个sku是否有虚拟库存
	 * 
	 * @param skuCode
	 * @return
	 * @author chenhg 2016年5月20日 上午11:57:22
	 */
	public boolean hasCount(String skuCode) {
		if (ProductSku.dao.getCount(skuCode) > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 下单，锁定库存时获取相关的信息
	 * 
	 * @param skuCode
	 * @return
	 * @author huangzq 2016年12月21日 下午1:32:38
	 *
	 */
	public Record getSkuForSubmitOrder(String skuCode) {

		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append("	p.`name` as proName,");
		sql.append("	p.`id` as productId,");
		sql.append("	p.`status` as proStatus,");
		sql.append("	p.lock_status as proLockStatus,");
		sql.append("	p.source as proSource ,");
		sql.append("	shop.id as shopId ,");
		sql.append("	shop.no as shopNo ,");
		sql.append("	shop.name as shopName ,");
		sql.append("	shop.status as shopStatus ,");
		sql.append("	shop.forbidden_status as shopForbiddenStatus ,");
		sql.append("	supplier.id as supplierId ,");
		sql.append("	supplier.no as supplierNo ,");
		sql.append("	supplier.name as supplierName ,");
		sql.append("	sku.eq_price/10 as efunPrice,");
		sql.append("	sku.sku_img as productImg,");
		sql.append("	ifnull(sku.property_decs,'') as properties,");
		sql.append("	sku.eq_price as eqPrice,");
		sql.append("	sku.eq_price/10 as efunPrice,");
		sql.append("	sku.supplier_price as supplierPrice,");
		sql.append("	sku.virtual_count+sku.real_count-sku.lock_count-sku.store_lock_count as storeCount");
		sql.append(" FROM");
		sql.append("  t_pro_sku sku  ");
		sql.append(" LEFT JOIN t_product p ON sku.product_id = p.id");
		sql.append(" LEFT JOIN t_shop shop ON shop.id = p.shop_id ");
		sql.append(" LEFT JOIN t_supplier supplier ON supplier.id = p.supplier_id ");
		sql.append(" WHERE");

		sql.append(" sku.code = ?");
		sql.append(" order by sku.eq_price asc");
		return Db.findFirst(sql.toString(), skuCode);

	}

	/**
	 * 商品详情--销售属性页的SKU信息
	 * 
	 * @param skuCode
	 * @return
	 * @author chenhg 2016年3月16日 上午9:12:04
	 */
	public Record getSkuMessage(String skuCode, int productId) {
		Record record = null;
		StringBuffer sql = new StringBuffer();
		if (StringUtil.notNull(skuCode)) {
			sql.append(" SELECT ");
			sql.append(" a.product_id productId, ");
			sql.append(" a.eq_price, ");
			sql.append(" b.product_img, ");
			sql.append(" a.property_decs, ");

			sql.append(" a.virtual_count+ifnull(a.real_count,0) - a.lockCount - a.storeLockCount as count");
			sql.append(" FROM v_com_sku a ");
			sql.append(" LEFT JOIN t_product b ON a.product_id = b.id ");
			sql.append(" where a.`code` = ? ");

			record = Db.findFirst(sql.toString(), skuCode);
			List<String> valueIds = Db.query("select valueId from v_com_sku_property where skuCode = ? ", skuCode);
			record.set("valueIds", valueIds.toArray());
			// 处理幸运一折购价格
			String eqPriceStr = record.get("eq_price").toString();
			BigDecimal eqPrice = new BigDecimal(eqPriceStr);
			// 保留两位小数，向上取整
			BigDecimal efunPrice = eqPrice.multiply(EfunSku.EFUN_PRICE_RATE,
					new MathContext(eqPriceStr.length() - 2, RoundingMode.CEILING));
			record.set("eqPrice", efunPrice);
		} else {
			sql.append(" select  ");
			sql.append(" a.id productId, ");
			sql.append(" a.eq_price, ");
			sql.append(" a.product_img, ");
			sql.append(" '未选择属性' as property_decs, ");
			sql.append(" 0 as count, ");
			sql.append(" null as valueIds ");
			sql.append(" from t_product a where a.id = ? ");
			record = Db.findFirst(sql.toString(), productId);
		}
		return record;
	}

	/**
	 * 获取商品详情--介绍
	 */
	public Record getProductIntroduce(int proId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append("  c.`name` brandName, ");
		sql.append("  b.introduce, ");
		sql.append("  b.detail ");
		sql.append(" FROM t_pro_sku a ");
		sql.append("  LEFT JOIN t_product b ON a.product_id = b.id ");
		sql.append("  LEFT JOIN t_pro_brand c on b.brand_id = c.id ");
		sql.append(" where a.product_id = ?  ");
		return Db.findFirst(sql.toString(), proId);
	}

	/**
	 * 获取商品详情--普通属性信息
	 */
	public List<Record> getNoSellProperties(int proId) {
		// 获取商品普通属性
		StringBuffer sql = new StringBuffer();
		sql.append(" select propertyName,value ");
		sql.append("  from t_pro_sku a  ");
		sql.append("  left join v_com_property b ");
		sql.append("  on a.product_id = b.productId  ");
		sql.append(" where b.isSell = ? and a.product_id = ?   ");
		sql.append(" and a.is_efun = ?    ");
		List<Record> properties = Db.find(sql.toString(), BaseConstants.NO, proId, BaseConstants.YES);
		return properties;
	}
	
	
	
	
	
	
	/**
	 * 检查商品状态(与getSkuForSubmitOrder()联合使用)
	 * @param skuList
	 * @return
	 * @author huangzq
	 * 2016年12月15日 下午4:40:12
	 *
	 */
	public JsonMessage checkSku(Record... skuList){
		JsonMessage jsonMessage = new JsonMessage();
		List<String> productNames = new ArrayList<String>();
		for(Record sku : skuList){
			//判断商品状态
			if(sku.get("efunPrice")==null||sku.getInt("proStatus")!=Product.STATUS_SHELVE
				||sku.getInt("proLockStatus")!=Product.LOCK_STATUS_ENABLE){
				productNames.add(sku.getStr("proName")+" "+sku.getStr("properties"));	
				
			}else{
				int proSource = sku.getInt("proSource");
				if(proSource==Product.SOURCE_EXCLUSIVE||proSource==Product.SOURCE_SELF_EXCLUSIVE){
					//判断店铺状态
					if(sku.get("shopStatus")==null||sku.getInt("shopStatus")!=Shop.STATUS_ACTIVATED||sku.getInt("shopForbiddenStatus")!=Shop.FORBIDDEN_STATUS_NORMAL){
						productNames.add(sku.getStr("proName")+" "+sku.getStr("properties"));	
					}
				}
			}
			
			
		}
		if(StringUtil.notNull(productNames)){
			jsonMessage.setData(productNames);
			jsonMessage.setStatusAndMsg("4", "部分商品已下架");
		}
		return jsonMessage;
	}
	
	
	
	/**
	 * 获取Sku对应的商品状态和店铺状态.
	 * 
	 * @param skuCode
	 *            sku编码.
	 * 
	 * @author Chengyb
	 * @return product_id：商品Id<br>
	 *         status：商品状态<br>
	 *         lock_status：商品锁定状态<br>
	 *         shop_id：店铺Id<br>
	 *         shop_status：店铺状态<br>
	 *         forbidden_status：店铺禁用状态
	 */
	public Record getProductStatusAndShopStatus(String skuCode) {
		return Db.findFirst("SELECT s.product_id, t.status, t.lock_status, p.id AS shop_id, p.status AS shop_status, p.forbidden_status AS shop_forbidden_status FROM t_pro_sku s LEFT JOIN t_product t ON s.product_id = t.id LEFT JOIN t_shop p ON t.shop_id = p.id WHERE s.code=?", skuCode);
	}
	
	/**
	 * 获取Sku对应的商品来源.
	 * 
	 * @param skuCode
	 *            sku编码.
	 * 
	 * @author Chengyb
	 * @return 1：店铺专卖商品，2：自营专卖商品，3：自营公共商品，4：E趣代销s商品，5：厂家自发商品.
	 */
	public Integer getProductSource(String skuCode) {
		return Db.queryInt("SELECT t.source FROM t_pro_sku s LEFT JOIN t_product t ON s.product_id = t.id WHERE s.code=?", skuCode);
	}
	
}