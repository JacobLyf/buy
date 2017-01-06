package com.buy.model.shop;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;


public class ShopTransfer extends Model<ShopTransfer>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final ShopTransfer dao = new ShopTransfer();
	/**
	 * 审核状态:等待审核
	 */
	public static final int AUDIT_STATUS_WAIT = 0;
	/**
	 * 审核状态:通过
	 */
	public static final int AUDIT_STATUS_PASS = 1;
	/**
	 * 审核状态:不通过
	 */
	public static final int AUDIT_STATUS_UNPASS = 2;

	/**
	 * 状态:撤销待审核
	 */
	public static final int AUDIT_STATUS_CANCLE_WAIT = 3;
	/**
	 * 状态：已撤销
	 */
	public static final int AUDIT_STATUS_CANCLE_PASS = 4;
	/**
	 * 状态：撤销失败
	 */
	public static final int AUDIT_STATUS_CANCLE_UNPASS = 5;
	
	/**
	 * 店铺开通渠道 - 自主申请
	 */
	public static final int OPEN_CHANNEL_BYSELF = 1;
	/**
	 * 店铺开通渠道 - 代理商转出
	 */
	public static final int OPEN_CHANNEL_AGENT = 2;
	
	/**
	 * 查询店铺转出/撤销记录 - 锁行
	 */
	public ShopTransfer findByIdAndLock(Integer transferId) {
		return ShopTransfer.dao.findFirst("SELECT * FROM t_shop_transfer_record WHERE id = ? FOR UPDATE", transferId);
	}
	
	/**
	 * 查询店铺转出/撤销记录 - 锁行
	 */
	public ShopTransfer findByIdAndLock(Integer transferId, String agentId) {
		return ShopTransfer.dao.findFirst("SELECT * FROM t_shop_transfer_record WHERE id = ? AND agent_id = ? FOR UPDATE", transferId, agentId);
	}
	
	/**
	 * 撤销店铺转出初始化
	 * @author Sylveon 
	 */
	public void cancelToInit(String shopId) {
		// 店铺初始化
		StringBuffer update = new StringBuffer();
		update.append(" UPDATE t_shop ");
		update.append(" SET ");
		update.append(	" password = DEFAULT, ");
		update.append(	" idcard = DEFAULT, ");
		update.append(	" sort_id = DEFAULT, ");
		update.append(	" shop_keeper = DEFAULT, ");
		update.append(	" service_tel = DEFAULT, ");
		update.append(	" service_begin_time = DEFAULT, ");
		update.append(	" service_end_time = DEFAULT, ");
		update.append(	" mobile = DEFAULT, ");
		update.append(	" qq = DEFAULT, ");
		update.append(	" we_chat = DEFAULT, ");
		update.append(	" pay_password = DEFAULT, ");
		update.append(	" email = DEFAULT, ");
		update.append(	" logo = DEFAULT, ");
		update.append(	" logo_org = DEFAULT, ");
		update.append(	" status = DEFAULT, ");
		update.append(	" forbidden_status = DEFAULT, ");
		update.append(	" introduction = DEFAULT, ");
		update.append(	" description = DEFAULT, ");
		update.append(	" update_time = NOW(), ");
		update.append(	" province_code = DEFAULT, ");
		update.append(	" city_code = DEFAULT, ");
		update.append(	" area_code = DEFAULT, ");
		update.append(	" zip = DEFAULT, ");
		update.append(	" address = DEFAULT, ");
		update.append(	" is_reward = DEFAULT, ");
		update.append(	" renew_type = DEFAULT, ");
		update.append(	" acvtivate_time = DEFAULT, ");
		update.append(	" expire_date = DEFAULT, ");
		update.append(	" is_recommend = DEFAULT, ");
		update.append(	" open_channel = DEFAULT, ");
		update.append(	" is_deposit = DEFAULT, ");
		update.append(	" is_return_certification = DEFAULT, ");
		update.append(	" is_communication = DEFAULT, ");
		update.append(	" is_express_services = DEFAULT, ");
		update.append(	" is_traffic_safety = DEFAULT, ");
		update.append(	" is_responsibility = DEFAULT, ");
		update.append(	" is_peishi = DEFAULT, ");
		update.append(	" is_shop_cerification = DEFAULT, ");
		update.append(	" good_rate = DEFAULT, ");
		update.append(	" fav_count = DEFAULT ");
		update.append(" WHERE id = ? ");
		Db.update(update.toString(), shopId);
		// 删除店铺实名制数据
		String deleteSql = "DELETE FROM t_shop_certification_record WHERE shop_id = ?";
		Db.update(deleteSql, shopId);
	}
	
}
