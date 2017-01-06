package com.buy.model.address;

import java.util.ArrayList;
import java.util.List;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class Address extends Model<Address>{
	
	private static final long serialVersionUID = 1L;
	
	public static final Address dao = new Address();
	
	/**
	 * 国家 - 中国
	 */
	public static final int CODE_COUNTRY_CN = 0;
	
	/**
	 * 省
	 */
	public static final int PROVINCE = 1;
	/**
	 * 市
	 */
	public static final int CITY = 2;
	/**
	 * 区
	 */
	public static final int AREA = 3;
	/**
	 * 街道
	 */
	public static final int STREET = 4;
	
	/**
	 * 获取不同级别的区域列表
	 * @param level(1：省,2：市，3：区，4：街道)
	 * @param parentCode
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月29日上午11:21:39
	 */
	public List<Record> findListByLevel(int parentCode,int level){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	t.id, ");
		sql.append("	t.`code`, ");
		sql.append("	t.`name` ");
		sql.append("FROM ");
		sql.append("	t_address t ");
		sql.append("WHERE ");
		sql.append("	t.parent_code = ? ");
		sql.append("AND t.`level` = ? ");
		sql.append("ORDER BY ");
		sql.append("	t.`code` ");
		return Db.find(sql.toString(), parentCode,level);
	}
	
	/**
	 * 获取省、市、区、街道下拉框列表（根据省级编码获取市级列表，根据市级编码获取区级列表）
	 * PC后台专用（返回编号跟名称处理成value跟label，后台BJUI特殊要求）
	 * @param parentCode  父级编码，为0时查询省级列表
	 * @return
	 * @author Jacob
	 * 2015年8月3日下午3:40:19
	 */
	public List<Record> searchListByParentCode(Integer parentCode){
		List<Record> list = new ArrayList<Record>();
		Record record = new Record();
		record.set("value", "");
		record.set("label", "请选择");
		list.add(record);
		if(null == parentCode) 
			return list;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	t.`code` value, ");
		sql.append("	t.`name` label ");
		sql.append("FROM ");
		sql.append("	t_address t ");
		sql.append("WHERE ");
		sql.append("	t.parent_code = ? ");
		sql.append("ORDER BY ");
		sql.append("	t.`code` ");
		list.addAll(Db.find(sql.toString(), parentCode));
		return list;
	}
	
	/**
	 * 获取省、市、区、街道下拉框列表（根据省级编码获取市级列表，根据市级编码获取区级列表）
	 * PC前台专用
	 * @param parentCode
	 * @return
	 * @author Jacob
	 * 2015年12月3日上午10:06:57
	 */
	public List<Record> searchListByParentCode4Web(Integer parentCode){
		List<Record> list = new ArrayList<Record>();
		if(null == parentCode) 
			return list;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	t.`code`, ");
		sql.append("	t.`name` ");
		sql.append("FROM ");
		sql.append("	t_address t ");
		sql.append("WHERE ");
		sql.append("	t.parent_code = ? ");
		sql.append("ORDER BY ");
		sql.append("	t.`code` ");
		list = Db.find(sql.toString(), parentCode);
		return list;
	}
	
	/**
	 * 根据名称获取相应的省市区对象
	 * @param name 省\市\区名称
	 * @return
	 * @author Jacob
	 * 2015年7月31日下午3:31:33
	 */
	public Record getProvCityAreaStreetByName(String name){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	* ");
		sql.append("FROM ");
		sql.append("	t_address ");
		sql.append("WHERE ");
		sql.append(" name = ? ");
		return Db.findFirst(sql.toString(),name);
	}

	/**
	 * 根据名称获取相应的省市区 code
	 * @param name 省\市\区名称
	 * @return
	 * @author Jacob
	 * 2015年7月31日下午3:31:33
	 */
	public int getCodeByName(String name){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	code ");
		sql.append("FROM ");
		sql.append("	t_address ");
		sql.append("WHERE ");
		sql.append(" name = ? ");
		return Db.queryInt(sql.toString(),name);
	}

	/**
	 * 根据编号获取相应的省市区对象
	 * @param name 省\市\区名称
	 * @return
	 * @author Jacob
	 * 2015年7月31日下午3:31:33
	 */
	public Record getProvCityAreaStreetByCode(int code){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	* ");
		sql.append("FROM ");
		sql.append("	t_address ");
		sql.append("WHERE ");
		sql.append(" code = ? ");
		return Db.findFirst(sql.toString(),code);
	}
	
	/**
	 * 判断地址编码是否存在
	 * @param code
	 * @return
	 * boolean	返回true表示编码存在
	 * @author wangy
	 * @date 2015年10月12日 下午4:48:49
	 */
	public boolean codeExist(int code){
		String sql = "SELECT count(id) count FROM t_address WHERE code=?";
		Long count = Db.findFirst(sql,code).getLong("count");
		return count>0;
	}
	
	/**
	 * 根据省市区编码获取名称.
	 * 
	 * @param provinceCode
	 *            省编码
	 * @param cityCode
	 *            市编码
	 * @param areaCode
	 *            区编码
	 * @return
	 */
	public List<Record> transformCode(Integer provinceCode, Integer cityCode, Integer areaCode) {
		String sql = "SELECT s.code, s.name FROM t_address s WHERE s.code in (?, ?, ?) ORDER BY s.code DESC";
		return Db.find(sql, provinceCode, cityCode, areaCode);
	}
	
	/**
	 * 将省市区编码转为名称
	 * 
	 * @param list
	 * @return
	 * @author Jacob 2015年12月11日上午9:53:03
	 */
	public List<Record> changeAddressCodeToName(List<Record> list) {
		// 将省市区编码转为名称.
		for (int i = 0, size = list.size(); i < size; i++) {
			Record record = list.get(i);

			List<Record> nameList = Address.dao.transformCode(record.getInt("province_code"), record.getInt("city_code"), record.getInt("area_code"));
			for (int j = 0, size2 = nameList.size(); j < size2; j++) {
				Integer code = nameList.get(j).getInt("code");
				if (record.getInt("province_code").equals(code)) {
					record.set("province_name", nameList.get(j).getStr("name"));
					continue;
				}
				if (record.getInt("city_code").equals(code)) {
					record.set("city_name", nameList.get(j).getStr("name"));
					continue;
				}
				if (record.getInt("area_code").equals(code)) {
					record.set("area_name", nameList.get(j).getStr("name"));
					continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * 根据地址代码获取地址名称
	 * @param code
	 * @return
	 * @author Jacob
	 * 2015年11月13日下午8:22:58
	 */
	public String getNameByCode(Integer code){
		String sql = " SELECT s.name FROM t_address s WHERE s.code = ? ";
		return Db.queryStr(sql, code);
	}
}
