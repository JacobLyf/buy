package com.buy.model.store;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.map.MapValueComparator;
import com.buy.model.address.Address;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.order.Order;
import com.buy.model.order.OrderDetail;
import com.buy.model.product.ProductSku;
import com.buy.model.user.RecAddress;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * Model - 仓库
 */
public class Store extends Model<Store> {

	private static final long serialVersionUID = 1L;

	public static final Store dao = new Store();

	/**
	 * 仓库类型 - 云店
	 */
	public static final int TYPE_CLOUD = 1;
	/**
	 * 仓库类型 - 分仓
	 */
	public static final int TYPE_STORES = 2;
	/**
	 * 仓库类型 - 总仓
	 */
	public static final int TYPE_TOTAL = 3;
	
	/**
	 * 总仓编号
	 */
	public static final String TOTAL_NO = "zgs";
	
	/**
	 * app排序-主图
	 */
	public static final int O2OSHOP_APP_SORT_NUM = 6;
	
	

	/**
	 * 发布状态 -未发布 
	 */
	public static final int RELEASE_STATUS_DISABLE = 0;
	/**
	 * 发布状态 -已发布 
	 */
	public static final int RELEASE_STATUS_ENABLE = 1;
	
	/**
	 * 获取仓库名称
	 * @param storeNo
	 * @return
	 * @author Jacob
	 * 2016年4月8日下午2:27:43
	 */
	public String getNameByNo(String storeNo){
		String sql = "SELECT s.name secretKey FROM t_store s WHERE s.`no` = ? ";
		return Db.queryStr(sql,storeNo);
	}

	/**
	 * pos 调用 （彬哥）
	 * @param storeNo
	 * @return
	 * @author chenhg
	 * 2016年3月26日 上午11:09:16
	 */
	public String getSecretKeyByNo(String storeNo){
		String sql = "SELECT s.secret_key secretKey FROM t_store s WHERE s.`no` = ? ";
		return Db.queryStr(sql,storeNo);
	}
	/**
	 * 通过编号获取仓库
	 * @param storeNo
	 * @return
	 * @author huangzq
	 */
	public Store getStoreByNo(String storeNo){
		String sql = "SELECT * FROM t_store s WHERE s.`no` = ? ";
		return dao.findFirst(sql,storeNo);
	}
	/**
	 * 获取总仓
	 * @return
	 * @author huangzq
	 */
	public Store getTotalStore(){
		String sql = "SELECT * FROM t_store s WHERE s.type = ? ";
		return dao.findFirst(sql,TYPE_TOTAL);
	}
	
	/**
	 * 获取总仓编号
	 * @return
	 * @author Jacob
	 * 2016年3月29日上午11:45:28
	 */
	public String getTotalStoreNo(){
		String sql = "SELECT no FROM t_store s WHERE s.type = ? ";
		Store store = dao.findFirst(sql,TYPE_TOTAL);
		return store!=null?store.getStr("no"):"";
	}
	
	/**
	 * 获取满足购买数量的商品SKU的所在收货地址的O2O门店列表
	 * @param skuCode  商品SKU识别码
	 * @return
	 * @author Jacob
	 * 2015年12月10日下午5:31:10
	 */
	public List<Record> findListByParams(String skuCode){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.*, ");
		sql.append(" 	IF(osm.pro_count - osm.store_lock_count < 0,0,osm.pro_count - osm.store_lock_count) as pro_count ");
		sql.append(" FROM ");
		sql.append(" 	(select a.* from t_store a where a.release_status = ?) s, ");
		sql.append(" 	(select b.sku_code, b.store_no from t_o2o_sku_map b where b.sku_code = ?) m, ");
		sql.append(" 	(select c.sku_code, c.store_no, c.pro_count, c.store_lock_count from t_store_sku_map c where c.sku_code = ?) osm ");
		sql.append(" WHERE ");
		sql.append(" 	s.no = osm.store_no ");
		sql.append("    AND m.sku_code = osm.sku_code");
		sql.append("    AND m.store_no = osm.store_no");
		return Db.find(sql.toString(), RELEASE_STATUS_ENABLE, skuCode, skuCode);
	}
	
	/**
	 * 根据云店id，获取云店地址信息
	 * @param storeId
	 * @return
	 */
	public Record findAddressName(String storeId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  p.`name` provinceName, ");
		sql.append("  c.`name` cityName, ");
		sql.append("  area.`name` areaName, ");
		sql.append("  a.no storeNo, ");
		sql.append("  a.principal, ");
		sql.append("  a.mobile, ");
		sql.append("  a.address ");
		sql.append(" FROM (SELECT s.principal,s.mobile,s.province_code,s.city_code,s.area_code,s.address,s.no from t_store s WHERE s.id = ?) a  ");
		sql.append("  LEFT JOIN t_address p ON a.province_code = p.`code` ");
		sql.append("  LEFT JOIN t_address c ON a.city_code = c.`code` ");
		sql.append("  LEFT JOIN t_address area ON a.area_code = area.`code` ");
		
		return Db.findFirst(sql.toString(), storeId);
	}
	
	/**
	 * 根据云店编号，获取云店地址信息
	 * @param storeId
	 * @return
	 */
	public Record findAddressNameByNo(String storeNo){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  p.`name` provinceName, ");
		sql.append("  c.`name` cityName, ");
		sql.append("  area.`name` areaName, ");
		sql.append("  a.no storeNo, ");
		sql.append("  a.principal, ");
		sql.append("  a.mobile, ");
		sql.append("  a.address ");
		sql.append(" FROM (SELECT s.principal,s.mobile,s.province_code,s.city_code,s.area_code,s.address,s.no from t_store s WHERE s.no = ?) a  ");
		sql.append("  LEFT JOIN t_address p ON a.province_code = p.`code` ");
		sql.append("  LEFT JOIN t_address c ON a.city_code = c.`code` ");
		sql.append("  LEFT JOIN t_address area ON a.area_code = area.`code` ");
		
		return Db.findFirst(sql.toString(), storeNo);
	}
	
	/**
	 * 获取订单的发货/自提的云店信息
	 * @param orderId
	 * @return
	 */
	public Record findStoreByOrderId(String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  o.o2o_shop_no, ");
		sql.append("  o.o2o_shop_name, ");
		sql.append("  s.principal, ");
		sql.append("  s.mobile, ");
		sql.append("  o.o2o_shop_address ");
		sql.append(" FROM t_order o  ");
		sql.append(" left join t_store s on o.o2o_shop_no = s.no  ");
		
		sql.append(" WHERE o.id = ? ");
		 
		return Db.findFirst(sql.toString(), orderId);
	}
	
