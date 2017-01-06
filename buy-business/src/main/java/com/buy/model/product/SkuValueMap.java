package com.buy.model.product;

import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

public class SkuValueMap extends Model<SkuValueMap> {
	private static final long serialVersionUID = 6158141935755316649L;

	public static final SkuValueMap dao = new SkuValueMap();

	/**
	 * 删除映射
	 * 
	 * @param skuCode
	 * @author huangzq
	 */
	@Before(Tx.class)
	public void delete(String skuCode) {
		Db.update("delete from t_sku_value_map where sku_code = ?", skuCode);
	}

	/**
	 * 添加映射
	 * 
	 * @param skuCode
	 * @param valueId
	 * @author huangzq
	 */
	public void add(String skuCode, long valueId) {
		SkuValueMap map = dao.findById(skuCode, valueId);
		if (map == null) {
			map = new SkuValueMap();
			map.set("sku_code", skuCode);
			map.set("value_id", valueId);
			map.save();
		}
	}

	/**
	 * 清除无效的映射
	 */
	public void clearDisableMap() {
		String sql = "DELETE m.* FROM t_sku_value_map m LEFT JOIN t_pro_sku s on m.sku_code = s.`code` LEFT JOIN t_pro_property_value pv on m.value_id = pv.id where s.`code` is null or pv.id is null";
		Db.update(sql);
	}

	/**
	 * 根据商品id获取属性值ID
	 * @author chenhj
	 */
	public List<Long> getSkuPropertyValueIdByProId(int proId) {
		String sql = "SELECT DISTINCT value_id FROM t_sku_value_map WHERE sku_code IN (SELECT `code` FROM t_pro_sku WHERE product_id = ?);";
		List<Record> lists = Db.find(sql, proId);
		List<Long> vals = new ArrayList<Long>();
		if (!StringUtil.isNull(lists)) {
			for (Record r : lists) {
				vals.add(r.getLong("value_id"));
			}
		}
		return vals;
	}
}
