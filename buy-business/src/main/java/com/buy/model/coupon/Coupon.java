package com.buy.model.coupon;  

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/** 
 * ClassName:Coupon 
 * Date:     2015年9月25日 下午3:44:01
 * @author   HuangSx
 * @version   
 * @see       
 */
public class Coupon extends Model<Coupon> {
	/**
	 * 商城优惠券.
	 */
	public static final int  TYPE_MALL = 1;
	/**
	 * 店铺优惠券.
	 */
	public static final int  TYPE_SHOP = 2;
	/**
	 * 未发布状态
	 */
	public static final int  STATUS_UN_PUBLISHED = 0;
	/**
	 * 发布状态
	 */
	public static final int  STATUS_PUBLISHED = 1;
	/**
	 * 删除状态
	 */
	public static final int  STATUS_DELETE = 2;
	/**
	 * 未使用
	 */
	public static final int UN_USE = 0;
	/**
	 * 已使用
	 */
	public static final int USED = 1;
	/**
	 * 已失效
	 */
	public static final int OVERDATE =2;
	/**
	 * 即将过期天数
	 */
	public static final int SOON_EXPIRE_INTERVAL = 3;
	
	public static Coupon dao = new Coupon();
	
	/**
	 * 判断用户是否拥有该店铺的优惠券
	 * @param userId
	 * @param shopId
	 * @return
	 * boolean
	 * @author wangy
	 * @date 2015年9月29日 上午9:42:37
	 */
	public boolean hasCouponOfShop(String userId,int shopId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT COUNT(b.id) count");
		sql.append(" FROM");
		//查询用户可用优惠券
		sql.append(" (SELECT coupon_id FROM t_user_coupon_map WHERE is_used=0 and user_id=?) as a");
		//查询店铺有效优惠券
		sql.append(" ,(SELECT id FROM t_discount_coupon WHERE shop_id=? AND end_date>=CURDATE()) as b");
		sql.append(" WHERE");
		sql.append(" a.coupon_id=b.id");
		Record record = Db.findFirst(sql.toString(), new Object[]{userId,shopId});
		long count = record.getLong("count");
		return count>0;
	}
	/**
	 * 验证是否存在该优惠券
	 * 存在就返回优惠券coupon
	 * @author HuangSx
	 * @date : 2015年9月28日 下午4:16:38
	 * @param code
	 * @return
	 */
	public Coupon isExist(String code) {
		String sql ="select a.id,a.shop_id,a.discount from t_discount_coupon a where a.code = ?";
		return Coupon.dao.findFirst(sql,code);
	}
	
	
}
  