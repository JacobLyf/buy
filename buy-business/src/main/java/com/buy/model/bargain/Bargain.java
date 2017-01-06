package com.buy.model.bargain;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.buy.date.DateUtil;
import com.buy.model.activity.Activity;
import com.buy.qrCode.QrCodeUtil;
import com.buy.radomutil.WeightData;
import com.buy.radomutil.WeightRandom;
import com.buy.string.StringUtil;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 讨钱.
 * 
 * @author Chengyb
 */
public class Bargain extends Model<Bargain> {

	private static final long serialVersionUID = 3353002042759015254L;
	
	public static final Bargain dao = new Bargain();
	
	/**
	 * 获取用户可以讨钱的订单.
	 * 
	 * @author Chengyb
	 */
	public List<Record> getBargainOrders(String userId) {
		StringBuffer sql = new StringBuffer("SELECT t.id AS order_id, t.efun_discount, o.product_id, o.product_name, o.product_img, o.sku_code, o.price, s.begin_time, s.back_amount, s.record FROM t_order t ");
		sql.append(" LEFT JOIN ");
		sql.append(" t_order_detail o");
		sql.append(" ON ");
		sql.append(" t.id = o.order_id ");
		sql.append(" LEFT JOIN ");
		sql.append(" t_bargain s ");
		sql.append(" ON ");
		sql.append(" t.id = s.order_id ");
		sql.append(" WHERE ");
		sql.append(" t.user_id = '").append(userId).append("'");
		sql.append(" AND t.is_efun_nine = 1");
		sql.append(" AND t.comfirm_time >= '").append(Activity.BARGAIN_START_TIME).append("'");
		sql.append(" AND t.comfirm_time <= '").append(Activity.BARGAIN_END_TIME).append("'");
		sql.append(" ORDER BY ");
		sql.append(" t.comfirm_time DESC ");
		return Db.find(sql.toString());
	}
	
	/**
	 * 生成讨钱二维码.
	 * 
	 * @author Chengyb
	 */
	public void bargainQrCode(String userId, String orderId, OutputStream outputStream) throws Exception {
		if(StringUtils.isNotBlank(orderId)) {
			String sql = " SELECT t.id FROM t_order t WHERE t.id = '" + orderId + "' AND t.user_id = " + userId + " AND t.is_efun_nine = 1 AND t.comfirm_time >= '" + Activity.BARGAIN_START_TIME + "' AND t.comfirm_time <= '" + Activity.BARGAIN_END_TIME + "' ORDER BY t.comfirm_time DESC ";
			
			List<Record> list = Db.find(sql);
			
			if(null != list && list.size() > 0) {
				// 启动计时.
				try{
					Db.update("INSERT INTO t_bargain VALUES ('" + orderId + "', NOW(), NULL, NULL)");
				} catch(Exception e) {
				}
				// 生成二维码流文件.
				QrCodeUtil.generateQRCode(PropKit.use("global.properties").get("app.domain") + "/ / ?orderId=" + orderId, outputStream, "png", 176, 176);
			}
		}
	}
	
	/**
	 * 获取讨钱订单详情.
	 * 
	 * @author Chengyb
	 */
	public Map<String, Object> getBargainOrderDetails(String orderId, String wxId) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		StringBuffer sql = new StringBuffer("SELECT t.id AS order_id, t.efun_discount, t.user_name, p.avatar, o.product_id, o.product_name, o.product_img, o.sku_code, o.price, s.begin_time, s.number, s.back_amount, s.record FROM t_order t ");
		sql.append(" LEFT JOIN ");
		sql.append(" t_order_detail o");
		sql.append(" ON ");
		sql.append(" t.id = o.order_id ");
		sql.append(" LEFT JOIN ");
		sql.append(" t_bargain s ");
		sql.append(" ON ");
		sql.append(" t.id = s.order_id ");
		sql.append(" LEFT JOIN ");
		sql.append(" t_user p ");
		sql.append(" ON ");
		sql.append(" t.user_id = p.id ");
		sql.append(" WHERE ");
		sql.append(" t.id = '").append(orderId).append("'");
		sql.append(" AND t.is_efun_nine = 1");
		sql.append(" AND t.comfirm_time >= '").append(Activity.BARGAIN_START_TIME).append("'");
		sql.append(" AND t.comfirm_time <= '").append(Activity.BARGAIN_END_TIME).append("'");
		sql.append(" ORDER BY ");
		sql.append(" t.comfirm_time DESC ");
		Record record = Db.findFirst(sql.toString());
		
