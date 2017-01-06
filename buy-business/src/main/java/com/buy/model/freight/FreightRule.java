package com.buy.model.freight;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class FreightRule extends Model<FreightRule>{
	private static final long serialVersionUID = -5178074236807749080L;
	
	/**
	 * 免邮规则-不免邮
	 */
	public static final int TYPE_NOT = 1;
	
	/**
	 * 免邮规则-满X元免邮
	 */
	public static final int TYPE_AMOUNT  = 2;
	
	/**
	 * 免邮规则-满X件免邮
	 */
	public static final int TYPE_NUMBER = 3;
	
	public static final FreightRule dao = new FreightRule();
	
	/**
	 * 根据运费模板Id删除规则.
	 * 
	 * @param templateId
	 * 
	 * @author Chengyb
	 */
	public void deleteByTemplateId(Integer templateId) {
		String sql = "DELETE FROM t_freight_rule WHERE template_id=?";
		Db.update(sql, templateId);
	}
	
}