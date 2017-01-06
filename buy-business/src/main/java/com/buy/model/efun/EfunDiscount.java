package com.buy.model.efun;

import com.buy.common.JsonMessage;
import com.buy.radomutil.WeightData;
import com.buy.radomutil.WeightRandom;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 幸运翻牌购翻牌概率表
 */
public class EfunDiscount extends Model<EfunDiscount>{
	
	private static final long serialVersionUID = 1L;
	public static final EfunDiscount dao = new EfunDiscount();
	
	/**
	 * 获取折扣
	 *   获取前
	 *   	1、需要验证该参与记录是否属于当前会员
	 *   	2、需要验证该参与记录是否已经获取过折扣值了
	 * @return
	 */
	public EfunDiscount getEfunDiscountVal(String[] discountIds){
		//得到所有的折扣记录
		List<EfunDiscount> list = getAllEfunDiscount();
		List<WeightData> all = new ArrayList<WeightData>();
		for(EfunDiscount ed : list){
			String edId = ed.get("id").toString();
			BigDecimal probability = ed.getBigDecimal("probability");
			int weight = probability.multiply(new BigDecimal("100")).intValue();
			WeightData wc = new WeightData(edId,weight);
			all.add(wc);
		}
    	//得到折扣id
		String edId = WeightRandom.getWeigthData(all);
		EfunDiscount resultED = EfunDiscount.dao.findById(edId);
		//更新会员参与记录的折扣值
		//eod.set("discount_val", resultED.getBigDecimal("value")).update();
		List<Object> params = new ArrayList<>();
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE t_efun_order_detail SET discount_val="+resultED.getBigDecimal("value")+" WHERE id IN(");

		for(int i=0;i<discountIds.length;i++){
			if(i < discountIds.length - 1){
				sql.append("?, ");
			}else{
				sql.append("?");
			}
			params.add(discountIds[i]);
		}
		sql.append(")");

		Db.update(sql.toString(),params.toArray());
		return resultED;
	}
	
	/**
	 * 得到折扣表的所有记录
	 * @return
	 */
	public List<EfunDiscount> getAllEfunDiscount(){
		String sql = "select * from t_efun_discount order by value asc";
		return EfunDiscount.dao.find(sql);
	}
	
	/**
	 * 得到非抽中的折扣记录
	 * @return
	 */
	public List<EfunDiscount> getNoWinEfunDiscount(int edId){
		String sql = "select name from t_efun_discount where id <> ?";
		List<EfunDiscount> result = EfunDiscount.dao.find(sql, edId);
		Collections.shuffle(result);
		return result;
	}
	
	
	/**
	 * 获取折扣
	 * （验证重复点击）
	 * @param efunOrderId
	 * @param userId
	 * @return
	 */
	public JsonMessage efunDiscount(String efunOrderId, String userId,String[] discountIds){
		JsonMessage jsonMessage = new JsonMessage();
		//获取参与记录
		EfunUserOrder euo = EfunUserOrder.dao.getEfunOrder(efunOrderId, userId);
		EfunOrderDetail eod = EfunOrderDetail.dao.findById(discountIds[0]);
		//抽取折扣
		EfunDiscount ed = getEfunDiscountVal(discountIds);
		Record win = new Record();
		BigDecimal eqPrice = euo.getBigDecimal("eq_price");
		BigDecimal discountVal = ed.getBigDecimal("value");
		//保留两位小数，向上取整
		BigDecimal discountPrice = (eqPrice.multiply(discountVal)).setScale(2, BigDecimal.ROUND_CEILING);
		
		win.set("name", ed.get("name"));
		win.set("eqPrice", eqPrice);
		win.set("discountPrice", discountPrice);
		
		//获取没抽中的折扣名称
		List<EfunDiscount> noWin = EfunDiscount.dao.getNoWinEfunDiscount(ed.getInt("id"));
		
		Record result = new Record();
		result.set("win", win);
		result.set("noWin", noWin);
		jsonMessage.setData(result);
		
		return jsonMessage;
	}

	/**
	 * 获取翻牌折扣.
	 * 
	 * @author Chengyb
	 */
	public BigDecimal getEfunDiscount() {
		// 得到所有的折扣记录.
		List<EfunDiscount> list = getAllEfunDiscount();
		List<WeightData> all = new ArrayList<WeightData>();
		for(EfunDiscount ed : list){
			String edId = ed.get("id").toString();
			BigDecimal probability = ed.getBigDecimal("probability");
			int weight = probability.multiply(new BigDecimal("100")).intValue();
			WeightData wc = new WeightData(edId,weight);
			all.add(wc);
		}
    	// 得到折扣id.
		String edId = WeightRandom.getWeigthData(all);
		EfunDiscount resultED = EfunDiscount.dao.findById(edId);
		return resultED.getBigDecimal("value");
	}
	
}