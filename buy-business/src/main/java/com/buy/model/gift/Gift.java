package com.buy.model.gift;  

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/** 
 * ClassName:Gift  
 * Date:     2015年10月23日 下午1:24:06  
 * @author   HuangSx
 * @version   
 * @see       
 */
public class Gift extends Model<Gift> {
	public static final Gift dao = new Gift();
	
	/**
	 * 类型：1优惠券
	 */
	public static final int TYPE_COUPON = 1;
	/**
	 * 类型：2 商品
	 */
	public static final int TYPE_PRODUCT =2;
	/**
	 * 下架
	 */
	public static final int STATUS_DOWN =0;
	/**
	 * 上架
	 */
	public static final int STATUS_UP =1;
	/**
	 * 删除
	 */
	public static final int STATUS_DELETE =2;
	
	
	/**
	 * 根据礼品id来获取该礼品的数量
	 * @author HuangSx
	 * @date : 2015年10月27日 下午1:07:02
	 * @return
	 */
	public int getGiftNum(int giftId){
		String sql = "select a.count from t_gift a  where a.id =? ";
		return Db.queryInt(sql,giftId);
	}
}
      