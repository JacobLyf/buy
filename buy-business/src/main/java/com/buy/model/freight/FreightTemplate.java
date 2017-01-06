package com.buy.model.freight;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.store.Store;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class FreightTemplate extends Model<FreightTemplate>{
	
	private static final long serialVersionUID = -5943897497023139313L;
	
	private  Logger L = Logger.getLogger(FreightTemplate.class);
	
	/**
	 * 运费模板-用户类型【商城】
	 */
	public static final int TYPE_MALL = 1;
	
	/**
	 * 运费模板-用户类型【店铺】
	 */
	public static final int TYPE_SHOP = 2;
	
	/**
	 * 运费模板-用户类型【供应商】
	 */
	public static final int TYPE_SUPPLIER = 3;
	
	/**
	 * 运费模板-用户类型【云店】
	 */
	public static final int TYPE_STORE = 4;
	
	public static final String CITY_DELIVER_NAME = "同城（市）配送";
	/**
	 * 同城首重运费
	 */
	public static final double CITY_FIRST_WEIGTH_CASH = 8;
	/**
	 * 同城首重
	 */
	public static final double CITY_FIRST_WEIGTH_NUM = 1;
	/**
	 * 同城续重运费
	 */
	public static final double CITY_ADD_WEIGTH_CASH = 3;
	
	public static final String PROVINCE_DELIVER_NAME = "省内配送";
	/**
	 * 省内首重运费
	 */
	public static final double PROVINCE_FIRST_WEIGTH_CASH = 9;
	/**
	 * 省内首重
	 */
	public static final double PROVINCE_FIRST_WEIGTH_NUM = 1;
	/**
	 * 省内续重运费
	 */
	public static final double PROVINCE_ADD_WEIGTH_CASH = 3;
	
	public static final FreightTemplate dao = new FreightTemplate();
	
	/**
	 * 运费计算.
	 * 
	 * @param productId
	 *            产品Id.
	 * @param toProvinceCode
	 *            收货地址【省】.
	 * @param toCityCode
	 *            收货地址【市】.
	 * @param toAreaCode
	 *            收货地址【区】.
	 * 
	 * @author Chengyb
	 * @return
	 */
	public BigDecimal calculate(Integer productId, Integer toProvinceCode, Integer toCityCode, Integer toAreaCode) {
		// 分组计算：Map【key】运费模板Id,Map【value】Map.
		Map<Integer, List<Map<String, Object>>> map = new HashMap<Integer, List<Map<String, Object>>>();
		
		Product product = Product.dao.findById(productId);
		
		if(null != product) {
			// 是否包邮.
			Integer freeFreight = product.getInt("is_free_postage");
			if(freeFreight == Product.NO_FREE_POSTAGE) {
				// 运费模板Id.
			    Integer templateId = product.getInt("freight_id");
			    
			    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			    
			    Map<String, Object> productData = new HashMap<String, Object>();
			    productData.put("number", 1); // 商品数量.
			    productData.put("price", product.getBigDecimal("eq_price")); // 单个商品金额.
			    productData.put("weight", product.getBigDecimal("weight")); // 单个商品重量.
			    
			    list.add(productData);
			    map.put(templateId, list);
			} else {
				return new BigDecimal(0.00);
			}
		}
		
		BigDecimal freight = calculateByFreightTemplateGroup(map, toProvinceCode, toCityCode, toAreaCode);
		
		L.info("计算运费: 商品Id " + productId + ", 省: " + toProvinceCode + ", 市: " + toCityCode + ", 区: " + toAreaCode + ", 运费: " + freight);
		
		return freight;
	}
	
	/**
	 * 运费计算.
	 * 
	 * @param productMap
	 *            产品Map【key】Sku识别码,【value】产品数量.
	 * @param toProvinceCode
	 *            收货地址【省】.
	 * @param toCityCode
	 *            收货地址【市】.
	 * @param toAreaCode
	 *            收货地址【区】.
	 * 
	 * @author Chengyb
	 * @return
	 */
	public BigDecimal calculate(Map<String, Integer> productMap, Integer toProvinceCode, Integer toCityCode, Integer toAreaCode) {
		// 分组计算：Map【key】运费模板Id,Map【value】Map.
		Map<Integer, List<Map<String, Object>>> map = new HashMap<Integer, List<Map<String, Object>>>();
		
		// 按照运费模板分组商品.
		groupingByTemplateId(productMap, map);
		
		return calculateByFreightTemplateGroup(map, toProvinceCode, toCityCode, toAreaCode);
	}
	
	/**
	 * 云店发货运费计算.
	 * 
	 * @param productMap
	 *            产品Map【key】Sku识别码,【value】产品数量.
	 * @param storeNo
	 *            云店编号.
	 * @param toProvinceCode
	 *            收货地址【省】.
	 * @param toCityCode
	 *            收货地址【市】.
	 * @param toAreaCode
	 *            收货地址【区】.
	 * 
	 * @author Chengyb
	 * @return
	 */
	public BigDecimal calculate(Map<String, Integer> productMap, String storeNo, Integer toProvinceCode, Integer toCityCode, Integer toAreaCode) {
		// 分组计算：Map【key】运费模板Id,Map【value】Map.
		Map<Integer, List<Map<String, Object>>> map = new HashMap<Integer, List<Map<String, Object>>>();
		
		// 运费模板Id.
	    Integer templateId = Store.dao.findFreightTemplate(storeNo);
	    
	    /*=====================================
		 * 将商品分组到不同的运费模板中.
		 *=====================================*/
	    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	    
	    for (Entry<String, Integer> entry : productMap.entrySet()) {
		    String skuCode = entry.getKey(); // Sku识别码.
		    Integer productNumber = entry.getValue(); // 产品数量.
		    
		    // 获取商品Sku对象.
		    ProductSku productSku = ProductSku.dao.findById(skuCode);
		    
		    // 商品信息.
		    Product product = Product.dao.findById(productSku.getInt("product_id"));
		    
		    Map<String, Object> productData = new HashMap<String, Object>();
		    productData.put("number", productNumber); // 商品数量.
		    productData.put("price", productSku.getBigDecimal("eq_price")); // 单个商品金额.
		    productData.put("weight", product.getBigDecimal("weight")); // 单个商品重量.
		    
		    list.add(productData);
	    }

		map.put(templateId, list);
		
		return calculateByFreightTemplateGroup(map, toProvinceCode, toCityCode, toAreaCode);
	}
	
	private void groupingByTemplateId(Map<String, Integer> productMap, Map<Integer, List<Map<String, Object>>> map) {
		for (Entry<String, Integer> entry : productMap.entrySet()) {
		    String skuCode = entry.getKey(); // Sku识别码.
		    Integer productNumber = entry.getValue(); // 产品数量.
		    
		    // 获取商品Sku对象.
		    ProductSku productSku = ProductSku.dao.findById(skuCode);
		    
		    // 商品信息.
		    Product product = Product.dao.findById(productSku.getInt("product_id"));
		    // 运费模板Id.
		    Integer templateId = product.getInt("freight_id");
		    
		    /*=====================================
			 * 将商品分组到不同的运费模板中.
			 *=====================================*/
		    List<Map<String, Object>> list;
		    if(map.containsKey(templateId)) {
		    	list = map.get(templateId);
		    } else {
		    	list = new ArrayList<Map<String, Object>>();
		    }
		    
		    // 是否包邮.
		    Integer freeFreight = product.getInt("is_free_postage");
			if (null != freeFreight && freeFreight == Product.NO_FREE_POSTAGE) {
				Map<String, Object> productData = new HashMap<String, Object>();
				productData.put("number", productNumber); // 商品数量.
				productData.put("price", productSku.getBigDecimal("eq_price")); // 单个商品金额.
				productData.put("weight", product.getBigDecimal("weight")); // 单个商品重量.

				list.add(productData);
				map.put(templateId, list);
			}
		}
	}
	
	/**
	 * 计算每组运费模板中商品的运费.
	 * 
	 * @param freightTemplateMap
	 *            运费模板Map.
	 * @param toProvinceCode
	 *            收货地址【省】.
	 * @param toCityCode
	 *            收货地址【市】.
	 * @param toAreaCode
	 *            收货地址【区】.
	 * 
	 * @author Chengyb
	 * @return
	 */
	private BigDecimal calculateByFreightTemplateGroup(Map<Integer, List<Map<String, Object>>> freightTemplateMap,
			Integer toProvinceCode, Integer toCityCode, Integer toAreaCode) {
		// 各组运费模板合计后的总运费.
		BigDecimal totalFreight = new BigDecimal(0);
				
		/*=====================================
		 * 计算每组运费模板中商品的运费.
		 *=====================================*/
		for (Entry<Integer, List<Map<String, Object>>> entry : freightTemplateMap.entrySet()) {
			Integer templateId = entry.getKey(); // 运费模板Id.
			
			// 商品总件数.
			Integer templateTotalNumber = 0;
			// 商品总金额.
			BigDecimal templateTotalAmount = new BigDecimal(0);
			// 商品总重量.
			BigDecimal templateTotalWeight = new BigDecimal(0);
			
			// 匹配当前运费模板的运费规则.
			Record ruleRecord = matchingFreightRule(templateId, toProvinceCode, toCityCode, toAreaCode);
			
//			if(null != list && list.get(0).getInt("rule_code") == FreightRule.TYPE_NOT) { // 不免邮.
//				/*=====================================
//				 * 不存在免邮规则.
//				 *=====================================*/
//				// 计算运费.
//				totalFreight = totalFreight.add(calculateFreight(templateTotalWeight, list.get(0).getBigDecimal("first_weigth_cash"), list.get(0).getBigDecimal("first_weigth_num"), list.get(0).getBigDecimal("add_weight_cash")));
//			} else {
//				/*=====================================
//				 * 【满X元免邮】/【满X件免邮】.
//				 *=====================================*/
		    if(null != ruleRecord) {
		    	/*=====================================
				 * 当前运费模板下的商品列表.
				 *=====================================*/
		    	List<Map<String, Object>> productList = entry.getValue();
					
				for (int i = 0, size = productList.size(); i < size; i++) {
					Map<String, Object> product = productList.get(i);
						
					BigDecimal number = new BigDecimal(((Integer) product.get("number")).intValue());
					// 统计运费模板下的所有商品的总件数.
					templateTotalNumber += (Integer) product.get("number");
					// 统计运费模板下的所有商品的总重量.
					templateTotalWeight = templateTotalWeight.add(number.multiply((BigDecimal) product.get("weight")));
					// 统计运费模板下的所有商品的总金额.
					templateTotalAmount = templateTotalAmount.add(number.multiply((BigDecimal) product.get("price")));
				}
				
				// 是否免邮标志位.
				Boolean flag = false;
					
				if(ruleRecord.getInt("rule_code") == FreightRule.TYPE_AMOUNT) {// 满X元包邮.
					// 符合满邮条件.
					if(ruleRecord.getBigDecimal("condition").compareTo(templateTotalAmount) <= 0) {
						flag = true;
					}
				}
				if(ruleRecord.getInt("rule_code") == FreightRule.TYPE_NUMBER) {// 满X件包邮.
					// 符合满邮条件.
					if(ruleRecord.getBigDecimal("condition").compareTo(new BigDecimal(templateTotalNumber)) <= 0) {
						flag =true;
					}
				}
					
				if(!flag) {
					// 当前运费模板下商品的总运费.
					BigDecimal templateFreight = calculateFreight(templateTotalWeight, ruleRecord.getBigDecimal("first_weigth_cash"), ruleRecord.getBigDecimal("first_weigth_num"), ruleRecord.getBigDecimal("add_weight_cash"));
						
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
					System.out.println("当前运费模板下商品运费:" + templateFreight);
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
						
					// 计算运费.
					totalFreight = totalFreight.add(templateFreight);
				}
		    } else {
		    	L.error("【错误】未匹配到运费规则: 运费模板Id = " + templateId + ", 省 =" + toProvinceCode + ", 市 = " + toCityCode +", 区 = " + toAreaCode);
		    }
		}
		return totalFreight;
	}
	
	public Record matchingFreightRule(Integer templateId, Integer toProvinceCode, Integer toCityCode,
			Integer toAreaCode) {
		// 【省】【市】【区】匹配.
		Record record = findByTemplateAndDistrict(templateId, toProvinceCode, toCityCode, toAreaCode);
		
		// 【省】【市】匹配.
		if(null == record) {
			record = findByTemplateAndDistrict(templateId, toProvinceCode, toCityCode, null);
		}
		
		// 【省】匹配.
		if(null == record) {
			record = findByTemplateAndDistrict(templateId, toProvinceCode, null, null);
		}
		
		// 匹配全国模板.
		if(null == record) {
			record = findByTemplateAndDistrict(templateId, null, null, null);
		}
		return record;
	}
	
	/**
	 * 获取产品对应的运费规则.
	 * 
	 * @param templateId
	 *            运费模板Id.
	 * @param toProvinceCode
	 *            收货地址【省】.
	 * @param toCityCode
	 *            收货地址【省】.
	 * @param toAreaCode
	 *            收货地址【区】.
	 * 
	 * @author Chengyb
	 * @return 
	 */
	public Record findByTemplateAndDistrict(Integer templateId, Integer toProvinceCode, Integer toCityCode, Integer toAreaCode) {
		List<Object> paramList = new ArrayList<Object>();
		
		StringBuffer sql = new StringBuffer("SELECT * FROM t_freight_rule s WHERE s.template_id = ?");
		paramList.add(templateId);
		
		// 省.
		if(null != toProvinceCode) {
			sql.append(" AND s.province_code = ?");
			
			paramList.add(toProvinceCode);
		} else {
			sql.append(" AND s.province_code is null");
		}
		
		// 市.
		if(null != toCityCode) {
			sql.append(" AND s.city_code = ?");
			
			paramList.add(toCityCode);
		} else {
			sql.append(" AND s.city_code is null");
		}
		// 区.
		if(null != toAreaCode) {
			sql.append(" AND s.area_code = ?");
			
			paramList.add(toAreaCode);
		} else {
			sql.append(" AND s.area_code is null");
		}
		
	return Db.findFirst(sql.toString(), paramList.toArray());
	}
	
	/**
	 * 计算邮费.
	 * 
	 * @param totalWeight
	 *            商品总重量.
	 * @param firstWeigthCash
	 *            首重金额.
	 * @param firstWeigthNum
	 *            首重重量.
	 * @param addWeightCash
	 *            续重金额.
	 * 
	 * @author Chengyb
	 */
	private BigDecimal calculateFreight(BigDecimal totalWeight, BigDecimal firstWeigthCash, BigDecimal firstWeigthNum,
			BigDecimal addWeightCash) {
		if(totalWeight.compareTo(firstWeigthNum) <= 0) {
			return firstWeigthCash;
		} else {
			double count = Math.ceil((totalWeight.subtract(firstWeigthNum)).doubleValue());
			return firstWeigthCash.add(new BigDecimal(count).multiply(addWeightCash));
		}
	}
	
	/**
	 * 保存模板
	 * @author chenhj
	 * @param templateType 模板类型
	 * @param templateName 模板名称
	 * @param targetId 所属用户ID
	 * @param date 创建日期
	 * @return
	 */
	public FreightTemplate saveTemplate(Integer templateType, String templateName, String targetId, Date date){
		FreightTemplate template = new FreightTemplate();
		template.set("name", templateName);
		template.set("target_type", templateType);
		template.set("create_time", date);
		if(targetId != null){
			template.set("target_id", targetId);
		}
		template.save();
		return template;
	}
	
	/**
	 * 复制运费模板
	 * @author chenhj
	 * @param mainTemplateId 主模板ID
	 * @return
	 */
	public boolean copyFreight(Integer mainTemplateId){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM t_freight_template t WHERE t.id = ?");
		// 查询主模板对象
		Record mainTemplate = Db.findFirst(sql.toString(), mainTemplateId);
		// 主模板名字
		String m_template_name = mainTemplate.getStr("name");
		
		sql.setLength(0);
		// 利用模糊查询查询出类似于主模板名字的列表
		sql.append(" SELECT");
		sql.append("    max(`name`) name");
		sql.append(" FROM `t_freight_template`");
		sql.append(" WHERE `name` LIKE '");
		if(m_template_name.contains("(")){
			sql.append(m_template_name.substring(0, m_template_name.indexOf("(")+1));
		}else{
			sql.append(m_template_name);
		}
		sql.append("%' ORDER BY name DESC");
		
		FreightTemplate freightRecord = FreightTemplate.dao.findFirst(sql.toString());
		// 取出最长的模板名
		String freightName = freightRecord.getStr("name");
		StringBuffer nameBuffer = new StringBuffer();
		// 判断是否已经包含 副本
		if(freightName.contains("-副本")){
			// 判断是否包含副本个数
			if(freightName.contains("(") && freightName.contains(")")){
				Integer count = Integer.parseInt(freightName.substring(freightName.indexOf("(") + 1, freightName.indexOf(")")));
				nameBuffer.append(freightName.split("-")[0]);
				nameBuffer.append("-副本(");
				// 副本个数  + 1
				nameBuffer.append(count + 1);
				nameBuffer.append(")");
			}else{// 有副本但没有个数则创建副本个数
				nameBuffer.append(freightName.split("-")[0]);
				nameBuffer.append("-副本(2)");
			}
		}else{// 不包含副本则创建第一个副本模板
			nameBuffer.append(m_template_name);
			nameBuffer.append("-副本");
		}
		
		// 保存模板副本
		FreightTemplate copyTemplate = saveTemplate(mainTemplate.getInt("target_type"), 
									  				nameBuffer.toString(), 
													mainTemplate.getStr("target_id"),
													new Date());
		// 取出模板副本ID
		Integer copyTemplateId = copyTemplate.getInt("id");
		// 清空旧sql语句
		sql.setLength(0);
		// 组装sql语句查询运费规则
		sql.append(" SELECT");
		sql.append("    template_id,");
		sql.append("    province_code,");
		sql.append("    city_code,");
		sql.append("    area_code,");
		sql.append("    first_weigth_cash,");
		sql.append("    first_weigth_num,");
		sql.append("    add_weight_cash,");
		sql.append("    rule_code,");
		sql.append("    `condition`,");
		sql.append("    create_time,");
		sql.append("    update_time");
		sql.append(" FROM `t_freight_rule`");
		sql.append(" WHERE template_id = ?");
		// 查询出主模板的运费数据
		List<FreightRule> freightRuleRecords = FreightRule.dao.find(sql.toString(), mainTemplateId);
		List<Boolean> result = new ArrayList<Boolean>();
		for (FreightRule record : freightRuleRecords) {
			record.set("template_id", copyTemplateId);
			record.remove("id");
			result.add(record.save());
		}
		if(result.contains(false)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 判断当前运费模板是否属于指定用户.
	 * 
	 * @author Chengyb
	 * @return
	 */
	public Boolean isUserFreightTemplate(Integer templateId, String targetId, Integer targetType){
		String sql = "SELECT * FROM t_freight_template WHERE id = ? AND target_id = ? AND target_type = ?";
		
		return Db.find(sql, templateId, targetId, targetType).isEmpty() ? false : true;
	}
	
	/**
	 * 获取所有全
	 * @return
	 */
	public List<Record> getStoreFreightDetail() {
		StringBuffer sql = new StringBuffer();
		sql.append(" select fr.first_weigth_cash firstWeightCash, ")
		   .append("   fr.first_weigth_num firstWeightNum,")
		   .append("   fr.add_weight_cash addWeightCash,")
		   .append("   CONCAT(t1.`name`, '-', t2.`name`) as toAddress")
		   .append(" from t_freight_rule fr")
		   .append("   LEFT JOIN t_address t1 ON fr.province_code = t1.code")
		   .append("   LEFT JOIN t_address t2 ON fr.city_code = t2.code")
		   .append("   where template_id = ? AND fr.area_code IS NULL ");
		
		List<Record> storeList = Store.dao.getAllStoreFreightId();
		
		for (Record store : storeList) {
			List<Record> ruleList = Db.find(sql.toString(), store.getInt("freight_template_id"));
			if (StringUtil.notNull(ruleList)) {
				store.set("list", ruleList);
				
				BigDecimal cash = new BigDecimal(0); 
				for (Record rule : ruleList) {
					if (StringUtil.isNull(rule.getStr("toAddress"))) {
						rule.set("toAddress", "全国");
					}
					cash = cash.add(rule.getBigDecimal("firstWeightCash"));
				}
				
				cash = cash.divide(new BigDecimal(ruleList.size()), BigDecimal.ROUND_HALF_UP);
				store.set("averageCash", cash);
			} else {
				store.set("list", null);
				store.set("averageCash", 0);
			}
		}
		return storeList;
	}
}