	/**
	 * 获取云店退货的地址明细（包括手机号码、收件人）
	 * @param storeNo
	 * @return
	 * @author chenhg
	 * 2016年11月15日 下午2:17:04
	 */
	public Record findStoreShippingAddress(String storeNo){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  s.`name`, ");
		sql.append("  s.`no`, ");
		sql.append("  s.principal, ");
		sql.append("  s.mobile, ");
		sql.append("  p.`name` provinceName, ");
		sql.append("  c.`name` cityName, ");
		sql.append("  area.`name` areaName, ");
		sql.append("  CONCAT(p.`name`,' ',c.`name`, ' ',area.`name`) as addressHead, ");
		sql.append("  s.address ");
		sql.append(" FROM t_store s  ");
		sql.append("   LEFT JOIN t_address p ON s.province_code = p.`code` ");
		sql.append("   LEFT JOIN t_address c ON s.city_code = c.`code`");
		sql.append("  LEFT JOIN t_address area ON s.area_code = area.`code` ");
		sql.append(" WHERE s.`no` = ?  ");
		
		return Db.findFirst(sql.toString(), storeNo);
	}
	
	/**
	 * 获取当前订单能退货的云店或仓库
	 * @param orderId
	 * @return
	 */
	public List<Record> findStoreForReturnProduct(String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT   ");
		sql.append("   s.id, ");
		sql.append("   s.`name`, ");
		sql.append("   s.`no`, ");
		sql.append("   s.principal, ");
		sql.append("   s.mobile, ");
		sql.append("   CONCAT(p.`name`,c.`name`,area.`name`,s.address) as address ");
		sql.append(" FROM    ");
		sql.append("  ( SELECT b.store_no ");
		sql.append("    FROM");
		sql.append("     (SELECT ssm.store_no,count(1) num");
		sql.append("      FROM");
		sql.append("        (SELECT od.sku_code FROM t_order_detail od WHERE od.order_id = ? ) a");
		sql.append("         LEFT JOIN t_store_sku_map ssm ON ssm.sku_code = a.sku_code");
		sql.append("         GROUP BY ssm.store_no) b ");
		sql.append("      WHERE b.num =  ");
		sql.append("        (SELECT COUNT(1) odNum FROM t_order_detail od2 WHERE od2.order_id = ? )");
		sql.append("  ) c");
		sql.append("  LEFT JOIN t_store s ON c.store_no = s.`no`  ");
		sql.append("  LEFT JOIN t_address p ON s.province_code = p.`code`  ");
		sql.append("  LEFT JOIN t_address c ON s.city_code = c.`code`  ");
		sql.append("  LEFT JOIN t_address area ON s.area_code = area.`code`  ");
		sql.append(" WHERE s.release_status = ? ");
		sql.append("  AND s.status = ? ");
		
		return Db.find(sql.toString(), orderId, orderId, Store.RELEASE_STATUS_ENABLE, BaseConstants.YES);
	}
	
