package com.buy.model.error;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;

/**
 * 异常信息日志
 * @author huangzq
 *
 */
public class ErrorLog extends Model<ErrorLog> {

	private static final long serialVersionUID = 1L;
	
	public static final ErrorLog dao = new ErrorLog();
	/**
	 * 状态;未处理
	 */
	public static final int STATUS_UNSOLVED = 0;
	/**
	 * 状态;已处理
	 */
	public static final int STATUS_SOLVED = 1;

	/**
	 * 类型：MQ异常
	 */
	public static final int TYPE_MQ = 1;
	/**
	 * 添加异常信息
	 * @param type 类型
	 * @param keyContent 关键内容
	 * @param errorMessage 异常信息
	 * @param remark 备注
	 */
	public void add(int type,String keyContent,String errorMessage,String remark){
		ErrorLog errorLog = new ErrorLog();
		errorLog.set("type", type);
		errorLog.set("key_content", keyContent);
		errorLog.set("error_message", errorMessage);
		errorLog.set("remark", remark);
		errorLog.set("create_time", new Date());
		errorLog.save();
		
	}

	
}
