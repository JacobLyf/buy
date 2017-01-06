package com.buy.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.buy.numOprate.MathUtil;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class SysParam  extends Model<SysParam>{	
	
	/**
	 * 系统参数类型-开发参数
	 */
	public final static int TYPE_DEVELOP = 1;
	/**
	 * 系统参数类型-配置参数
	 */
	public final static int TYPE_CONFIGURE = 2;
	
	/**
	 * 系统参数风格-单值
	 */
	public final static int STYLE_SINGLE = 1;
	/**
	 * 系统参数风格-多值
	 */
	public final static int STYLE_MULTI = 2;
	
	
	
	private static final long serialVersionUID = 1L;
	public static final SysParam dao = new SysParam();
	/**
	 * 根据code获取值
	 * @param code
	 * @return
	 * @author huangzq
	 */
	public String getStrByCode(String code){
		SysParam sysParam = dao.findFirst( "SELECT s.`value` FROM t_sys_param s where s.`code` = ? ", code);
		if(null != sysParam){
			return sysParam.getStr("value");
		}
		return null;
		
	}
	/**
	 * 根据code获取值
	 * @param code
	 * @return
	 * @author huangzq
	 */
	public Integer getIntByCode(String code){
		String str = getStrByCode(code);
		return Integer.parseInt(str);
		
	}
	/**
	 * 根据code获取值
	 * @param code
	 * @return
	 * @author huangzq
	 */
	public BigDecimal getBigDecimalByCode(String code){
		String str = getStrByCode(code);
		return new BigDecimal(str);
			
	}
	/**
	 * 获取参数名称和参数值
	 * @param code	参数CODE
	 * @return		参数数据集合
	 * @author 		Sylveon
	 */
	public List<Record> findByCode(String code){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT s.id, s.value, s.name, s.remark FROM t_sys_param s WHERE s.parent_id = (");
		sql.append("	SELECT t.id FROM t_sys_param t WHERE t.code = ?");
		sql.append(" )");
		return Db.find(sql.toString(), code);
	}
	
	/**
	 * 是否存在多值
	 */
	public boolean existChildren(String parentCode, String childCode) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT COUNT(1) FROM t_sys_param WHERE parent_id = ( ");
		sql.append(		" SELECT id FROM t_sys_param WHERE code = ? ");
		sql.append(" ) ");
		sql.append(" AND value = ? ");
		long flag = Db.queryLong(sql.toString(), parentCode, childCode);
		return flag > 0 ? true : false; 
	}

	/**
	 * 获取参数值
	 * @param code
	 * @return
	 * @author Sylveon
	 */
	public List<String> findValueByCode(String code) {
		// 获取参数值
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT s.value FROM t_sys_param s WHERE s.parent_id = (");
		sql.append("	SELECT t.id FROM t_sys_param t WHERE t.code = ?");
		sql.append(" )");
		List<Record> list = Db.find( sql.toString(), code);
		// 把参数值转化Stirng集合
		List<String> result = new ArrayList<String>();
		for (Record r : list)
			result.add( r.getStr("value"));
		return result;
	}
	
	/**
	 * 获取参名称
	 * @param code
	 * @return
	 * @author Sylveon
	 */
	public List<String> findNameByCode(String code) {
		// 获取参数值
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT s.name FROM t_sys_param s WHERE s.parent_id = (");
		sql.append("	SELECT t.id FROM t_sys_param t WHERE t.code = ?");
		sql.append(" )");
		List<Record> list = Db.find(sql.toString(), code);
		// 把参数值转化Stirng集合
		List<String> result = new ArrayList<String>();
		for (Record r : list)
			result.add( r.getStr("name"));
		return result;
	}
	
	/**
	 * 根据系统参数值
	 * @param code
	 * @param value
	 * @author 		Sylveon
	 */
	public void updateValue(String code, String value) {
		SysParam sysParam = SysParam.dao.findFirst("SELECT id FROM t_sys_param WHERE code = ?", code);
		sysParam.set("value", value).update();
	}
	
	/**
	 * 现金提取--获取手续费计算参数
	 */
	public Record getSysParam(){
		BigDecimal rate = SysParam.dao.getBigDecimalByCode("draw_charge_rate");
		BigDecimal maxDraw = SysParam.dao.getBigDecimalByCode("draw_charge_max");
		BigDecimal minDraw = SysParam.dao.getBigDecimalByCode("draw_charge_min");
		Record record = new Record();
		record.set("rate", rate);
		record.set("maxDraw", maxDraw);
		record.set("minDraw", minDraw);
		return record;
	}
	
	
	/**
	 * 计算提现手续费
	 * @param applyCash
	 * @return
	 * @author chenhg
	 * 2016年7月25日 下午2:57:35
	 */
	public Record calculateFees(String applyCash){
		applyCash = StringUtil.cutNullBlank(applyCash);
		BigDecimal cash = new BigDecimal(applyCash);
		BigDecimal drawRate = SysParam.dao.getBigDecimalByCode("draw_charge_rate");
		BigDecimal drawMax = SysParam.dao.getBigDecimalByCode("draw_charge_max");
		BigDecimal drawMin = SysParam.dao.getBigDecimalByCode("draw_charge_min");
		//手续费
		BigDecimal fee = cash.multiply(drawRate);
		
		fee = MathUtil.ceilByScale(fee, 2);//保留两位小数，第三位大于0 向上进1
		if(fee.compareTo(drawMin)<0){
			fee = drawMin;
		}else if(fee.compareTo(drawMax)>0){
			fee = drawMax;
		}
		//实发金额
		BigDecimal actualCash = cash.subtract(fee);
		DecimalFormat df = new DecimalFormat("0.00"); 
		
		Record result = new Record();
		result.set("fees", df.format(fee));//手续费
		result.set("actualCash", df.format(actualCash));//实发金额
		
		return result;
	}
}
