package com.buy.service.pos.push;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.encryption.MD5Builder;
import com.buy.model.pos.PushPosRecord;
import com.buy.model.product.Product;
import com.buy.string.StringUtil;
import com.google.gson.Gson;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class PushPosProduct {
	
	/*
	 * 参数
	 */
	
	static Logger L = Logger.getLogger(PushPosProduct.class);
	
	private String key;
	
	private Integer proId;
	private Integer[] proIds;
	private List<Integer> proIdList;
	private List<Integer> proIdDelList;
	
	private String sku;
	private String[] skus;
	private List<String> skuList;
	private List<String> skuDelList;
	
	public final static String OP_PRO_NEW = "product_new";
	public final static String OP_PRO_EDI = "product_edi";
	public final static String OP_PRO_DEL = "product_del";
	
	public final static String OP_PRO_O2O_NEW = "product_o2o_new";
	public final static String OP_PRO_O2O_EDI = "product_o2o_edi";
	public final static String OP_PRO_O2O_DEL = "product_o2o_del";
	
	public final static String OP_PRO_PUB_NEW = "product_pub_new";
	public final static String OP_PRO_PUB_EDI = "product_pub_edi";
	public final static String OP_PRO_PUB_DEL = "product_pub_del";
	
	public final static String OP_SKU_EDI = "sku_edi";
	public final static String OP_SKU_DEL = "sku_del";
	
	public final static String OP_SHOP_FROZEN = "shop_frozen";
	
	public final static String REQ_PRO_UPDATE = "updateProduct";
	public final static String REQ_SKU_UPDATE = "updateSku";
	public final static String REQ_SKU_DELETE = "deleteSku";
	
	public void push() {
		
		/*
		 * 商品增加
		 */
		
		// 普通商品新增
		if (OP_PRO_NEW.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId)) {
				String proIdsStr = proId + "";
				pushProduct(proIdsStr);
				pushSkuByProduct(proIdsStr);
			}
		
		// O2O商品增加
		} else if (OP_PRO_O2O_NEW.equals(key)) {
		
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId))
				pushProductO2o(proId + "");
			L.info("商品SKU:" + sku);
			if (StringUtil.notNull(sku))
				pushSkuO2o("'" + sku + "'");
			if (StringUtil.notNull(proIds)) {
				String proIdsStr = StringUtil.arrayToString(",", proIds);
				L.info("商品ID:" + proIdsStr);
				pushProductO2o(proIdsStr);
			}
			if (StringUtil.notNull(skus)) {
				String skusStr = StringUtil.arrayToStringForSql(",", skus);
				L.info("商品SKU:" + skusStr);
				pushSkuO2o(skusStr);
			}
			
		// 公共商品增加
		} else if (OP_PRO_PUB_NEW.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId)) {
				String proIdsStr = proId + "";
				pushProductPub(proIdsStr);
				if (StringUtil.isNull(skuList))		// web添加
					pushSkuByProduct(proIdsStr);
			}
			if (StringUtil.notNull(skuList)) {
				String skusStr = StringUtil.listToStringForSql(",", skuList);
				L.info("商品SKU:" + skusStr);
				pushSkuPub(skusStr);
			}
		
		/*
		 * 商品修改
		 */	
			
		// 普通商品修改
		} else if (OP_PRO_EDI.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId))
				pushProduct(proId + "");
			if (StringUtil.notNull(skuList)) {
				String skusStr = StringUtil.listToStringForSql(",", skuList);
				L.info("商品SKU:" + skusStr);
				pushSku(skusStr);
			}
			if (StringUtil.notNull(proId) && StringUtil.notNull(skuDelList)) {
				String skusDelStr = StringUtil.listToStringForSql(",", skuDelList);
				L.info("商品删除SKU:" + skusDelStr);
				pushSkuDel(proId + "", skuDelList);
			}
			
			
		// O2O商品修改
		} else if (OP_PRO_O2O_EDI.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId))
				pushProductO2o(proId + "");
			L.info("商品SKU:" + sku);
			if (StringUtil.notNull(sku))
				pushSkuO2o("'" + sku + "'");
		
		// 公共商品修改
		} else if (OP_PRO_PUB_EDI.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId))
				pushProductPub(proId + "");
			if (StringUtil.notNull(skuList)) {
				String skusStr = StringUtil.listToStringForSql(",", skuList);
				L.info("商品SKU:" + skusStr);
				pushSkuPub(skusStr);
			}
			if (StringUtil.notNull(proId) && StringUtil.notNull(skuDelList)) {
				String skusDelStr = StringUtil.listToStringForSql(",", skuDelList);
				L.info("商品删除SKU:" + skusDelStr);
				pushSkuDelPub(proId + "", skuDelList);
			}
			
		/*
		 * 商品删除
		 */
			
		// 普通商品删除
		} else if (OP_PRO_DEL.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId))
				pushProduct(proId + "");
			if (StringUtil.notNull(proIdDelList)) {
				String proIdsStr = StringUtil.listToString(",", proIdDelList);
				L.info("商品ID:" + proIdsStr);
				pushProduct(proIdsStr);
			}
		
		// O2O商品删除
		} else if (OP_PRO_O2O_DEL.equals(key)) {
			
			L.info("商品SKU:" + sku);
			if (StringUtil.notNull(proId) && StringUtil.notNull(sku)) {
				List<String> skuDelList = new ArrayList<>();
				skuDelList.add(sku);
				pushSkuDelO2o(proId + "", skuDelList);
			}
			if (StringUtil.notNull(proIdDelList)) {
				String proIdsStr = StringUtil.listToString(",", proIdDelList);
				L.info("商品ID:" + proIdsStr);
				pushProduct(proIdsStr);
			}
			
		// 公共商品删除
		} else if (OP_PRO_PUB_DEL.equals(key)) {
			
			L.info("商品ID:" + proId);
			if (StringUtil.notNull(proId))
				pushProductPub(proId + "");
			if (StringUtil.notNull(proIdDelList)) {
				String proIdsStr = StringUtil.listToString(",", proIdDelList);
				L.info("商品ID:" + proIdsStr);
				pushProductPub(proIdsStr);
			}
			if (StringUtil.notNull(proIdDelList)) {
				String proIdsStr = StringUtil.listToString(",", proIdDelList);
				L.info("商品ID:" + proIdsStr);
				pushProduct(proIdsStr);
			}
			
		/*
		 * SKU
		 */
			
		// SKU修改
		} else if (OP_SKU_EDI.equals(key)) {
			
			if (StringUtil.notNull(skuList)) {
				String skusStr = StringUtil.listToStringForSql(",", skuList);
				L.info("商品SKU:" + skusStr);
				pushSku(skusStr);
			}
			
		// SKU删除
		} else if (OP_SKU_DEL.equals(key)) {
			
			if (StringUtil.notNull(proId) && StringUtil.notNull(skuDelList)) {
				String skusDelStr = StringUtil.listToStringForSql(",", skuDelList);
				L.info("商品删除SKU:" + skusDelStr);
				pushSkuDel(proId + "", skuDelList);
			}
			
		/*
		 * 店铺冻结
		 */
			
		} else if (OP_SHOP_FROZEN.equals(key)) {
			
			if (StringUtil.notNull(proIdList)) {
				String proIdsStr = StringUtil.arrayToString(",", proIds);
				L.info("商品ID:" + proIdsStr);
				pushProductPub(proIdsStr);
			}
			if (StringUtil.notNull(skuList)) {
				String skusStr = StringUtil.listToStringForSql(",", skuList);
				L.info("商品SKU:" + skusStr);
				pushProductPub(proId + "");
			}
			
		}
	}
	
	/*
	 * Product
	 */
	
	boolean pushProduct(String proIdsStr) {
		if (StringUtil.isBlank(proIdsStr))
			return false;
		
		String pruductUrl = PropKit.use("global.properties").get("wap.product.url");
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" no, ");
		sql.append(" name, ");
		sql.append(" unit, ");
		sql.append(" (CASE ");
		sql.append(		" WHEN source BETWEEN 1 AND 2 THEN shopNo ");
		sql.append(		" WHEN source BETWEEN 3 AND 5 THEN supplierNo ");
		sql.append(		" ELSE '' ");
		sql.append(" END) supplierNo, ");
		sql.append(" (CASE ");
		sql.append(		" WHEN source BETWEEN 1 AND 2 THEN shopName ");
		sql.append(		" WHEN source BETWEEN 3 AND 5 THEN supplierName ");
		sql.append(		" ELSE '' ");
		sql.append(" END) supplierName, ");
		sql.append(" updateTime, ");
		sql.append(" sortId, ");
		sql.append(" sortCode, ");
		sql.append(" status proStatus, ");
		sql.append(" eqPrice, ");
		sql.append(" source, ");
		sql.append(" CONCAT('" + pruductUrl + "', id) productUrl ");
		sql.append(" FROM v_mag_product ");
		sql.append(" WHERE id IN (" + proIdsStr + ") ");
		sql.append(" AND auditStatus = ? ");
		sql.append(" AND (source IN (?, ?, ?) OR isO2o = ?) ");
		
		List<Record> proList = Db.find(
				sql.toString(),
				Product.AUDIT_STATUS_SUCCESS,
				Product.SOURCE_SELF_EXCLUSIVE,
				Product.SOURCE_SELF_PUBLIC,
				Product.SOURCE_FACTORY,
				BaseConstants.YES
		);
		
		return push(REQ_PRO_UPDATE, proList);
	}
	
	boolean pushProductPub(String proIdsStr) {
		if (StringUtil.isBlank(proIdsStr))
			return false;
		
		String pruductUrl = PropKit.use("global.properties").get("wap.product.url");
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" no, ");
		sql.append(" name, ");
		sql.append(" unit, ");
		sql.append(" (CASE ");
		sql.append(		" WHEN source BETWEEN 1 AND 2 THEN shopNo ");
		sql.append(		" WHEN source BETWEEN 3 AND 5 THEN supplierNo ");
		sql.append(		" ELSE '' ");
		sql.append(" END) supplierNo, ");
		sql.append(" (CASE ");
		sql.append(		" WHEN source BETWEEN 1 AND 2 THEN shopName ");
		sql.append(		" WHEN source BETWEEN 3 AND 5 THEN supplierName ");
		sql.append(		" ELSE '' ");
		sql.append(" END) supplierName, ");
		sql.append(" updateTime, ");
		sql.append(" sortId, ");
		sql.append(" sortCode, ");
		sql.append(" status proStatus, ");
		sql.append(" eqPrice, ");
		sql.append(" source, ");
		sql.append(" CONCAT('" + pruductUrl + "', id) productUrl ");
		sql.append(" FROM v_mag_product ");
		sql.append(" WHERE id IN (" + proIdsStr + ") ");
		sql.append(" AND (source BETWEEN ? AND ? OR isO2o = ?) ");
		List<Record> proList = Db.find(
			sql.toString(),
			Product.SOURCE_SELF_PUBLIC,
			Product.SOURCE_FACTORY,
			BaseConstants.YES
		);
		
		return push(REQ_PRO_UPDATE, proList);
	}
	
	boolean pushProductO2o(String proIdsStr) {
		if (StringUtil.isBlank(proIdsStr))
			return false;
		
		String pruductUrl = PropKit.use("global.properties").get("wap.product.url");
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" no, ");
		sql.append(" name, ");
		sql.append(" unit, ");
		sql.append(" (CASE ");
		sql.append(		" WHEN source BETWEEN 1 AND 2 THEN shopNo ");
		sql.append(		" WHEN source BETWEEN 3 AND 5 THEN supplierNo ");
		sql.append(		" ELSE '' ");
		sql.append(" END) supplierNo, ");
		sql.append(" (CASE ");
		sql.append(		" WHEN source BETWEEN 1 AND 2 THEN shopName ");
		sql.append(		" WHEN source BETWEEN 3 AND 5 THEN supplierName ");
		sql.append(		" ELSE '' ");
		sql.append(" END) supplierName, ");
		sql.append(" updateTime, ");
		sql.append(" sortId, ");
		sql.append(" sortCode, ");
		sql.append(" status proStatus, ");
		sql.append(" eqPrice, ");
		sql.append(" source, ");
		sql.append(" CONCAT('" + pruductUrl + "', id) productUrl ");
		sql.append(" FROM v_mag_product ");
		sql.append(" WHERE id IN (" + proIdsStr + ") ");
		List<Record> proList = Db.find(sql.toString());
		
		return push(REQ_PRO_UPDATE, proList);
	}
	
	/*
	 * Sku
	 */
	
	boolean pushSkuByProduct(String proIdsStr) {
		if (StringUtil.isBlank(proIdsStr))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" sku.eq_price eqPrice, ");
		sql.append(" if(sku.real_barCode <> sku.barCode, 1, 0) barCodeStatus, ");
		sql.append(" GROUP_CONCAT(' ', CONCAT(pv.propertyName, ':', pv.value)) AS property, ");
		sql.append(" sku.update_time AS updateTime, ");
		sql.append(" sku.supplier_price AS supplierPrice, ");
		sql.append(" sku.real_barCode as barcode, ");
		sql.append(" sku.barCode as originalBarcode, ");
		sql.append(" sku.eq_price AS retailPrice, ");
		sql.append(" sku.code AS skuCode, ");
		sql.append(" p.no productNo ");
		sql.append(" FROM t_pro_sku sku ");
		sql.append(" LEFT JOIN v_com_sku_property pv ON sku.code = pv.skuCode ");
		sql.append(" LEFT JOIN t_product p ON sku.product_id = p.id ");
		sql.append(" WHERE p.id IN (" + proIdsStr + ") ");
		sql.append(" GROUP BY sku.code ");
		List<Record> skuList = Db.find(sql.toString());
		
		return push(REQ_SKU_UPDATE, skuList);
	}
	
	boolean pushSku(String skusStr) {
		if (StringUtil.isBlank(skusStr))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" sku.eq_price eqPrice, ");
		sql.append(" if(sku.real_barCode <> sku.barCode, 1, 0) barCodeStatus, ");
		sql.append(" GROUP_CONCAT(' ', CONCAT(pv.propertyName, ':', pv.value)) AS property, ");
		sql.append(" sku.update_time AS updateTime, ");
		sql.append(" sku.supplier_price AS supplierPrice, ");
		sql.append(" sku.real_barCode as barcode, ");
		sql.append(" sku.barCode as originalBarcode, ");
		sql.append(" sku.eq_price AS retailPrice, ");
		sql.append(" sku.code AS skuCode, ");
		sql.append(" p.no productNo ");
		sql.append(" FROM v_com_sku sku ");
		sql.append(" LEFT JOIN v_com_sku_property pv ON sku.code = pv.skuCode ");
		sql.append(" LEFT JOIN t_product p ON sku.product_id = p.id ");
		sql.append(" WHERE sku.code IN (" + skusStr + ") ");
		sql.append(" AND p.audit_status = ? ");
		sql.append(" AND (p.source IN (?, ?, ?) OR sku.is_o2o = ?) ");
		sql.append(" GROUP BY sku.code ");
		List<Record> skuList = Db.find(
				sql.toString(),
				Product.AUDIT_STATUS_SUCCESS,
				Product.SOURCE_SELF_EXCLUSIVE,
				Product.SOURCE_SELF_PUBLIC,
				Product.SOURCE_FACTORY,
				BaseConstants.YES
		);

		return push(REQ_SKU_UPDATE, skuList);
	}
	
	boolean pushSkuPub(String skusStr) {
		if (StringUtil.isBlank(skusStr))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" sku.eq_price eqPrice, ");
		sql.append(" if(sku.real_barCode <> sku.barCode, 1, 0) barCodeStatus, ");
		sql.append(" GROUP_CONCAT(' ', CONCAT(pv.propertyName, ':', pv.value)) AS property, ");
		sql.append(" sku.update_time AS updateTime, ");
		sql.append(" sku.supplier_price AS supplierPrice, ");
		sql.append(" sku.real_barCode as barcode, ");
		sql.append(" sku.barCode as originalBarcode, ");
		sql.append(" sku.eq_price AS retailPrice, ");
		sql.append(" sku.code AS skuCode, ");
		sql.append(" p.no productNo ");
		sql.append(" FROM v_com_sku sku ");
		sql.append(" LEFT JOIN v_com_sku_property pv ON sku.code = pv.skuCode ");
		sql.append(" LEFT JOIN v_mag_product p ON sku.product_id = p.id ");
		sql.append(" WHERE sku.code IN (" + skusStr + ") ");
		sql.append(" AND (p.source BETWEEN ? AND ? OR p.isO2o = ?) ");
		sql.append(" GROUP BY sku.code ");
		List<Record> skuList = Db.find(
			sql.toString(),
			Product.SOURCE_SELF_PUBLIC,
			Product.SOURCE_FACTORY,
			BaseConstants.YES
		);
		
		return push(REQ_SKU_UPDATE, skuList);
	}
	
	boolean pushSkuO2o(String skusStr) {
		if (StringUtil.isBlank(skusStr))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" sku.eq_price eqPrice, ");
		sql.append(" if(sku.real_barCode <> sku.barCode, 1, 0) barCodeStatus, ");
		sql.append(" GROUP_CONCAT(' ', CONCAT(pv.propertyName, ':', pv.value)) AS property, ");
		sql.append(" sku.update_time AS updateTime, ");
		sql.append(" sku.supplier_price AS supplierPrice, ");
		sql.append(" sku.real_barCode as barcode, ");
		sql.append(" sku.barCode as originalBarcode, ");
		sql.append(" sku.eq_price AS retailPrice, ");
		sql.append(" sku.code AS skuCode, ");
		sql.append(" p.no productNo ");
		sql.append(" FROM v_com_sku sku ");
		sql.append(" LEFT JOIN v_com_sku_property pv ON sku.code = pv.skuCode ");
		sql.append(" LEFT JOIN v_mag_product p ON sku.product_id = p.id ");
		sql.append(" WHERE sku.code IN (" + skusStr + ") ");
		sql.append(" GROUP BY sku.code ");
		List<Record> skuList = Db.find(sql.toString());
		
		return push(REQ_SKU_UPDATE, skuList);
	}
	
	/*
	 * Del Sku
	 */
	
	boolean pushSkuDel(String proIdsStr, List<String> skuDelList) {
		if (StringUtil.isBlank(proIdsStr) && StringUtil.notNull(skuDelList))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT id FROM v_mag_product ");
		sql.append(" WHERE id IN (" + proIdsStr + ") ");
		sql.append(" AND (source IN (?, ?, ?) OR isO2o = ?) ");
		List<Record> pros = Db.find(
				sql.toString(),
				Product.SOURCE_SELF_EXCLUSIVE,
				Product.SOURCE_SELF_PUBLIC,
				Product.SOURCE_FACTORY,
				BaseConstants.YES
		);
		
		if (StringUtil.notNull(pros)) {
			List<Record> listRecord = new ArrayList<Record>();
			for (String s : skuDelList)
				listRecord.add(new Record().set("skuCode", s));
			
			return push(REQ_SKU_DELETE, listRecord);
		} else {
			L.info("商品SKU-JSON:" + null);
			return false;
		}
	}
	
	boolean pushSkuDelPub(String proIdsStr, List<String> skuDelList) {
		if (StringUtil.isNull(skuDelList))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT id FROM v_mag_product ");
		sql.append(" WHERE id IN (" + proIdsStr + ") ");
		sql.append(" AND (source BETWEEN ? AND ? OR isO2o = ?) ");
		List<Record> proList = Db.find(
			sql.toString(),
			Product.SOURCE_SELF_PUBLIC,
			Product.SOURCE_FACTORY,
			BaseConstants.YES
		);
		
		if (StringUtil.notNull(proList)) {
			List<Record> listRecord = new ArrayList<Record>();
			for (String s : skuDelList)
				listRecord.add(new Record().set("skuCode", s));
			
			return push(REQ_SKU_DELETE, listRecord);
		} else {
			L.info("商品SKU-JSON:" + null);
			return false;
		}
	}
	
	boolean pushSkuDelO2o(String proIdsStr, List<String> skuDelList) {
		if (StringUtil.isNull(skuDelList))
			return false;
		
		StringBuffer sql = new StringBuffer(" SELECT id FROM v_mag_product ");
		sql.append(" WHERE id IN (" + proIdsStr + ") ");
		List<Record> proList = Db.find(sql.toString());
		
		if (StringUtil.notNull(proList)) {
			List<Record> listRecord = new ArrayList<Record>();
			for (String s : skuDelList)
				listRecord.add(new Record().set("skuCode", s));
			
			return push(REQ_SKU_DELETE, listRecord);
		} else {
			L.info("商品SKU-JSON:" + null);
			return false;
		}
	}
	
	/**
	 * 调用接口
	 */
	boolean push(String reqName, List<Record> list) {
		if (StringUtil.isBlank(reqName) || StringUtil.isNull(list)) {
			L.info("推送POS参数为空");
			return false;
		}
		
		/*
		 * 转化JSON
		 */
		List<HashMap<String, Object>> descList = new ArrayList<HashMap<String, Object>>();
		for (Record r : list) {
			HashMap<String, Object> temp = new HashMap<String, Object>();
			for (String column : r.getColumnNames())
				temp.put(column, r.get(column));
			descList.add(temp);
		}
		String json = new Gson().toJson(descList);

		/*
		 * 打印JSON，并记录商品编号或名称
		 */
		String subject = "";
		
		// 商品更新
		if (REQ_PRO_UPDATE.equals(reqName))
		{
			L.info("商品-JSON:" + json);
			subject = getSubjectStr(list, "no");
		}
		
		// sku更新
		else if (REQ_SKU_UPDATE.equals(reqName))
		{
			L.info("商品-SKU:" + json);
			subject = getSubjectStr(list, "skuCode");
		}

		// sku删除
		else if (REQ_SKU_DELETE.equals(reqName))
		{
			L.info("商品-SKU:" + json);
			subject = getSubjectStr(list, "skuCode");
		}
		
		// 不存在接口
		else
		{
			L.info("调用接口名称：" + reqName + "（不存在）");
			return false;
		}
		
		/*
		 * 设置推送参数
		 */
		HashMap<String, String> paras = new HashMap<String, String>();
		String paraKey = PropKit.use("global.properties").get("push.pos.para.key");
		paras.put(paraKey, json);
		
		/*
		 * 处理推送数据
		 */
		int i = 0;
		NameValuePair[] data = new NameValuePair[1];
		StringBuffer sb = new StringBuffer();
		if (StringUtil.notNull(paras)) {
			data = new NameValuePair[paras.size() + 1];
			
			Object[] parasKeys = paras.keySet().toArray();
			Arrays.sort(parasKeys);
			
			Set<Entry<String, String>> set = paras.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			
			while(it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				String k = (String) entry.getKey();
				String v = (String) entry.getValue();
				
				data[i] = new NameValuePair(k, v);
				i++;
				
				if (!"sign".equals(k) && !"key".equals(k))
					sb.append(k + "=" + v + "&");
			}
			
			String key = PropKit.use("global.properties").get("push.pos.key");
			sb.append("key=" + key);
			String sign = MD5Builder.getMD5Str(sb.toString()).toUpperCase();
			i = i > 0 ? i : 0;
			data[i] = new NameValuePair("sign", sign);
		} else {
			L.info("没有商品参数，不推送");
			L.info("推送POS参数：" + sb.toString());
			return false;
		}
		
		/*
		 * 推送
		 */
		PushPosRecord error = null;
		JsonMessage jm = mainPush(reqName, data);
		
		/*
		 * 推送失败，再推送4次
		 */
		if (!"0".equals(jm.getStatus())) {
			int c = 0;
			while (c < 4) {
				jm = mainPush(reqName, data);
				if ("0".equals(jm.getStatus()))
					break;
				else
					c++;
			}
			
			if (!"0".equals(jm.getStatus()))
				error = new PushPosRecord().set("remark", jm.getMsg());
		}
		
		// 增加错误记录
		if (StringUtil.notNull(error)) {
			Date now = new Date();
			String reqResult = (String) jm.getData();
			
			error
				.set("request",			reqName)
				.set("type",			PushPosRecord.TYPE_PRODUCT)
				.set("subject",			subject)
				.set("result_json",		reqResult)
				.set("create_time",		now)
				.set("update_time",		now)
				.save();
		}
		
		/*
		 * 推送结果
		 */
		L.info("推送POS接口：" + reqName);
		L.info("推送POS状态：" + jm.getStatus());
		L.info("推送POS数据：" + jm.getData());
		L.info("推送POS消息：" + jm.getMsg());
		return true;
	}
	
	static JsonMessage mainPush(String reqName, NameValuePair[] data) {
		JsonMessage result = new JsonMessage();
		String reqResult = null;
		
		try {
			String reqPrefix = PropKit.use("global.properties").get("push.pos.prefix");
			PostMethod post = new PostMethod(reqPrefix + reqName);
			post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=utf-8");
			post.setRequestBody(data);
			
			HttpClient client = new HttpClient();
			client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			client.executeMethod(post);
			
			reqResult = new String(post.getResponseBodyAsString().getBytes());
			L.info("推送POS结果：" + reqResult);
			
			return new Gson().fromJson(reqResult, JsonMessage.class).setData(reqResult);
		} catch (Exception e) {
			e.printStackTrace();
			return result.setStatusAndMsg("-1", "请求出错");
		}
	}
	
	String getSubjectStr(List<Record> proList, String proNoKey) {
		String result = "";
		if (StringUtil.isNull(proList))
			return result;
		
		for (Record p : proList) {
			String proNo = p.getStr(proNoKey);
			result += proNo + ",";
		}
		return result.substring(0, result.length() - 1);
	}
	
	/*
	 * getter / setter
	 */

	public PushPosProduct setKey(String key) {
		this.key = key;
		return this;
	}

	public PushPosProduct setProId(Integer proId) {
		this.proId = proId;
		return this;
	}

	public PushPosProduct setProIds(Integer[] proIds) {
		this.proIds = proIds;
		return this;
	}

	public PushPosProduct setProIdList(List<Integer> proIdList) {
		this.proIdList = proIdList;
		return this;
	}

	public PushPosProduct setSku(String sku) {
		this.sku = sku;
		return this;
	}

	public PushPosProduct setSkus(String[] skus) {
		this.skus = skus;
		return this;
	}

	public PushPosProduct setSkuList(List<String> skuList) {
		this.skuList = skuList;
		return this;
	}

	public PushPosProduct setSkuDelList(List<String> skuDelList) {
		this.skuDelList = skuDelList;
		return this;
	}

	public PushPosProduct setProIdDelList(List<Integer> proIdDelList) {
		this.proIdDelList = proIdDelList;
		return this;
	}
	
}
