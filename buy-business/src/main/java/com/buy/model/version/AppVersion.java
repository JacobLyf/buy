package com.buy.model.version;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * APP 版本
 */
public class AppVersion extends Model<AppVersion>
{
	private static final long serialVersionUID = 1L;
	
	public static final AppVersion dao = new AppVersion();

	/** 版本类型 - android **/
	public static final int TYPE_ANDROID = 1;
	/** 版本类型 - ios **/
	public static final int TYPE_IOS = 2;
	
	/** 审核状态 - 等待审核 **/
	public static final int AUDIT_STATUS_WAIT = 1;
	/** 审核状态 - 审核成功 **/
	public static final int AUDIT_STATUS_SUCCESS = 0;
	
	/**
	 * 版本1
	 */
	public static final String VERSION_1 = "V1";
	
	/**
	 * 获取最新版本的安卓下载链接
	 * @param appType
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年6月28日上午10:03:42
	 */
	public Record getAndroidInfo(String otherViewPath)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(		" version_no versionNo, ");
		sql.append(		" content, ");
		sql.append(		" link, ");
		sql.append(		" audit_status auditStatus,");
		sql.append(		" publish_time publishTime ");
		sql.append(" FROM t_app_version ");
		sql.append(" WHERE 1 = 1 ");
		sql.append(" AND app_type = ? ");
		sql.append(" AND audit_status = ? ");
		sql.append(" ORDER BY CAST(REPLACE(version_no, '.', '') AS SIGNED) DESC ");
		Record result = Db.findFirst(sql.toString(),AppVersion.TYPE_ANDROID,AppVersion.AUDIT_STATUS_SUCCESS);
		
		// Android情况
		String link = result.getStr("link");
		if (StringUtil.notNull(link))
			result.set("link",otherViewPath + link);
		
		return result;
	}
	
}