		if(null != record) {
			// 讨钱开始时间.
			Date date = record.getDate("begin_time");
			if(null != date) {
				if((new Date()).before(DateUtil.addHour(date, 1))) { // 讨钱还没结束.
					// 已讨金额.
					BigDecimal backAmount = record.getBigDecimal("back_amount");
					if(null == backAmount) {
						backAmount = new BigDecimal(0.00);
					}
					if(backAmount.compareTo(record.getBigDecimal("price").multiply(new BigDecimal(0.1)).setScale(2, BigDecimal.ROUND_HALF_UP)) == -1) { // 讨钱未达到一折购金额.
						// 已讨次数.
						int number = record.getLong("number").intValue();
						if(number < 30) {
							// 讨钱记录.
							String history = record.getStr("record");
							if(null != history && history.indexOf(wxId) != -1) {
								map.put("message", "小伙伴太热情啦，已经帮TA讨过了哦~");
							}
						} else {
							map.put("message", "已达到该商品的讨钱次数，无法再为TA讨钱~");
						}
					} else {
						map.put("message", "其他小伙伴已将商品讨至免单，无法再为TA讨钱~");
					}
				} else { // 讨钱已超时.
					map.put("message", "很抱歉，“讨钱”已经结束了~");
				}
			}
			// 处理讨钱记录.
			List<JSON> list = new ArrayList<JSON>();
			String json = record.getStr("record");
			if(!StringUtil.isBlank(json)) {
				String[] array = json.split("\\|");
				for (int i = 0; i < array.length; i++) {
					list.add(JSON.parseObject(array[i]));	
				}
			}
			map.put("history", list);
			record.set("begin_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(DateUtil.addHour(record.getDate("begin_time"), 1)));
			map.put("record", record);
		} else { // 无效订单.
			map.put("message", "很抱歉，该订单号无效~");
		}
		return map;
	}
	
	/**
	 * 讨钱.
	 * 
	 * @author Chengyb
	 */
	public String bargain(String orderId, String wxId, String wxPortrait, String wxName) throws Exception {
		Map<String, Object> map = getBargainOrderDetails(orderId, wxId);
		
		if(!map.containsKey("message")) {
			Record record = (Record) map.get("record");
			
			String history = record.getStr("record");
			
			if(null == history || history.indexOf(wxId) == -1) {
				Bargain bargain = Bargain.dao.findFirst("select * from t_bargain a where a.order_id = ? for update", orderId);
				
				// 一折购金额.
				BigDecimal yiZhe = record.getBigDecimal("price").multiply(new BigDecimal(0.1));
				// 已讨金额.
				BigDecimal backAmount = record.getBigDecimal("back_amount") == null ? new BigDecimal(0.00) : record.getBigDecimal("back_amount");
				// 本次讨钱金额.
				List<WeightData>  categorys = new ArrayList<WeightData>();  
		    	WeightData wc1 = new WeightData("0.18", 40);
		    	WeightData wc2 = new WeightData("0.50", 40);
		    	WeightData wc3 = new WeightData("0.88", 10);
		    	WeightData wc4 = new WeightData("1.88", 5);
		    	WeightData wc5 = new WeightData("2.88", 4);
		    	WeightData wc6 = new WeightData("8.88", 1);
		    	
		    	categorys.add(wc6);
		    	categorys.add(wc5);
		    	categorys.add(wc4);
		    	categorys.add(wc3);
		        categorys.add(wc2);
		        categorys.add(wc1);
		        
		        BigDecimal currentAmount = new BigDecimal(WeightRandom.getWeigthData(categorys));
		        if(yiZhe.subtract(backAmount).compareTo(currentAmount) == -1) { // 本次讨钱金额已超过剩余金额.
		        	currentAmount = yiZhe.subtract(backAmount);
		        }
		        String money = currentAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
		        
		        // 记录本次讨钱.
		        Map<String, String> recordMap = new HashMap<String, String>();
		        recordMap.put("wxId", wxId);
		        recordMap.put("wxPortrait", wxPortrait);
		        recordMap.put("wxName", wxName);
		        recordMap.put("money", money);
		        recordMap.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		        
		        String json;
		        if(StringUtil.isBlank(history)) {
		        	json = JSON.toJSON(recordMap).toString();
		        } else {
		        	json = history + "|" + JSON.toJSON(recordMap).toString();
		        }
		        
		        // 更新讨钱.
		        bargain.set("number", (record.getLong("number").intValue() + 1));
		        bargain.set("back_amount", backAmount.add(currentAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
//		        bargain.set("record", "\"" + json.replaceAll("\"", "\\\\\"") + "\"");
		        bargain.set("record", json);
		        bargain.update();
		        
		        return money;
			} else {
				return "小伙伴太热情啦，已经帮TA讨过了哦~";
			}
		} else {
			return (String) map.get("message");
		}
	}
	
	/**
	 * 获取讨钱订单详情.
	 * 
	 * @author Chengyb
	 */
	public List<JSON> getBargainOrderRecords(String orderId) {
		StringBuffer sql = new StringBuffer("SELECT s.begin_time, s.number, s.back_amount, s.record ");
		sql.append("  FROM ");
		sql.append(" t_bargain s ");
		sql.append(" WHERE ");
		sql.append(" s.order_id = '").append(orderId).append("'");
		Record record = Db.findFirst(sql.toString());
		
		List<JSON> list = new ArrayList<JSON>();
		if(null != record) {
			// 处理讨钱记录.
			String json = record.getStr("record");
			if(!StringUtil.isBlank(json)) {
				String[] array = json.split("\\|");
				for (int i = 0; i < array.length; i++) {
					list.add(JSON.parseObject(array[i]));	
				}
			}
		}
		return list;
	}

}