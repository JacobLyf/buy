package com.buy.model.shop;

import com.jfinal.plugin.activerecord.Model;

public class ShopCertification extends Model<ShopCertification> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final ShopCertification dao = new ShopCertification();
	
	/**
	 * 审核状态：待审核
	 */
	public static final int AUDIT_STATUS_WAIT = 0;
	
	/**
	 * 审核状态：通过
	 */
	public static final int AUDIT_STATUS_PASS = 1;
	
	/**
	 * 审核状态：未通过
	 */
	public static final int AUDIT_STATUS_UNPASS = 2;
	
	/**
	 * 类型：店铺激活
	 */
	public static final int TYPE_ACTIVATION = 1;
	/**
	 * 类型：实名认证
	 */
	public static final int TYPE_VERIFIED = 2;
	/**
	 * 类型：假一赔十
	 */
	public static final int TYPE_PEISHI = 3;
	/**
	 * 类型：七天内退货认证
	 */
	public static final int TYPE_RETURN = 4;
	/**
	 * 类型：通讯保障认证
	 */
	public static final int TYPE_COMMUNICATION = 5;
	/**
	 * 类型：快速发货认证
	 */
	public static final int TYPE_EXPRESS_SERVICERS = 6;
	/**
	 * 类型：货运安全及包装认证
	 */
	public static final int TYPE_TRAFFIC_SAFETY = 7;
	/**
	 * 类型：卖家义务及违规处理
	 */
	public static final int TYPE_RESPONSIBILITY= 8;
	
	public ShopCertification getForLock(int cerId) {
		String sql = "SELECT * FROM t_shop_certification_record WHERE id = ? FOR UPDATE";
		return ShopCertification.dao.findFirst(sql, cerId);
	}
	
}
