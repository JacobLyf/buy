package com.buy.model.feedback;

import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class Feedback extends Model<Feedback> {

	private static final long serialVersionUID = 1L;
	public static final Feedback dao = new Feedback();
	
	/**
	 * 目标类型 会员 
	 */
	public static final int TYPE_USER = 1;
	/**
	 * 目标类型 店铺 
	 */
	public static final int TYPE_SHOP = 2;
	/**
	 * 目标类型 代理商 
	 */
	public static final int TYPE_AGENT = 3;
	/**
	 * 目标类型  供货商 
	 */
	public static final int TYPE_SUPPLIER= 4;

	/**
	 * 回复状态：未回复
	 */
	public static final int STATUS_WAIT = 0;
	/**
	 * 回复状态：已回复
	 */
	public static final int STATUS_PASS = 1;
	
	/**
	 * PC前端--四个角色获取意见反馈列表 
	 * @param targetId
	 * @param targetType
	 * @param page
	 * @return
	 * @author chenhg
	 * 2016年2月20日 上午11:50:59
	 */
	public Page<Record> getFeedbackPage(String targetId, Integer targetType, Page<Object> page){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		
		select.append(" SELECT ");
		select.append(" a.id, ");
		select.append(" DATE_FORMAT(a.feedback_time,'%Y-%m-%d %H:%i') feedbackTime, ");
		select.append(" a.title, ");
		select.append(" a.`status` ");
		
		where.append(" FROM t_feedback a ");
		where.append(" WHERE a.target_id = ? ");
		where.append(" AND a.target_type = ? ");
		where.append(" ORDER BY a.feedback_time desc ");
		
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(), targetId, targetType);
	}

	/**
	 * PC前端--四个角色保存意见反馈
	 * @param userId
	 * @param userName
	 * @param feedbackTitle
	 * @param feedbackContent
	 * @author chenhg
	 * 2016年2月20日 下午1:35:09
	 */
	public boolean saveMessage(String targetId, Integer targetType, String targetNo,
			String feedbackTitle, String feedbackContent) {
		Feedback feedback = new Feedback();
		feedback
			.set("target_id", targetId)
			.set("target_no", targetNo)
			.set("target_type", targetType)
			.set("title", feedbackTitle)
			.set("content", feedbackContent)
			.set("status", STATUS_WAIT)
			.set("feedback_time", new Date())
			.save();
		return true;
	}
	
	/**
	 * PC前端--四个角色 获取详情信息
	 * @param id
	 * @return
	 * @author chenhg
	 * 2016年2月20日 下午3:03:28
	 */
	public Record getContent(String id){
		String sql = " SELECT title, content from t_feedback  WHERE id = ? ";
		return Db.findFirst(sql, id);
	}
	
	/**
	 * PC前端--四个角色 获取回复信息
	 * @param id
	 * @return
	 * @author chenhg
	 * 2016年2月20日 下午3:03:28
	 */
	public Record getReplyContent(String id){
		String sql = " SELECT DATE_FORMAT(reply_time,'%Y-%m-%d %H:%i') replyTime, reply_content replyContent from t_feedback  WHERE id = ? ";
		return Db.findFirst(sql, id);
	}
}
