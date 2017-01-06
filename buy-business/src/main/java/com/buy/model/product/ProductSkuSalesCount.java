package com.buy.model.product;

import java.util.List;

import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

public class ProductSkuSalesCount extends Model<ProductSkuSalesCount> {

	private static final long serialVersionUID = 4481013030718590578L;
	
	public static final ProductSkuSalesCount dao = new ProductSkuSalesCount();
	
	/**
	 * 初始化.
	 * 
	 * @param productId
	 *            商品Id.
	 */
	public void init(Integer productId) {
		if (!StringUtil.isNull(productId)) {
			List<Record> list = ProductSku.dao.getSkuCodesByProductId(productId);
			StringBuffer sb = new StringBuffer("INSERT INTO t_sku_sales_count(sku_code) VALUES ");
			
			for (int i = 0; i < list.size(); i++) {
				Record record = list.get(i);
				sb.append("('").append(record.getStr("code")).append("')");
				if(i != list.size() - 1) {
					sb.append(",");
				}
			}
			Db.update(sb.toString());
		}
	}
	
	/**
	 * 初始化.
	 * 
	 * @param skus
	 *            Sku列表.
	 */
	public void init(List<Record> skus,Integer productId) {
		if (!StringUtil.isNull(skus)) {
			StringBuffer sb = new StringBuffer("INSERT INTO t_sku_sales_count(sku_code,product_id,update_time) VALUES ");
			
			for (int i = 0; i < skus.size(); i++) {
				Record record = skus.get(i);
				sb.append("('").append(record.getStr("code")).append("',").append(productId).append(",now())");;
				if(i != skus.size() - 1) {
					sb.append(",");
				}
			}
			Db.update(sb.toString());
		}
	}
	
	/**
	 * 更新.
	 * 
	 * @param productId
	 *            商品Id
	 * @param skus
	 *            Sku列表.
	 */
	@Before(Tx.class)
	public void update(Integer productId) {
		if (StringUtil.notNull(productId)) {
			//获取待新增的sku
			List<Record> addList = ProductSku.dao.getAddSku4SaleCount(productId);
			//获取待删除的sku
			List<Record> deleteList = ProductSku.dao.getDeleteSku4SaleCount(productId);
			
			// 新提交的需要插入的数据.
			StringBuffer insert = new StringBuffer("INSERT INTO t_sku_sales_count(sku_code,product_id,update_time) VALUES ");
			if(addList.size()>0){
				for (int i = 0; i < addList.size(); i++) {
					Record record = addList.get(i);
					insert.append("('").append(record.getStr("sku_code")).append("',").append(productId).append(",now())");
					if(i != addList.size() - 1) {
						insert.append(",");
					}
				}
				Db.update(insert.toString());
			}
			// 需要删除的数据.
			for (int i = 0; i < deleteList.size(); i++) {
				Record record = deleteList.get(i);
				SkuSalesCount.dao.deleteById(record.getStr("sku_code"));
			}
		}
	}

	/**
	 * 初始化.
	 * 
	 * @param skuCode
	 *            商品sku编码.
	 */
	public void init(String skuCode,Integer productId) {
		if (!StringUtil.isBlank(skuCode)) {
			Db.update("INSERT INTO t_sku_sales_count(sku_code,product_id,update_time) VALUES (?，?,now())", skuCode,productId);
		}
	}

}