package com.buy.model.news;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class NewsType extends Model<NewsType> {
	
	/**
	 * 资讯类型 - e趣学院
	 */
	public static final String CODE_EFUNSCHOOL = "efunSchool";
	/**
	 * 资讯类型 - 帮助中心
	 */
	public static final String CODE_HELP = "help";
	/**
	 * 资讯类型 - 社区
	 */
	public static final String CODE_COMMUNITY = "community";

	private static final long serialVersionUID = 1L;
	public static final NewsType dao = new NewsType();

	/**
	 * 根据父级code查找子资讯类型列表
	 * @param parentCode	父级code
	 * @return				资讯类型列表
	 * @author Sylveon
	 */
	public List<NewsType> findChildrenByParentCode(String parentCode) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" id, `name`");
		sql.append(" FROM t_news_type");
		sql.append(" WHERE parent_id IN (SELECT id FROM t_news_type WHERE CODE = ?)");
		sql.append(" ORDER BY ISNULL(`order`) ASC, `order` ASC");
		return NewsType.dao.find(sql.toString(), parentCode);
	}
	
	/**
	 * 根据资讯类型ID获取名称
	 * @param newsTypeId	资讯类型ID
	 * @return				资讯名称
	 * @author Sylveon
	 */
	public String getNameById(int newsTypeId) {
		return Db.queryStr("SELECT name FROM t_news_type WHERE id = ?", newsTypeId);
	}
	
	/**
	 * 根据资讯类型编号查找ID
	 * @param code		资讯类型编号
	 * @return			资讯类型ID
	 * @author Sylveon
	 */
	public Integer getIdByCode(String code) {
		return Db.queryInt("SELECT id FROM t_news_type WHERE code = ?", code);
	}
	
}
