package com.buy.model.news;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - 资讯
 */
public class News extends Model<News> {
	
	private static final long serialVersionUID = 1L;
	public static final News dao = new News();
	
	/** 资讯状态 - 未发布 */
	public static final int STATUS_UNRELEASE = 0;
	/** 资讯状态 - 经发布 */
	public static final int STATUS_RELEASE = 1;
	
	/**
	 * 根据唯一编码获取资讯内容
	 * @return	资讯内容	
	 * @author Sylveon
	 */
	public String getContent(String code) {
		return Db.queryStr("SELECT content FROM t_news WHERE code = ?", code);
	}
	
}
