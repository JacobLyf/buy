package com.buy.model.product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.img.Image;
import com.buy.string.StringUtil;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ProductPropertyValue extends Model<ProductPropertyValue>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final ProductPropertyValue dao = new ProductPropertyValue();
	/**
	 * 通过商品Id和属性id获取非销售属性值
	 * @param productId
	 * @param propertyId
	 * @author huangzq
	 */
	public ProductPropertyValue getNotSellValue(int productId,int propertyId){
		return dao.findFirst("select id,value from t_pro_property_value where product_id = ? and property_id = ?  ", productId,propertyId);
	}
	
	/**
	 * 通过商品Id和属性id获取非销售属性值
	 * @param productId
	 * @param propertyId
	 * @author huangzq
	 */
	public ProductPropertyValue getSellValue(int productId,int propertyId,String value){
		return dao.findFirst("select id,value from t_pro_property_value where product_id = ? and property_id = ? and value = ?  ", productId,propertyId,value);
	}
	
	/**
	 * 清理商品属性值
	 * @param productId
	 * @param noDeleteIds
	 * @author huangzq
	 */
	public void clearAllValues(int productId,List<Long> noDeleteIds ){
		String sql = "delete from t_pro_property_value where product_id = ? and id not in(";
		for(Long id:noDeleteIds){
			sql+="'"+id+"',";
		}
		sql = sql.substring(0,sql.length()-1);
		sql+=")";
		Db.update(sql,productId);
	}
	/**
	 * 清理商品普通属性值
	 * @param productId
	 * @param noDeleteIds
	 * @author huangzq
	 */
	public void clearNoSellValuse(int productId,List<Long> noDeleteIds ){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	pv.id ");
		sql.append("FROM ");
		sql.append("	t_pro_property_value pv ");
		sql.append("LEFT JOIN t_pro_property p ON p.id = pv.property_id ");
		sql.append("WHERE");
		sql.append("	pv.product_id = ? ");
		sql.append("AND p.is_sell = ? ");
		List<Long> ids =  Db.query(sql.toString(),productId,BaseConstants.NO);
		ids.removeAll(noDeleteIds);
		String deleteSql = "delete from t_pro_property_value where id in (";
		if(StringUtil.notNull(ids)){
			for(Long id : ids){
				deleteSql+=id+",";
			}
			deleteSql = deleteSql.substring(0,deleteSql.length()-1);
			deleteSql+=")";
			Db.update(deleteSql);
		}
	}
	/**
	 * 清理商品销售属性值
	 * @param productId
	 * @param noDeleteIds
	 * @author huangzq
	 */
	public void clearSellValuse(int productId,List<Long> noDeleteIds ){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	pv.id ");
		sql.append("FROM ");
		sql.append("	t_pro_property_value pv ");
		sql.append("LEFT JOIN t_pro_property p ON p.id = pv.property_id ");
		sql.append("WHERE");
		sql.append("	pv.product_id = ? ");
		sql.append("AND p.is_sell = ? ");
		List<Long> ids =  Db.query(sql.toString(),productId,BaseConstants.YES);
		ids.removeAll(noDeleteIds);
		String deleteSql = "delete from t_pro_property_value where id in (";
		if(StringUtil.notNull(ids)){
			for(Long id : ids){
				deleteSql+=id+",";
			}
			deleteSql = deleteSql.substring(0,deleteSql.length()-1);
			deleteSql+=")";
			Db.update(deleteSql);
		}
	}
	/**
	 * 删除商品的普通属性值
	 * @param productId
	 * @author huangzq
	 */
	public void deleteNoSellPropertyValue(Integer productId){

		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	pv.id ");
		sql.append("FROM ");
		sql.append("	t_pro_property_value pv ");
		sql.append("LEFT JOIN t_pro_property p ON p.id = pv.property_id ");
		sql.append("WHERE");
		sql.append("	pv.product_id = ? ");
		sql.append("AND p.is_sell = ? ");
		List<Long> ids =  Db.query(sql.toString(),productId,BaseConstants.NO);
		String deleteSql = "delete from t_pro_property_value where id in (";
		if(StringUtil.notNull(ids)){
			for(Long id : ids){
				deleteSql+=id+",";
			}
			deleteSql = deleteSql.substring(0,deleteSql.length()-1);
			deleteSql+=")";
			Db.update(deleteSql);
		}
		
	}
	/**
	 * 删除商品的销售属性值
	 * @param productId
	 * @author huangzq
	 */
	public void deleteSellPropertyValue(Integer productId){

		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	pv.id ,pv.img_path ");
		sql.append("FROM ");
		sql.append("	t_pro_property_value pv ");
		sql.append("LEFT JOIN t_pro_property p ON p.id = pv.property_id ");
		sql.append("WHERE");
		sql.append("	pv.product_id = ? ");
		sql.append("AND p.is_sell = ? ");
		List<Record> properties =  Db.find(sql.toString(),productId,BaseConstants.YES);
		List<String> imgPaths = new ArrayList<String>();
		String deleteSql = "delete from t_pro_property_value where id in (";
		if(StringUtil.notNull(properties)){
			for(Record r : properties){
				deleteSql+=r.getLong("id")+",";
				if(StringUtil.notNull(r.getStr("img_path"))){
					imgPaths.add(r.getStr("img_path"));
				}
			}
		
			deleteSql = deleteSql.substring(0,deleteSql.length()-1);
			deleteSql+=")";
			Db.update(deleteSql);
		}
		
	}
	/**
	 * 删除商品的销售属性值
	 * @param productId
	 * @author huangzq
	 */
	public void deleteAllPropertyValue(Integer productId){

		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	pv.id ,pv.img_path ");
		sql.append("FROM ");
		sql.append("	t_pro_property_value pv ");
		sql.append("LEFT JOIN t_pro_property p ON p.id = pv.property_id ");
		sql.append("WHERE");
		sql.append("	pv.product_id = ? ");
		List<Record> properties =  Db.find(sql.toString(),productId);
		String deleteSql = "delete from t_pro_property_value where id in (";
		if(StringUtil.notNull(properties)){
			for(Record r : properties){
				deleteSql+=r.getLong("id")+",";

			}

			deleteSql = deleteSql.substring(0,deleteSql.length()-1);
			deleteSql+=")";
			Db.update(deleteSql);
		}
		
	}
	/**
	 * 设置图片
	 * @param pv
	 * @param imgPath
	 * @author huangzq
	 */
	public void setImgPath(ProductPropertyValue pv){
		String imgPath = pv.getStr("img_path");
		//图片上传路径
		String imgUploadPath = PropKit.getProp("global.properties").get("image.upload.base.path");
		File file = new File(imgUploadPath+imgPath);
		if(file.exists()){
			Image.dao.enable(pv.getStr("img_path"));
			pv.set("img_path", imgPath);
		}else{
			return;
		}
		//中图路径
		String midPath = StringUtil.getMidPath(imgPath);
		file = new File(imgUploadPath+midPath);
		if(file.exists()){
			pv.set("mid_path", midPath);
		}else{
			pv.set("mid_path", imgPath);
		}
		//小图路径
		String smallPath = StringUtil.getSmallPath(imgPath);
		file = new File(imgUploadPath+smallPath);
		if(file.exists()){
			pv.set("small_path", smallPath);
		}else{
			pv.set("small_path", imgPath);
		}
		
	}
	/**
	 * 属性值特殊字符处理
	 * @param value
	 * @return
	 * @author huangzq
	 */
	public static String propertyValueChange(String value){
		if(StringUtil.isNull(value)){
			return null;
		}
		return value.replace(",", "，").replace(":", "：").trim();
		
	}


}
