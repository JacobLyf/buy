package com.buy.model.product.nocargo;

import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.InternetHeaders;

/**
 * 货到通知
 */
public class ProReplenishNote extends Model<ProReplenishNote> {

	private static final long serialVersionUID = 1L;
	public static final ProReplenishNote dao = new ProReplenishNote();
	
	/** 通知状态 - 未通知 **/
	public static final int STATUS_UNNOTE = 0;
	/** 通知状态 - 已通知 **/
	public static final int STATUS_NOTED = 1;
	
	public ProReplenishNote getForLock(int id) {
		return ProReplenishNote.dao.findById("SELECT * FROM t_pro_replenish_note WHERE id = ? FOR UPDATE", id);
	}
	
	/**
	 * 添加会员到货通知.
	 * 
	 * @param nocargoId
	 * @param userId
	 * @param mobile
	 */
	public synchronized void add(Integer nocargoId, String userId, String mobile) {
		ProReplenishNote note = ProReplenishNote.dao.findFirst("SELECT * FROM t_pro_replenish_note WHERE nocargo_id = ? AND user_id = ?", nocargoId, userId);
		if(null == note) {
			note = new ProReplenishNote();
			note.set("nocargo_id", nocargoId);
			note.set("user_id", userId);
			note.set("mobile", mobile);
			note.set("create_time", new Date());
			note.save();
			
			// 订阅用户数+1.
			ProductNoCargo productNoCargo = ProductNoCargo.dao.findById(nocargoId);
			productNoCargo.set("user_count", productNoCargo.getInt("user_count") + 1);
		} else {
			note.set("mobile", mobile);
			note.set("update_time", new Date());
			note.set("submit_times", note.getInt("submit_times") + 1);
			note.update();
		}
	}
	
	/**
	 * 获取需要通知的会员信息.
	 * 
	 * @param nocargoId
	 * @return 
	 */
	public List<ProReplenishNote> findByNocargoId(Integer nocargoId) {
		return ProReplenishNote.dao.find("SELECT * FROM t_pro_replenish_note WHERE nocargo_id = ? AND status = 0", nocargoId);
	}
	
}