package com.buy.plugin.event.pos.supplier;

import java.util.List;

import org.apache.log4j.Logger;

import com.buy.common.JsonMessage;
import com.buy.service.pos.push.PushPosProduct;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class PushPosSupplierListener implements ApplicationListener<PushPosSupplierEvent> {

	Logger L = Logger.getLogger(PushPosSupplierListener.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(PushPosSupplierEvent event) {
		
		/*List<String> idList = (List<String>) event.getSource();
		
		if (StringUtil.isNull(idList))
			return;
		
		String idStr = StringUtil.listToStringForSql(",", idList);
		
		StringBuffer sql = new StringBuffer(" SELECT ");
		sql.append(" s.id supplierId, ");
		sql.append(" s.no supplierNo, ");
		sql.append(" s.name supplierName, ");
		sql.append(" s.qq, ");
		sql.append(" s.update_time updateTime, ");
		sql.append(" a1.name provName, ");
		sql.append(" a2.name cityName, ");
		sql.append(" a3.name areaName, ");
		sql.append(" s.address, ");
		sql.append(" s.contact, ");
		sql.append(" s.mobile, ");
		sql.append(" s.tel, ");
		sql.append(" s.email, ");
		sql.append(" s.status ");
		sql.append(" FROM t_supplier s ");
		sql.append(" LEFT JOIN t_address a1 ON s.province_code = a1.code ");
		sql.append(" LEFT JOIN t_address a2 ON s.city_code = a2.code ");
		sql.append(" LEFT JOIN t_address a3 ON s.area_code = a3.code ");
		sql.append(" WHERE s.id IN (" + idStr + ") ");
		
		List<Record> supplierList = Db.find(sql.toString());
		if (StringUtil.isNull(supplierList))
			return;
		
		for (Record s : supplierList) {
			String realAdress = "";
			String provName = s.getStr("provName");
			String cityName = s.getStr("cityName");
			String areaName = s.getStr("areaName");
			String address = s.getStr("address");
			
			if (StringUtil.notBlank(provName))
				realAdress += provName;
			if (StringUtil.notBlank(cityName))
				realAdress += cityName;
			if (StringUtil.notBlank(areaName))
				realAdress += areaName;
			if (StringUtil.notBlank(address))
				realAdress += address;
			
			s.set("address", realAdress).remove("provName").remove("cityName").remove("areaName");
		}
		
		String json = PushPos.HandleToJson(supplierList);
		JsonMessage jsonMessage = PushPos.getResult("updateSupplier", json);
		L.info("供货商修改推送POS状态：" + jsonMessage.getStatus());
		L.info("供货商修改推送POS数据：" + jsonMessage.getData());
		L.info("供货商修改推送POS消息：" + jsonMessage.getMsg());*/
	}

}
