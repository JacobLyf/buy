package com.buy.model.notice;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class NoticeType extends Model<NoticeType> {
	
	private static final long serialVersionUID = 1L;
	public static final NoticeType dao = new NoticeType();
	
	/**
	 * 查找所有公告类别
	 * @return
	 * @author Sylveon
	 */
	public List<Record> findAll() {
		String sql = "SELECT id, name FROM t_notice_type ORDER BY sort_num ASC, create_time DESC";
		return Db.find(sql);
	}

}
