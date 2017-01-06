package com.buy.model.message;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 信息 和 用户映射
 * @author Sylveon
 */
public class MessageUser extends Model<MessageUser> {

	private static final long serialVersionUID = 1L;
	public static MessageUser dao = new MessageUser();
	
	public void addMessageUser(int msgId, String targetId, int targetType) {
		new MessageUser()
			.set("message_id",		msgId)				// 消息ID
			.set("target_id", 		targetId)			// 目标ID
			.set("target_type", 	targetType)			// 目标类型
			.set("status",			Message.STATUS_UNREAD)
			.set("create_time",		new Date())
			.save();
	}
	
}
