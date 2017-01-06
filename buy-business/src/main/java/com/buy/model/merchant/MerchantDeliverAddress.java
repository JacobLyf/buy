package com.buy.model.merchant;

import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class MerchantDeliverAddress extends Model<MerchantDeliverAddress> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -584807507416047047L;

	public static final MerchantDeliverAddress dao = new MerchantDeliverAddress();

	/**
	 * 更新发货地址
	 * 
	 * @param id
	 *            主键
	 * @param merchantId
	 *            商家ID
	 * @param provinceCode
	 *            省
	 * @param cityCode
	 *            市
	 * @param areaCode
	 *            区
	 * @param address
	 *            详细地址
	 * @return
	 */
	public boolean updateDeliverAddress(Integer id, String merchantId,
			Integer provinceCode, Integer cityCode, Integer areaCode,
			String address) {
		
		return new MerchantDeliverAddress().set("id", id)
				.set("merchant_id", merchantId)
				.set("province_code", provinceCode).set("city_code", cityCode)
				.set("area_code", areaCode).set("address", address)
				.set("update_time", new Date()).update();
	}

	/**
	 * 保存发货地址
	 * 
	 * @param merchantId
	 *            商家ID
	 * @param provinceCode
	 *            省
	 * @param cityCode
	 *            市
	 * @param areaCode
	 *            区
	 * @param address
	 *            详细地址
	 * @return
	 */
	public boolean saveDeliverAddress(String merchantId, Integer merchantType, Integer provinceCode,
			Integer cityCode, Integer areaCode, String address) {
		
		return new MerchantDeliverAddress().set("merchant_id", merchantId)
				.set("province_code", provinceCode).set("city_code", cityCode)
				.set("area_code", areaCode).set("address", address)
				.set("create_time", new Date()).set("update_time", new Date())
				.set("merchant_type", merchantType)
				.save();
	}
	
	/**
	 * 根据商家ID获取发货地址
	 * @param merchantId
	 * 				商家ID
	 * @param merchantType
	 * 				商家类型
	 * @return
	 */
	public MerchantDeliverAddress findByMerchantId(String merchantId, Integer merchantType) {
		String sql = "select * from t_merchant_deliver_address where merchant_id = ? and merchant_type = ?";
		return dao.findFirst(sql, merchantId, merchantType);
	}
	
	/**
	 * 根据商家ID获取发货地址ID
	 * @param merchantId
	 * @return
	 */
	public Integer getIdByMerchantId(String merchantId) {
		return Db.queryInt("select id from t_merchant_deliver_address where merchant_id = ?",  merchantId);
	}
	
	/**
	 * 根据商家ID获取发货地址详情
	 * @param merchantId
	 * @param merchantType
	 * @return
	 */
	public Record findDetailByMerchantId(String merchantId, Integer merchantType) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select prov.`name` as province, city.`name` as city,"
				+ " area.`name` as area, m.address as address");
		sql.append(" from t_merchant_deliver_address m");
		sql.append(" LEFT JOIN t_address prov ON m.province_code = prov.`code`");
		sql.append(" LEFT JOIN t_address city ON m.city_code = city.`code`");
		sql.append(" LEFT JOIN t_address area ON m.area_code = area.`code`");
		sql.append(" where m.merchant_id = ? AND m.merchant_type = ?");
		return Db.findFirst(sql.toString(), merchantId, merchantType);
	}
}