	/**
	 * 根据省市区编码添加省市区名称
	 * @param list
	 * @return
	 * @author Jacob
	 * 2015年12月11日上午9:53:03
	 */
	public List<Record> changeAddressCodeToName(List<Record> list){
		//将省市区编码转为名称.
		for (int i = 0, size = list.size(); i < size; i++) {
			Record o2oShop = list.get(i);
			
			List<Record> nameList = Address.dao.transformCode(o2oShop.getInt("province_code"), o2oShop.getInt("city_code"), o2oShop.getInt("area_code"));
		    for (int j = 0, size2 = nameList.size(); j < size2; j++) {
				Integer code = nameList.get(j).getInt("code");
				if(o2oShop.getInt("province_code").equals(code)) {
					o2oShop.set("province_name", nameList.get(j).getStr("name"));
					continue;
				}
                if(o2oShop.getInt("city_code").equals(code)) {
                	o2oShop.set("city_name", nameList.get(j).getStr("name"));
                	continue;
				}
                if(o2oShop.getInt("area_code").equals(code)) {
                	o2oShop.set("area_name", nameList.get(j).getStr("name"));
                	continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * 获取所有仓库信息
	 * @return
	 * @author Eriol
	 */
	public List<Record> findAllList(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.id, ");
		sql.append(" 	s.no, ");
		sql.append(" 	s.name, ");
		sql.append(" 	s.type ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		return Db.find(sql.toString());
	}
	
	/**
	 * 获取幸运一折购中奖订单可发货云店
	 * @param efunUserOrderId
	 * @return
	 */
	public List<Record> findAllForEfunWinSend(String efunUserOrderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.id, ");
		sql.append(" 	s.no, ");
		sql.append(" 	s.name, ");
		sql.append(" 	s.type ");
		sql.append(" FROM t_efun_user_order euo ");
		sql.append(" 	LEFT JOIN t_store_sku_map ssm ON euo.sku_code = ssm.sku_code ");
		sql.append(" 	LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
		sql.append(" 	WHERE euo.id = ? ");
		sql.append(" 	AND ssm.pro_count - ssm.store_lock_count > 0 ");
		return Db.find(sql.toString(), efunUserOrderId);
	}
	
//	/**
//	 * 获取当前订单能指定发货的所有仓库信息
//	 * @param orderId
//	 * @return
//	 */
//	public List<Record> findEnoughCountStoreByOrderId(String orderId){
//		List<Record> orderDetailList = OrderDetail.dao.findSkuCodeList(orderId);
//		List<Record> storeList = null;
//		
//		if(orderDetailList.size() > 1){
//			boolean flag = true;//是否为第一个
//			for(Record od : orderDetailList){
//				StringBuffer sql = new StringBuffer();
//				if(flag){
//					flag = false;
//					sql.append(" SELECT ");
//					sql.append("   s.id,   ");
//					sql.append("   s.no,   ");
//					sql.append("   s.name,  ");
//					sql.append("   s.type   ");
//					sql.append(" FROM ");
//					sql.append("  t_store_sku_map ssm ");
//					sql.append("  LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
//					sql.append(" WHERE 1=1   ");
//					sql.append(" and ssm.sku_code = ?   ");
//					sql.append(" and ssm.pro_count - ssm.store_lock_count >= ?   ");
//					sql.append(" and s.`status` =?   ");
//					storeList = Db.find(sql.toString(), od.getStr("skuCode"), od.getNumber("count"), Store.RELEASE_STATUS_ENABLE);
//				}else{
//					if(storeList != null && storeList.size() > 0){
//						List<Object> paras = new ArrayList<Object>();
//						sql.append(" SELECT ");
//						sql.append("   s.id,   ");
//						sql.append("   s.no,   ");
//						sql.append("   s.name,  ");
//						sql.append("   s.type   ");
//						sql.append(" FROM ");
//						sql.append("  t_store_sku_map ssm ");
//						sql.append("  LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
//						sql.append(" WHERE 1=1   ");
//						sql.append(" and ssm.sku_code = ?   ");
//						sql.append(" and ssm.pro_count - ssm.store_lock_count >= ?   ");
//						sql.append(" and s.`status` =?   ");
//						
//						paras.add(od.getStr("skuCode"));
//						paras.add(od.getNumber("count"));
//						paras.add(Store.RELEASE_STATUS_ENABLE);
//						
//						
//						sql.append(" and s.id in(   ");
//						for(Record s: storeList){
//							sql.append(" ?,");
//							paras.add(s.getNumber("id"));
//						}
//						sql.deleteCharAt(sql.length()-1);  
//						sql.append(" )   ");
//						storeList = Db.find(sql.toString(), paras.toArray());
//					}
//				}
//			}
//		}else{
//			Record od = orderDetailList.get(0);
//			StringBuffer sql = new StringBuffer();
//			sql.append(" SELECT    ");
//			sql.append("   s.id,   ");
//			sql.append("   s.no,   ");
//			sql.append("   s.name,  ");
//			sql.append("   s.type   ");
//			sql.append("   FROM ");
//			sql.append("    t_store_sku_map ssm");
//			sql.append("   LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
//			sql.append("   WHERE  ssm.sku_code = ? ");
//			sql.append("    and ssm.pro_count - ssm.store_lock_count >= ? ");
//			sql.append("    and s.`status` =? ");
//			storeList = Db.find(sql.toString(), od.getStr("skuCode"), od.getNumber("count"), Store.RELEASE_STATUS_ENABLE);
//		}
//		
//		
//		
//		return storeList;
//	}
	
	
	/**
	 * 查看库存列表(库存充足)
	 * @param orderId
	 * @return
	 * @author chenhg
	 * 2016年11月17日 下午2:40:52
	 */
	public List<Record> getSendStoreListDetail(String orderId){
		List<Record> orderDetailList = OrderDetail.dao.findSkuCodeList(orderId);
		
		List<Record> storeList = null;
		//多个sku
		if(orderDetailList.size() > 1){
			//1、查询出所有的满足库存的云店/仓库的id
			boolean flag = true;//是否为第一个
			for(Record od : orderDetailList){
				StringBuffer sql = new StringBuffer();
				if(flag){
					flag = false;
					sql.append(" SELECT ");
					sql.append("  s.id ");
					sql.append(" FROM ");
					sql.append("  t_store_sku_map ssm ");
					sql.append("  LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
					sql.append(" WHERE 1=1   ");
					sql.append(" and ssm.sku_code = ?   ");
					sql.append(" and ssm.pro_count - ssm.store_lock_count >= ?   ");
					sql.append(" and s.`status` =?   ");
					storeList = Db.find(sql.toString(), od.getStr("skuCode"), od.getNumber("count"), Store.RELEASE_STATUS_ENABLE);
				}else{
					if(storeList != null && storeList.size() > 0){
						List<Object> paras = new ArrayList<Object>();
						sql.append(" SELECT ");
						sql.append("  s.id ");
						sql.append(" FROM ");
						sql.append("  t_store_sku_map ssm ");
						sql.append("  LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
						sql.append(" WHERE 1=1   ");
						sql.append(" and ssm.sku_code = ?   ");
						sql.append(" and ssm.pro_count - ssm.store_lock_count >= ?   ");
						sql.append(" and s.`status` =?   ");
						
						paras.add(od.getStr("skuCode"));
						paras.add(od.getNumber("count"));
						paras.add(Store.RELEASE_STATUS_ENABLE);
						
						
						sql.append(" and s.id in(   ");
						for(Record s: storeList){
							sql.append("'" + s.getNumber("id") + "',");
						}
						sql.deleteCharAt(sql.length()-1);  
						sql.append(" )   ");
						storeList = Db.find(sql.toString(), paras.toArray());
					}
				}
			}
			//2、查询云店/仓库的详细信息
			if(storeList != null && storeList.size() > 0){
				StringBuffer resutlSql = new StringBuffer();
				List<Object> resutlParas = new ArrayList<Object>();
				resutlSql.append(" SELECT   ");
				resutlSql.append("  a.*,  ");
				resutlSql.append("  CONCAT(p.`name`,c.`name`, area.`name`, a.address) AS storeAddress  ");
				resutlSql.append(" FROM   ");
				resutlSql.append("   (SELECT ");
				resutlSql.append("    s.id,");
				resutlSql.append("    s.no,");
				resutlSql.append("    s.name,");
				resutlSql.append("    s.type,");
				resutlSql.append("    s.province_code,");
				resutlSql.append("    s.city_code,");
				resutlSql.append("    s.area_code,");
				resutlSql.append("    s.address,");
				resutlSql.append("    '充足' as num ");
				resutlSql.append("   FROM ");
				resutlSql.append("    t_store s ");
				resutlSql.append("   WHERE 1=1 ");
				resutlSql.append("   and s.id in(");
				for(Record s: storeList){
					resutlSql.append("'" + s.getNumber("id") + "',");
				}
				resutlSql.deleteCharAt(resutlSql.length()-1); 
				resutlSql.append(") ");
				resutlSql.append("  ) a  ");
				resutlSql.append(" LEFT JOIN t_address p ON a.province_code = p.`code`  ");
				resutlSql.append(" LEFT JOIN t_address c on a.city_code = c.`code`  ");
				resutlSql.append(" LEFT JOIN t_address area ON a.area_code = area.`code`  ");
				
				storeList = Db.find(resutlSql.toString(), resutlParas.toArray());  
			}
			
		}else{
			StringBuffer sql = new StringBuffer();
			//查询云店/仓库的详细信息
			Record od = orderDetailList.get(0);
			sql.append(" SELECT   ");
			sql.append("  a.*,  ");
			sql.append("  CONCAT(p.`name`,c.`name`, area.`name`, a.address) AS storeAddress  ");
			sql.append(" FROM   ");
			sql.append("   (SELECT ");
			sql.append("    s.id,");
			sql.append("    s.no,");
			sql.append("    s.name,");
			sql.append("    s.type,");
			sql.append("    s.province_code,");
			sql.append("    s.city_code,");
			sql.append("    s.area_code,");
			sql.append("    s.address,");
			sql.append("    ssm.pro_count - ssm.store_lock_count as num");
			sql.append("   FROM ");
			sql.append("    t_store_sku_map ssm");
			sql.append("   LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
			sql.append("   WHERE  ssm.sku_code = ? ");
			sql.append("    and ssm.pro_count - ssm.store_lock_count >= ? ");
			sql.append("    and s.`status` =? ");
			sql.append("  ) a  ");
			sql.append("   LEFT JOIN t_address p ON a.province_code = p.`code` ");
			sql.append("   LEFT JOIN t_address c on a.city_code = c.`code` ");
			sql.append("   LEFT JOIN t_address area ON a.area_code = area.`code` ");
			         
			storeList = Db.find(sql.toString(), od.getStr("skuCode"), od.getNumber("count"), Store.RELEASE_STATUS_ENABLE);	
		}
		
		//补充运费列
		if(storeList != null && storeList.size() > 0){
			Map<String, Integer> productMap = new HashMap<String, Integer>();
			for(Record od : orderDetailList){
				productMap.put(od.getStr("skuCode"), od.getNumber("count").intValue());
			}
			
			Record toAddress = Order.dao.getToAddressCodeById(orderId);
			for (Record store : storeList) {
				BigDecimal freight = FreightTemplate.dao.calculate(productMap, store.getStr("no")
						,toAddress.getInt("provinceCode")
						, toAddress.getInt("cityCode")
						, toAddress.getInt("areaCode"));
				store.set("freight", freight);
			}
			//按运费排序
			Record curStore = null;
			for(int i = 0; i < storeList.size(); i++){
				for(int j = i + 1; j < storeList.size(); j++){
					if(storeList.get(i).getBigDecimal("freight").compareTo(storeList.get(j).getBigDecimal("freight")) > 0){
						curStore = storeList.get(i);
						storeList.set(i, storeList.get(j));
						storeList.set(j, curStore);
					}
				}
			}
		}
		
		return storeList;
	}
	
	
	/**
	 * 查看库存列表(库存不足)
	 * @param orderId
	 * @return
	 * @author chenhg
	 * 2016年11月17日 下午2:40:52
	 */
	public List<Record> getSendStoreListDetailNotEnough(String orderId){
		List<Record> orderDetailList = OrderDetail.dao.findSkuCodeList(orderId);
		
		List<Record> storeList = null;
		//多个sku
		if(orderDetailList.size() > 1){
			//1、查询出所有的满足库存的云店/仓库的id
			boolean flag = true;//是否为第一个
			for(Record od : orderDetailList){
				StringBuffer sql = new StringBuffer();
				if(flag){
					flag = false;
					sql.append(" SELECT ");
					sql.append("  s.id ");
					sql.append(" FROM ");
					sql.append("  t_store_sku_map ssm ");
					sql.append("  LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
					sql.append(" WHERE 1=1   ");
					sql.append(" and ssm.sku_code = ?   ");
					sql.append(" and s.`status` =?   ");
					storeList = Db.find(sql.toString(), od.getStr("skuCode"), Store.RELEASE_STATUS_ENABLE);
				}else{
					if(storeList != null && storeList.size() > 0){
						List<Object> paras = new ArrayList<Object>();
						sql.append(" SELECT ");
						sql.append("  s.id ");
						sql.append(" FROM ");
						sql.append("  t_store_sku_map ssm ");
						sql.append("  LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
						sql.append(" WHERE 1=1   ");
						sql.append(" and ssm.sku_code = ?   ");
						sql.append(" and s.`status` =?   ");
						
						paras.add(od.getStr("skuCode"));
						paras.add(Store.RELEASE_STATUS_ENABLE);
						
						sql.append(" and s.id in(   ");
						for(Record s: storeList){
							sql.append(" ?,");
							paras.add(s.getNumber("id"));
						}
						sql.deleteCharAt(sql.length()-1);  
						sql.append(")   ");
						storeList = Db.find(sql.toString(), paras.toArray());
					}
				}
			}
			//2、查询云店/仓库的详细信息
			if(storeList != null && storeList.size() > 0){
				StringBuffer resutlSql = new StringBuffer();
				List<Object> resutlParas = new ArrayList<Object>();
				resutlSql.append(" SELECT   ");
				resutlSql.append("  a.*,  ");
				resutlSql.append("  CONCAT(p.`name`,c.`name`, area.`name`, a.address) AS storeAddress  ");
				resutlSql.append(" FROM   ");
				resutlSql.append("   (SELECT ");
				resutlSql.append("    s.id,");
				resutlSql.append("    s.no,");
				resutlSql.append("    s.name,");
				resutlSql.append("    s.type,");
				resutlSql.append("    s.province_code,");
				resutlSql.append("    s.city_code,");
				resutlSql.append("    s.area_code,");
				resutlSql.append("    s.address,");
				resutlSql.append("    '不足' as num ");
				resutlSql.append("   FROM ");
				resutlSql.append("    t_store s ");
				resutlSql.append("   WHERE 1=1 ");
				resutlSql.append("   and s.id in(");
				for(Record s: storeList){
					resutlSql.append(" ?,");
					resutlParas.add(s.getNumber("id"));
				}
				resutlSql.deleteCharAt(resutlSql.length()-1); 
				resutlSql.append(") ");
				resutlSql.append("  ) a  ");
				resutlSql.append(" LEFT JOIN t_address p ON a.province_code = p.`code`  ");
				resutlSql.append(" LEFT JOIN t_address c on a.city_code = c.`code`  ");
				resutlSql.append(" LEFT JOIN t_address area ON a.area_code = area.`code`  ");
				
				storeList = Db.find(resutlSql.toString(), resutlParas.toArray());  
			}
			
		}else{
			StringBuffer sql = new StringBuffer();
			//查询云店/仓库的详细信息
			Record od = orderDetailList.get(0);
			sql.append(" SELECT   ");
			sql.append("  a.*,  ");
			sql.append("  CONCAT(p.`name`,c.`name`, area.`name`, a.address) AS storeAddress  ");
			sql.append(" FROM   ");
			sql.append("   (SELECT ");
			sql.append("    s.id,");
			sql.append("    s.no,");
			sql.append("    s.name,");
			sql.append("    s.type,");
			sql.append("    s.province_code,");
			sql.append("    s.city_code,");
			sql.append("    s.area_code,");
			sql.append("    s.address,");
			sql.append("    ssm.pro_count - ssm.store_lock_count as num");
			sql.append("   FROM ");
			sql.append("    t_store_sku_map ssm");
			sql.append("   LEFT JOIN t_store s ON ssm.store_no = s.`no` ");
			sql.append("   WHERE  ssm.sku_code = ? ");
			sql.append("    and s.`status` =? ");
			sql.append("  ) a  ");
			sql.append("   LEFT JOIN t_address p ON a.province_code = p.`code` ");
			sql.append("   LEFT JOIN t_address c on a.city_code = c.`code` ");
			sql.append("   LEFT JOIN t_address area ON a.area_code = area.`code` ");
			         
			storeList = Db.find(sql.toString(), od.getStr("skuCode"), Store.RELEASE_STATUS_ENABLE);	
		}
		
		//补充运费列
		if(storeList != null && storeList.size() > 0){
			Map<String, Integer> productMap = new HashMap<String, Integer>();
			for(Record od : orderDetailList){
				productMap.put(od.getStr("skuCode"), od.getNumber("count").intValue());
			}
			
			Record toAddress = Order.dao.getToAddressCodeById(orderId);
			for (Record store : storeList) {
				BigDecimal freight = FreightTemplate.dao.calculate(productMap, store.getStr("no")
						,toAddress.getInt("provinceCode")
						, toAddress.getInt("cityCode")
						, toAddress.getInt("areaCode"));
				store.set("freight", freight);
			}
			//按运费排序
			Record curStore = null;
			for(int i = 0; i < storeList.size(); i++){
				for(int j = i + 1; j < storeList.size(); j++){
					if(storeList.get(i).getBigDecimal("freight").compareTo(storeList.get(j).getBigDecimal("freight")) > 0){
						curStore = storeList.get(i);
						storeList.set(i, storeList.get(j));
						storeList.set(j, curStore);
					}
				}
			}
		}
		
		return storeList;
	}
	
	/**
	 * 获取所有云店信息
	 * @return
	 * @author Jacob
	 * 2016年3月21日下午8:25:28
	 */
	public List<Record> findList(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	* ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = ? ");
		sql.append(" AND s.type = ? ");
		return Db.find(sql.toString(),RELEASE_STATUS_ENABLE, TYPE_CLOUD);
	}
	
	/**
	 * 根据区编号获取可发货的云店编号列表
	 * @param areaCode 区编号
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月28日下午8:49:41
	 */
	public List<String> findNoListByAreaCode(Integer areaCode,String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.no storeNo ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" LEFT JOIN t_store_sku_map ssm ON ssm.store_no = s.no ");
		sql.append(" LEFT JOIN t_order_detail od ON ssm.sku_code = od.sku_code ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = 1 ");
		sql.append(" AND ssm.pro_count - ssm.store_lock_count >= od.count ");
		sql.append(" AND s.area_code = ? ");
		sql.append(" AND od.order_id = ?  GROUP BY storeNo");
		return Db.query(sql.toString(), areaCode, orderId);
	} 
	
	/**
	 * 根据区编号跟市编号获取可发货的同市内其它区分仓编号列表
	 * @param areaCode 区编号
	 * @param cityCode 市编号
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月28日下午8:49:41
	 */
	public List<String> findNoListByAreaCodeForOther(Integer areaCode,Integer cityCode,String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.no storeNo ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" LEFT JOIN t_store_sku_map ssm ON ssm.store_no = s.no ");
		sql.append(" LEFT JOIN t_order_detail od ON ssm.sku_code = od.sku_code ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = 1 ");
		sql.append(" AND ssm.pro_count - ssm.store_lock_count >= od.count ");
		sql.append(" AND s.area_code != ? ");
		sql.append(" AND s.city_code = ? ");
		sql.append(" AND od.order_id = ?  GROUP BY storeNo");
		return Db.query(sql.toString(), areaCode, cityCode, orderId);
	} 
	
	/**
	 * 根据市编号获取可发货的分仓编号列表
	 * @param cityCode 市编号
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月28日下午8:49:41
	 */
	public List<String> findNoListByCityCode(Integer cityCode,String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.no storeNo ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" LEFT JOIN t_store_sku_map ssm ON ssm.store_no = s.no ");
		sql.append(" LEFT JOIN t_order_detail od ON ssm.sku_code = od.sku_code ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = 1 ");
		sql.append(" AND ssm.pro_count - ssm.store_lock_count >= od.count ");
		sql.append(" AND s.city_code = ? ");
		sql.append(" AND od.order_id = ?  GROUP BY storeNo");
		return Db.query(sql.toString(), cityCode, orderId);
	}
	
	/**
	 * 根据市编号跟省编号获取可发货的同省份内其他城市分仓编号列表
	 * @param areaCode 市编号
	 * @param provinceCode 省编号
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月28日下午8:49:41
	 */
	public List<String> findNoListByCityCodeForOther(Integer cityCode, Integer provinceCode, String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.no storeNo ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" LEFT JOIN t_store_sku_map ssm ON ssm.store_no = s.no ");
		sql.append(" LEFT JOIN t_order_detail od ON ssm.sku_code = od.sku_code ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = 1 ");
		sql.append(" AND ssm.pro_count - ssm.store_lock_count >= od.count ");
		sql.append(" AND s.city_code != ? ");
		sql.append(" AND s.province_code = ? ");
		sql.append(" AND od.order_id = ?  GROUP BY storeNo");
		return Db.query(sql.toString(), cityCode, provinceCode, orderId);
	}
	
	/**
	 * 根据省编号获取可发货的分仓编号列表
	 * @param provinceCode 省编号
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月28日下午8:49:41
	 */
	public List<String> findNoListByProvinceCode(Integer provinceCode,String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.no storeNo ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" LEFT JOIN t_store_sku_map ssm ON ssm.store_no = s.no ");
		sql.append(" LEFT JOIN t_order_detail od ON ssm.sku_code = od.sku_code ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = 1 ");
		sql.append(" AND ssm.pro_count - ssm.store_lock_count >= od.count ");
		sql.append(" AND s.province_code = ? ");
		sql.append(" AND od.order_id = ?  GROUP BY storeNo");
		return Db.query(sql.toString(), provinceCode, orderId);
	}
	
	/**
	 * 根据省编号获取可发货的其它省份分仓编号列表
	 * @param provinceCode 省编号
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月28日下午8:49:41
	 */
	public List<String> findNoListByProvinceCodeForOther(Integer provinceCode,String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.no storeNo ");
		sql.append(" FROM ");
		sql.append(" 	t_store s ");
		sql.append(" LEFT JOIN t_store_sku_map ssm ON ssm.store_no = s.no ");
		sql.append(" LEFT JOIN t_order_detail od ON ssm.sku_code = od.sku_code ");
		sql.append(" WHERE ");
		sql.append(" 	s.release_status = 1 ");
		sql.append(" AND ssm.pro_count - - ssm.store_lock_count >= od.count ");
		sql.append(" AND s.province_code != ? ");
		sql.append(" AND od.order_id = ?  GROUP BY storeNo");
		return Db.query(sql.toString(), provinceCode, orderId);
	}
	
	/**
	 * 判断总仓是否在某个区县内
	 * @param areaCode
	 * @return
	 * @author Jacob
	 * 2016年3月29日下午1:30:31
	 */
	public boolean checkTotalStoreInArea(Integer areaCode){
		String sql = "SELECT no FROM t_store s WHERE s.area_code = ? AND s.type = ? ";
		return StringUtil.notNull(Db.queryStr(sql, areaCode, TYPE_TOTAL))?true:false;
	}
	
	/**
	 * 判断总仓是否在某个城市内
	 * @param cityCode
	 * @return
	 * @author Jacob
	 * 2016年3月29日下午1:30:31
	 */
	public boolean checkTotalStoreInCity(Integer cityCode){
		String sql = "SELECT no FROM t_store s WHERE s.city_code = ? AND s.type = ? ";
		return StringUtil.notNull(Db.queryStr(sql, cityCode, TYPE_TOTAL))?true:false;
	}
	
	/**
	 * 判断总仓是否在某个省份内
	 * @param provinceCode
	 * @return
	 * @author Jacob
	 * 2016年3月29日下午1:30:31
	 */
	public boolean checkTotalStoreInProvince(Integer provinceCode){
		String sql = "SELECT no FROM t_store s WHERE s.province_code = ? AND s.type = ? ";
		return StringUtil.notNull(Db.queryStr(sql, provinceCode, TYPE_TOTAL))?true:false;
	}
	
	/**
	 * 判断总仓是否在某个省份外
	 * @param provinceCode
	 * @return
	 * @author Jacob
	 * 2016年3月29日下午1:30:31
	 */
	public boolean checkTotalStoreInProvinceForOther(Integer provinceCode){
		String sql = "SELECT no FROM t_store s WHERE s.province_code != ? AND s.type = ? ";
		return StringUtil.notNull(Db.queryStr(sql, provinceCode, TYPE_TOTAL))?true:false;
	}
	
	
	/**
	 * 自动分配发货（系统分配）
	 * @param order
	 * 			当前订单
	 * @param skuCode
	 * 			商品sku
	 * @param count
	 * 			购买数量
	 * @param addressId
	 * 			收货地址ID
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public boolean autoDistributionDeliver(Order order, List<Record> skuCodeCountList,
			Integer addressId) throws SQLException {
		String orderId = order.getStr("id");
		// 根据收货地址ID获取区、市、省编号
		RecAddress proCityArea = 
				RecAddress.dao.findByIdLoadColumns(addressId, 
						"province_code, city_code, area_code");
		// 收货地址所在省编号
		int provinceCode = proCityArea.getInt("province_code");
		// 收货地址所在市编号
		int cityCode = proCityArea.getInt("city_code");
		// 收货地址所在区编号
		int areaCode = proCityArea.getInt("area_code");
		// 根据收货地址所在省编号获取该省的所有分仓编号
		List<String> provinceStoreNoList = dao.findNoListByProvinceCode(provinceCode, orderId);
		
		boolean result = false;
		// 检查买家收货地址所属省内是否存在云店
		if (StringUtil.notNull(provinceStoreNoList) && provinceStoreNoList.size() > 0) {
			// 保存省内所有云店和对应的发货运费
			Map<String, BigDecimal> StoreFreightMap = new LinkedHashMap<String, BigDecimal>();
			
			// 运费计算所需map
			Map<String, Integer> skuCodeCountMap = new HashMap<String, Integer>();
			// 组装所有skuCode用于比较云店库存
			StringBuffer skuCodes = new StringBuffer();
			// 将所有sku和购买数量放进运费计算的map
			for (Record record : skuCodeCountList) {
				skuCodeCountMap.put(record.getStr("skuCode"), record.getInt("count"));
				skuCodes.append("'" + record.getStr("skuCode") + "',");
			}
			
			// 开始计算省内所有云店运费
			for (String storeNo : provinceStoreNoList) {
				// 云店代发运费
				BigDecimal freight = new BigDecimal("0.00");
				// 计算出最终运费
				freight = freight.add(FreightTemplate.dao.calculate(skuCodeCountMap, storeNo, provinceCode, cityCode, areaCode));
				// 保存云店和对应发货运费
				StoreFreightMap.put(storeNo, freight);
			}
			
			// 对delieverStoreList进行value排序（对云店发货运费进行升序排序）
			StoreFreightMap = new MapValueComparator().sortMap(StoreFreightMap);
			
			// 发货云店列表
			List<String> deliverStoreList = new ArrayList<>();
			// 临时变量 记录前一间云店的运费和编号
			BigDecimal tempFreight =  null;
			String tempStoreNo = null;
			// 记录前一间云店的下标
			int insertIndex = 0;
			
			// 将发货运费排序好的Map循环比较相同运费的云店库存
			for (String storeNo : StoreFreightMap.keySet()) {
				// 第一次直接放入
				if (StringUtil.isNull(tempFreight)) {
					deliverStoreList.add(storeNo);
				} else {
					// 
					if (tempFreight.compareTo(StoreFreightMap.get(storeNo)) == 0) {
						String moreStore = StoreSkuMap.dao.compareStroeCount(skuCodes.substring(0, skuCodes.length() - 1), storeNo, tempStoreNo);
						if (storeNo.equals(moreStore)) {
							deliverStoreList.add(insertIndex, storeNo);
						} else {
							deliverStoreList.add(storeNo);
							insertIndex = deliverStoreList.size() - 1;
						}
					} else {
						deliverStoreList.add(storeNo);
						insertIndex = deliverStoreList.size() - 1;
					}
				}
				
				tempFreight = StoreFreightMap.get(storeNo);
				tempStoreNo = storeNo;
			}
			
			for (String storeNo : deliverStoreList) {
				if (StoreSkuMap.dao.enoughStoreCountForMany(storeNo, skuCodeCountList)) {
					// 转换锁定库存数   批量检查能否转换
					if (ProductSku.dao.transferLockCountForMany(storeNo, skuCodeCountList)) {
						// 发货云店地址
						Record storeAddress = Store.dao.getStoreAddressByNo(storeNo);
						
						order.set("o2o_shop_no", storeNo);
						order.set("o2o_shop_name", storeAddress.getStr("name"));
						order.set("o2o_shop_address", storeAddress.getStr("address"));
						order.set("distribution_time", new Date());
						order.set("store_freight", StoreFreightMap.get(storeNo));
						order.update();
						result = true;
						break;// 一次就够了
					} else { // 转换锁定库存数不成功，事务回滚，转到商家自行分配
						DbKit.getConfig().getConnection().rollback();
						return false;
					}
				}
			}
		}
		// 收货地址所属省内不存在云店，转到商家自行分配发货
		return result;
	}
	
	
	
	/**
	 * 云店同省发货
	 * @param provinceCode
	 * 			省地址码
	 * @param cityCode
	 * 			市地址码
	 * @param areaCode
	 * 			区地址码
	 * @param orderId
	 * 			订单ID
	 * @param totolStoreNo
	 * 			总仓编号
	 * @param skuCodeCountList
	 * 			
	 * @param order
	 */
	public boolean storeDeliverByProv(int provinceCode, 
			int cityCode, int areaCode, String orderId,
			String totolStoreNo, List<Record> skuCodeCountList, Order order) {
		
		// 根据收货地址所在城市编号获取该城市的所有分仓编号
		List<String> cityStoreNoList = dao.findNoListByCityCode(cityCode, orderId);
		// 获取收货地址所在省份的其他城市的分仓编号
		List<String> otherCityStoreNoList = 
				dao.findNoListByCityCodeForOther(cityCode, provinceCode, orderId);
		
		if (StringUtil.notNull(cityStoreNoList) && cityStoreNoList.size() > 0) {
			return storeDeliverByCity(cityCode, areaCode, orderId, totolStoreNo, skuCodeCountList, order);
		
		/********收货地址所在省份内其他城市是否有满足发货的分仓*****/
		} else {
			// 根据其他城市云店列表进行分配发货
			return distributionDeliveryByStoreNoList(otherCityStoreNoList, totolStoreNo, skuCodeCountList, order);
		}
	}
	
	/**
	 * 云店同市发货
	 * @param cityCode
	 * @param areaCode
	 * @param orderId
	 * @param totolStoreNo
	 * @param skuCodeCountList
	 * @param order
	 */
	public boolean storeDeliverByCity(int cityCode, int areaCode, String orderId, 
			String totolStoreNo, List<Record> skuCodeCountList, Order order) {
		/****最后判断收货地址所在城市是否有满足发货的分仓******/
		// 根据收货地址所在区编号获取该区县的所有分仓编号
		List<String> areaStoreNoList = dao.findNoListByAreaCode(areaCode, orderId);
		// 获取收货地址所在省份的其他城市的分仓编号
		List<String> otherAreaStoreNoList = dao.findNoListByAreaCodeForOther(areaCode, cityCode, orderId);
		
		if (StringUtil.notNull(areaStoreNoList) && areaStoreNoList.size() > 0) {
			// 根据同区的云店列表进行分配发货
			return distributionDeliveryByStoreNoList(areaStoreNoList, totolStoreNo, skuCodeCountList, order);
			
		/********收货地址所在城市内其他区县是否有满足发货的分仓*****/
		} else {
			// 根据同城市其他区的云店列表进行分配发货
			return distributionDeliveryByStoreNoList(otherAreaStoreNoList, totolStoreNo, skuCodeCountList, order);
		}
	}
	
	/**
	 * 根据仓库列表进行发配发货
	 * @param storeNoList
	 * 			仓库列表
	 * @param totolStoreNo
	 * 			总仓编号
	 * @param skuCodeCountList
	 * 			订单sku码列表
	 * @param order
	 * 			当前订单
	 */
	public boolean distributionDeliveryByStoreNoList(List<String> storeNoList, 
			String totolStoreNo, List<Record> skuCodeCountList, Order order) {
		boolean result = false;
		try {
			// 优先总仓
			if (storeNoList.contains(totolStoreNo)) {
				if (StoreSkuMap.dao.enoughStoreCountForMany(totolStoreNo, skuCodeCountList)) {
					// 转换锁定库存数
					if (ProductSku.dao.transferLockCountForMany(totolStoreNo, 
							skuCodeCountList)) {
						Record address = dao.getStoreAddressByNo(totolStoreNo);
						order.set("o2o_shop_no", totolStoreNo);
						order.set("o2o_shop_name", address.getStr("name"));
						order.set("o2o_shop_address", address.getStr("address"));
						order.set("distribution_time", new Date());
						
						return true;
					} else {
						// 事务回滚
						DbKit.getConfig().getConnection().rollback();
						return false;
					}
				}
			}
			for (String storeNo : storeNoList) {
				if(storeNo.equals(totolStoreNo)){
					continue;
				}
				if (StoreSkuMap.dao.enoughStoreCountForMany(storeNo, skuCodeCountList)) {
					if (ProductSku.dao.transferLockCountForMany(storeNo, 
							skuCodeCountList)) {
						Record address = dao.getStoreAddressByNo(storeNo);
						order.set("o2o_shop_no", storeNo);
						order.set("o2o_shop_name", address.getStr("name"));
						order.set("o2o_shop_address", address.getStr("address"));
						order.set("distribution_time", new Date());
						
						result = true;
						break;
					} else {
						// 事务回滚
						DbKit.getConfig().getConnection().rollback();
						return false;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 判断仓库是否存在
	 * @param storeNo
	 * @return
	 * @author Jacob
	 * 2016年4月11日下午8:30:57
	 */
	public boolean isExist(String storeNo){
		String sql = " select id from t_store where no = ? ";
		return Db.findFirst(sql,storeNo)!=null?true:false;
	}
	
	public Page<Store> lookUpByPage(Page<?> page, Ret paras) {
		StringBuffer select = new StringBuffer(" SELECT no, name ");
		
		StringBuffer where = new StringBuffer();
		where.append(" FROM t_store ");
		
		List<Object> paraList = new ArrayList<Object>();
		where.append(" WHERE release_status = ? ");	paraList.add(RELEASE_STATUS_ENABLE);
		where.append(" AND type = ? ");				paraList.add(TYPE_CLOUD);
		
		if (paras.notNull("storeNo")) {
			where.append(" AND no LIKE CONCAT('%', ?, '%')");
			paraList.add(paras.get("storeNo"));
		}
		if (paras.notNull("storeName")) {
			where.append(" AND name LIKE CONCAT('%', ?, '%')");
			paraList.add(paras.get("storeName"));
		}
		
		return Store.dao.paginate(
				page.getPageNumber(), page.getPageSize(),
				select.toString(), where.toString(),
				paraList.toArray()
		);
	}
	
	/**
	 * 根据云店/仓库编号 或者地址、名称信息
	 * @param stroeNo
	 * @return
	 */
	public Record getStoreAddressByNo(String storeNo){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  s.name, ");
		sql.append("  CONCAT(p.`name`,c.`name`,a.`name`,s.address) as address ");
		sql.append(" FROM  ");
		sql.append("  t_store s  ");
		sql.append(" LEFT JOIN t_address p ON s.province_code = p.`code`  ");
		sql.append(" LEFT JOIN t_address c ON s.city_code = c.`code`  ");
		sql.append(" LEFT JOIN t_address a ON s.area_code = a.`code`  ");
		sql.append(" where s.no = ?  ");
		
		return Db.findFirst(sql.toString(), storeNo);
	}
	
	/**
	 * 获取云店的运费模板.
	 * 
	 * @author Chengyb
	 */
	public Integer findFreightTemplate(String storeNo) {
		return Db.queryInt("SELECT freight_template_id FROM t_store WHERE no = ?", storeNo);
	}
	
	
	public List<Record> getAllStoreFreightId() {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  s.name, ");
		sql.append("  s.freight_template_id, ");
		sql.append("  CONCAT(p.`name`,c.`name`,a.`name`,s.address) as address ");
		sql.append(" FROM  ");
		sql.append("  t_store s  ");
		sql.append(" LEFT JOIN t_address p ON s.province_code = p.`code`  ");
		sql.append(" LEFT JOIN t_address c ON s.city_code = c.`code`  ");
		sql.append(" LEFT JOIN t_address a ON s.area_code = a.`code`  ");
		sql.append(" where s.release_status = ?  ");
		sql.append(" and s.type = ? and s.freight_template_id is not null ");
		
		return Db.find(sql.toString(), BaseConstants.YES, TYPE_CLOUD);
	}
	
	/**
	 * 获取云店列表.
	 * 
	 * @param provinceCode
	 *            省编码.
	 * @param cityCode
	 *            市编码.
	 * @param areaCode
	 *            区编码.
	 * @author Chengyb
	 * @return 
	 */
	public List<Record> getAllCloudStore(Integer provinceCode, Integer cityCode, Integer areaCode) {
		List<Integer> paraList = new ArrayList<Integer>();
		StringBuffer sql = new StringBuffer("SELECT id, name, province_code, city_code, area_code, address, principal, tel, longitude, latitude, mobile FROM t_store WHERE type = 1 AND release_status = 1 AND status = 1");
	    // 省.
		if(null != provinceCode) {
	    	sql.append(" AND ");
	    	sql.append(" province_code = ?");
	    	paraList.add(provinceCode);
	    }
		// 市.
		if(null != cityCode) {
			sql.append(" AND ");
			sql.append(" city_code = ?");
			paraList.add(cityCode);
		}
		// 区.
	    if(null != areaCode) {
	    	sql.append(" AND ");
	    	sql.append(" area_code = ?");
	    	paraList.add(areaCode);
	    }
	    List<Record> list = Db.find(sql.toString(), paraList.toArray());
	    if(null != list && list.size() > 0) {
	    	return changeAddressCodeToName(list);
	    }
		return list;
	}
	
	/**
	 * 查看可选门店
	 * @param productId
	 * @return
	 */
	public List<Record> getShowO2OShop(Integer productId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT DISTINCT  ");
		sql.append(" p.`name` o2oShopName, ");
		sql.append(" p.address o2oShopAddress");
		sql.append(" FROM ");
		sql.append(" t_o2o_sku_map skm ");
		sql.append(" LEFT JOIN t_store_sku_map t ON skm.sku_code = t.sku_code AND skm.store_no = t.store_no ");
		sql.append(" LEFT JOIN t_store p ON t.store_no = p. NO ");
		sql.append("  ");
		sql.append(" WHERE skm.product_id = ? ");
		sql.append(" AND t.pro_count - t.store_lock_count > 0 ");
		return Db.find(sql.toString(), productId);
	}
	
	/**
	 * 获取sku列表、云店列表
	 * @param productId
	 * @return
	 */
	public List<Record> getSkuAndStoreList(Integer productId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" b.`code` skuCode, ");
		sql.append(" c.valueIds ");
		sql.append(" FROM  v_com_sku b ");
		sql.append(" left join ( ");
		sql.append("   SELECT group_concat(valueId separator ',') valueIds,skuCode FROM v_com_sku_property WHERE productId = ? GROUP BY skuCode ");
		sql.append(" ) c on b.code = c.skuCode ");
		sql.append(" WHERE b.product_id = ? ");
		
		List<Record> list = Db.find(sql.toString(), productId, productId);
		for(Record rec : list){
			StringBuffer storeSql = new StringBuffer();
			storeSql.append(" SELECT ");
			storeSql.append("  s.`name`, ");
			storeSql.append("  ssm.pro_count - ssm.store_lock_count as total, ");
			storeSql.append("  CONCAT(p.`name`,' ',c.`name`, ' ',area.`name`,' ',s.address) as address ");
			storeSql.append(" FROM  ");
			storeSql.append("  (SELECT store_no,sku_code  FROM t_o2o_sku_map WHERE sku_code = ? ) a ");
			storeSql.append(" LEFT JOIN t_store_sku_map ssm ON a.sku_code = ssm.sku_code  ");
			storeSql.append(" LEFT JOIN t_store s ON a.store_no = s.`no`   ");
			storeSql.append(" LEFT JOIN t_address p ON s.province_code = p.`code`   ");
			storeSql.append(" LEFT JOIN t_address c ON s.city_code = c.`code`  ");
			storeSql.append(" LEFT JOIN t_address area ON s.area_code = area.`code`   ");
			storeSql.append(" WHERE  a.store_no = ssm.store_no  ");
			storeSql.append(" AND s.`status` = ?  ");
			
			rec.set("storeList", Db.find(storeSql.toString(), rec.getStr("skuCode"), Store.RELEASE_STATUS_ENABLE));
		}
		return list;
	}
}