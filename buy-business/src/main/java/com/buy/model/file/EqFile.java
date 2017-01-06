package com.buy.model.file;  

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;


public class EqFile extends Model<EqFile> {
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态：启用
	 */
	public static int STATUS_ENABLE = 1;
	/**
	 * 状态：无效
	 */
	public static int STATUS_DISABLE= 0;
	
	public static final EqFile dao = new EqFile();
	/**
	 * 文件有效
	 * @param paths
	 * @author huangzq
	 */
	public void enable(String... paths){
		
		String sql = "update  t_file set status = ? where path in (";
		for(String path : paths){
			sql+= "'"+path+"',";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,STATUS_ENABLE);
	}
	/**
	 * 文件无效
	 * @param paths
	 * @author huangzq
	 */
	public void disable(String... paths){
		
		String sql = "update  t_file set status = ? where path in (";
		for(String path : paths){
			sql+= "'"+path+"',";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,STATUS_DISABLE);
	}
	
	/**
	 * 判断文件是否存在表中
	 * @param path
	 * @return
	 * @author chenhg
	 * 2016年8月9日 下午2:10:59
	 */
	public boolean inTable(String path){
		String sql = "select path from t_file where path = ?";
		Record record = Db.findFirst(sql, path);
		return record == null ? false:true;
	}
	
	
	

}
      