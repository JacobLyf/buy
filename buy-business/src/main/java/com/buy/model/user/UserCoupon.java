package com.buy.model.user;  

import java.util.Date;

import com.buy.model.coupon.Coupon;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/** 
 * ClassName:UserCoupon 
 * Date:     2015年9月25日 下午3:47:01
 * @author   HuangSx
 * @version   
 * @see       
 */
public class UserCoupon extends Model<UserCoupon> {
	public static UserCoupon dao = new UserCoupon();
	/**
	 * 获取来源 
	 * 1:商城发放
	 */
	public static final int GET_FORM_EFUN=1;
	/**
	 * 获取来源 
	 * 2：店铺发放
	 */
	public static final int GET_FORM_SHOP=2;
	/**
	 * 获取来源 
	 * 3：市场活动
	 */
	public static final int GET_FORM_ACTION=3;
	/**
	 * 获取来源 
	 * 4：积分兑换
	 */
	public static final int GET_FORM_EXCHANGE=4;
	
	/**
	 * 未使用
	 */
	public static final int STATUS_NO_USED =0;
	/**
	 * 已使用
	 */
	public static final int STATUS_IS_USED =1;
	/**
	 * 已删除
	 */
	public static final int STATUS_IS_DEL = 2;
	/**
	 * 验证用户是否已经有了couponId的优惠券
	 * @author HuangSx
	 * @date : 2015年9月28日 下午4:45:25
	 * @param userId
	 * @param couponId
	 * @return
	 */
	public boolean isExist(String userId ,int couponId){
		String sql ="select count(a.id) from t_user_coupon_map a where a.user_id=? and a.coupon_id =?";
		return Db.queryLong(sql,userId,couponId) > 0;
	}
	
	/**
	 * 
	 * 激活/领取 优惠卷
	 * @author HuangSx
	 * @date : 2015年10月23日 上午9:33:24
	 * @param userId
	 * @param coupon
	 * @param getFrom  来源
	 * @return
	 */
	public boolean activeCoupon(String userId, Coupon coupon ,int getFrom ) {
		if(getFrom == UserCoupon.GET_FORM_EFUN || getFrom == UserCoupon.GET_FORM_SHOP){
			if(1 == coupon.getInt("shop_id")){
				getFrom = UserCoupon.GET_FORM_EFUN;
			}else {
				getFrom = UserCoupon.GET_FORM_SHOP;
			}
		}
		UserCoupon uc = new UserCoupon();
		return uc.set("user_id", userId)
			.set("coupon_id", coupon.getInt("id"))
			.set("create_time",new Date())
			.set("get_from", getFrom)
			.set("expiration_time", coupon.get("end_date"))
			.save();
	}
	
	
}
  