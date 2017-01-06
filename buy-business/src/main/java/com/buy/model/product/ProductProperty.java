package com.buy.model.product;

import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ProductProperty extends Model<ProductProperty>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1090017935039394955L;
	/**
	 * 状态：启用
	 */
	public static int STATUS_ENABLE = 1;
	/**
	 * 状态：禁用
	 */
	public static int STATUS_DIABLE = 0;
	/**
	 * 状态：已删除
	 */
	public static int STATUS_DELETE = 2;
	/**
	 * 填写方式：手动
	 */
	public static int INPUT_FUNCTION_MANUAL = 1;
	/**
	 * 填写方式：单选
	 */
	public static int INPUT_FUNCTION_RADIO = 2;
	/**
	 * 填写方式：多选
	 */
	public static int INPUT_FUNCTION_CKECK = 3;
	
	public static final ProductProperty dao = new ProductProperty();
	

	/**
	 * 商品详情--销售属性--用于展示的属性列表
	 * @param productId
	 * @return
	 */
	public List<Record> getSellProperties(Integer productId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("   c.propertyName, ");
		sql.append("   c.valueId, ");
		sql.append("   c.`value`, ");
		sql.append("   c.imgPath ");
		sql.append(" FROM t_product a ");
		sql.append(" inner JOIN t_pro_sku b ON b.product_id = a.id ");
		sql.append(" inner JOIN v_com_sku_property c ON b.`code` = c.skuCode ");
		sql.append(" WHERE a.id = ? ");
		sql.append(" ORDER BY c.propertyName ");
		 
		List<Record> list = Db.find(sql.toString(), productId);
		//返回列表
		List<Record> returnList = new ArrayList<Record>();
		Record record = new Record();//单个属性记录
		List<Record> curSkuMessage = new ArrayList<Record>();//单个属性的所有属性值记录列表
		for(int i=0; i < list.size(); i++){
			if(i == 0){
				record.set("keyName", list.get(i).get("propertyName"));//设置属性名称
				Record curRecord = new Record();
				curRecord.set("valueName", list.get(i).get("value"));
				curRecord.set("valueId", list.get(i).get("valueId"));
				curSkuMessage.add(curRecord);
			}else{
				String curPropertyName = list.get(i).get("propertyName").toString();
				String prePropertyName = list.get(i-1).get("propertyName").toString();
				//同一个属性
				if(curPropertyName.equals(prePropertyName)){
					String curValueId = list.get(i).get("valueId").toString();
					boolean notSet = true;
					for(Record oldRecord : curSkuMessage){
						if(curValueId.equals(oldRecord.get("valueId").toString())){
							notSet = false;
							break;
						}
					}
					//过滤相同的TODO
					if(notSet){
						Record curRecord = new Record();
						curRecord.set("valueName", list.get(i).get("value"));
						curRecord.set("valueId", curValueId);
						curSkuMessage.add(curRecord);
					}
					
				}else{//不是同一个属性
					record.set("valueList", curSkuMessage);
					returnList.add(record);
					//清空
					record = new Record();
					curSkuMessage = new ArrayList<Record>();
					//添加当前记录
					record.set("keyName", curPropertyName);//设置属性名称
					Record curRecord = new Record();
					curRecord.set("valueName", list.get(i).get("value"));
					curRecord.set("valueId", list.get(i).get("valueId"));
					curSkuMessage.add(curRecord);
				}
			}
			//当前为最后一条记录了
			if(i + 1 == list.size()){
				record.set("valueList", curSkuMessage);
				returnList.add(record);
			}
		}
		return returnList;
	}
	
	/**
	 * 根据商品id获取销售属性和值
	 * @param ret
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public List<Record> findSellValueByProductId(Integer productId){
		String sellPropertySql = "select DISTINCT propertyId, propertyName ,isImage from v_com_property where  isSell = ? and productId = ?";
		List<Record> sellProproties = Db.find(sellPropertySql, BaseConstants.YES, productId);
		List<Long> vals = SkuValueMap.dao.getSkuPropertyValueIdByProId(productId);
		for (Record r : sellProproties) {
			StringBuffer sellValueSql = new StringBuffer();
			sellValueSql.append(" SELECT ");
			sellValueSql.append("  valueId, ");
			sellValueSql.append("  value, ");
			sellValueSql.append("  smallPath  , ");
			sellValueSql.append("  midPath,  ");
			sellValueSql.append("  imgPath  ");
			sellValueSql.append(" FROM ");
			sellValueSql.append("  v_com_property ");
			sellValueSql.append(" WHERE ");
			sellValueSql.append("  isSell = ? ");
			sellValueSql.append(" AND propertyId = ? ");
			sellValueSql.append(" AND productId = ? ");
			if (vals.size() > 0) {
				sellValueSql.append(" AND valueId IN(");
				for (int i = 0; i < vals.size(); i++) {
					if (i < vals.size() - 1) {
						sellValueSql.append(vals.get(i) + ",");
					} else {
						sellValueSql.append(vals.get(i) + ")");
					}
				}
			}
			List<Record> values = Db.find(sellValueSql.toString(), BaseConstants.YES, r.getInt("propertyId"), productId);
			r.set("values", values);
		}
		return sellProproties;
	}
}
