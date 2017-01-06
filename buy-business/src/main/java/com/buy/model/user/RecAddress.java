package com.buy.model.user;

import java.util.Date;
import java.util.List;

import com.buy.common.Ret;
import com.buy.model.address.Address;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * Model - 会员收货地址
 * @author Sylveon
 *
 */
public class RecAddress extends Model<RecAddress> {
	
	/**
	 * 收货地址 - 非默认标识
	 */
	public final static int IS_NOT_DEFAULT = 0;
	/**
	 * 收货地址 - 默认标识
	 */
	public final static int IS_DEFAULT = 1;

	private static final long serialVersionUID = 1L;
	
	public final static RecAddress dao = new RecAddress();
	
	/**
	 * 添加收货地址.
	 * 
	 * @param ret
	 * @author Chengyb
	 */
	public void addRecAddress(Ret ret) {
		RecAddress dao = new RecAddress();
		dao.set("contact", ret.get("contact"))
			  .set("user_id", ret.get("userId"))
		      .set("province_code", ret.get("provinceCode"))
		      .set("city_code", ret.get("cityCode"))
		      .set("area_code", ret.get("areaCode"))
		      .set("address", ret.get("address"))
		      .set("zip", ret.get("zip"))
		      .set("mobile", ret.get("mobile"))
		      .set("tel", ret.get("tel"))
		      .set("is_default",ret.get("isDefault"))
		      .set("create_time", new Date())
			  .set("update_time", new Date())
			  .save();
	}
	
	/**
	 * 取消用户的默认收货地址.
	 * 
	 * @param userId
	 *            用户Id
	 * @author Chengyb
	 */
	public void cancelDefaultAddress(String userId) {
		String sql = "UPDATE t_reciever_address SET is_default = 0 WHERE user_id = ? AND is_default = 1";
		Db.update(sql, userId);
	}
	
	/**
	 * 设置用户的默认收货地址.
	 * 
	 * @param userId
	 *            用户Id
	 * @author addressId
	 *            收货地址Id
	 * @author Chengyb
	 */
	public void setDefaultAddress(String userId, Integer addressId) {
		String sql = "UPDATE t_reciever_address SET is_default = 1 WHERE user_id = ? AND id = ?";
		Db.update(sql, userId, addressId);
	}

	/**
	 * 更新收货地址.
	 * 
	 * @param ret
	 * @author Chengyb
	 */
	public void updateRecAddress(Ret ret) {
		RecAddress dao = new RecAddress();
		dao.set("id", ret.get("id"))
		      .set("contact", ret.get("contact"))
		      .set("user_id", ret.get("userId"))
              .set("province_code", ret.get("provinceCode"))
              .set("city_code", ret.get("cityCode"))
              .set("area_code", ret.get("areaCode"))
              .set("address", ret.get("address"))
              .set("zip", ret.get("zip"))
              .set("mobile", ret.get("mobile"))
              .set("tel", ret.get("tel"))
              .set("is_default",ret.get("isDefault"))
		      .set("update_time", new Date())
		      .update();
	}
	
	/**
	 * 获取默认收货地址的ID
	 * param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2015年12月10日下午3:41:37
	 */
	public Integer getIdOfDefault(String userId){
		String sql = "select id from t_reciever_address where is_default = ? and user_id = ? ";
		return Db.queryInt(sql,IS_DEFAULT,userId);
	}

	/**
	 * 获取默认收货地址的ID
	 * 确认订单时:若用户没有收货地址,在新增第一个时,且没有设置默认时,取第一个地址
	 * param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2015年12月10日下午3:41:37
	 */
	public Integer getIdOfNewAddr(String userId){
		String sql = "select id from t_reciever_address where user_id = ? LIMIT 1 ";
		return Db.queryInt(sql,userId);
	}

	/**
	 * 用户是否已经拥有默认收货地址.
	 * 
	 * @param userId
	 *            用户Id.
	 * @author Chengyb
	 */
	public Boolean hasDefaultAddress(String userId) {
		String sql = "select count(1) from t_reciever_address where is_default = ? and user_id = ? ";
		return Db.queryLong(sql, IS_DEFAULT, userId).intValue() > 0 ? true : false;
	}
	
	/**
	 * 用户是否已经拥有该收货地址.(此地址是否为用户的)
	 * @param userId 用户Id.
	 * @param addressId 收货地址Id
	 */
	public Boolean isHadAddress(Integer addressId,String userId) {
		String sql = "select count(1) from t_reciever_address where id = ? and user_id = ? ";
		return Db.queryLong(sql, addressId, userId).intValue() > 0 ? true : false;
	}
	
	/**
	 * 获取用户的收货地址列表.
	 * 
	 * @param userId
	 * 
	 * @author Chengyb
	 */
	public List<Record> list(String userId) {
		String sql = "SELECT id, is_default, contact, mobile, tel, zip, province_code, city_code, area_code, address FROM t_reciever_address s WHERE s.user_id = ? ORDER BY s.is_default DESC, s.create_time DESC";
		return changeAddressCodeToName(Db.find(sql, userId));
	}
	
	/**
	 * 将省市区编码转为名称
	 * 
	 * @param list
	 * @return
	 * @author Chengyb
	 */
	public List<Record> changeAddressCodeToName(List<Record> list){
		//将省市区编码转为名称.
		for (int i = 0, size = list.size(); i < size; i++) {
			Record recAddress = list.get(i);
			
			List<Record> nameList = Address.dao.transformCode(recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
		    for (int j = 0, size2 = nameList.size(); j < size2; j++) {
				Integer code = nameList.get(j).getInt("code");
				if(recAddress.getInt("province_code").equals(code)) {
					recAddress.set("province_name", nameList.get(j).getStr("name"));
					continue;
				}
                if(recAddress.getInt("city_code").equals(code)) {
                	recAddress.set("city_name", nameList.get(j).getStr("name"));
                	continue;
				}
                if(recAddress.getInt("area_code").equals(code)) {
                	recAddress.set("area_name", nameList.get(j).getStr("name"));
                	continue;
				}
			}
		}
		return list;
	}
	
	/**
	 * 获取会员默认收货地址
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年3月22日下午7:46:13
	 */
	public Record getDefaultRecAddress(String userId){
		String sql = "select * from t_reciever_address where is_default = ? and user_id = ? ";
		return changeAddressCodeToName(Db.findFirst(sql,IS_DEFAULT,userId));
	}
	
	/**
	 * 将省市区编码转为名称
	 */
	public Record changeAddressCodeToName(Record recAddress){
		List<Record> nameList = Address.dao.transformCode(recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
	    for (int j = 0, size2 = nameList.size(); j < size2; j++) {
			Integer code = nameList.get(j).getInt("code");
			if(recAddress.getInt("province_code").equals(code)) {
				recAddress.set("province_name", nameList.get(j).getStr("name"));
				continue;
			}
            if(recAddress.getInt("city_code").equals(code)) {
            	recAddress.set("city_name", nameList.get(j).getStr("name"));
            	continue;
			}
            if(recAddress.getInt("area_code").equals(code)) {
            	recAddress.set("area_name", nameList.get(j).getStr("name"));
            	continue;
			}
		}
		return recAddress;
	}
}
