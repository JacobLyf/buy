package com.buy.model.store;

import java.sql.SQLException;
import java.util.List;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * Model - 仓库跟sku关联表
 */
public class StoreSkuMap extends Model<StoreSkuMap> {

	private static final long serialVersionUID = 1L;

	public static final StoreSkuMap dao = new StoreSkuMap();

	/**
	 * 获取某个仓库中某个sku的库存
	 * @param skuCode
	 * @param storeNo
	 * @return
	 * @author huangzq
	 */
	public Integer getCount(String skuCode,String storeNo){
		return dao.findByIdLoadColumns(new Object[]{skuCode, storeNo},"pro_count").getInt("pro_count");
	}
	
	/**
	 * 获取某个仓库中某个sku的真实库存（减去锁定数）
	 * @param skuCode
	 * @param storeNo
	 * @return
	 */
	public Integer getStoreRealCount(String skuCode, String storeNo) {
		StoreSkuMap record = dao.findByIdLoadColumns(new Object[]{skuCode, storeNo},"pro_count, store_lock_count");
		return (record.getInt("pro_count") - record.getInt("store_lock_count"));
	}
	
	/**
	 * 获取总仓映射
	 * @return
	 * @author huangzq
	 */
	public StoreSkuMap getTotalStoreSkuMap(){
		
		String sql = "SELECT sm.* FROM t_store_sku_map sm left join t_store s on s.no = sm.store_no WHERE s.parent_id is null ";
		return dao.findFirst(sql);
	}
	/**
	 * 添加可用库存
	 * @param code
	 * @param count
	 * @param storeNo
	 * @return
	 * @author huangzq
	 */
	public void plusCount(String skuCode ,String storeNo,Integer count ){
		boolean result = false;
		StoreSkuMap storeSkuMap = null;
	
		while (result==false) {
			if(StringUtil.isNull(skuCode)){
				storeSkuMap = this.getTotalStoreSkuMap();
			}else{
				storeSkuMap = StoreSkuMap.dao.findById(skuCode,storeNo);
			}
			if(storeSkuMap!=null){
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_store_sku_map SET pro_count = pro_count + ? ,");
				updateSql.append(" version = version + 1,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND sku_code = ?");
				updateSql.append(" AND store_no = ?");
				int resultCount = Db.update(updateSql.toString(),count,storeSkuMap.getInt("version"),skuCode,storeNo);
				//更新成功
				if(resultCount!=0){
					result = true;
					//添加记录
					StoreInOutRecord.dao.save(storeNo, skuCode, count, StoreInOutRecord.TYPE_ONLINE_ORDER_IN, null);
				}
			}else{
				return;
				//数据不存在
				//throw new RuntimeException("数据不存在");
			}
			
		}
	}
	
	/**
	 * 扣减库存（扣除：选中仓库已锁定库存、选中仓库的实际库存）
	 * @param storeNo
	 * @param skuCode
	 * @param count
	 * @return
	 * @author chenhg
	 */
	public boolean subsubtractCount(String storeNo, String skuCode, int count) throws SQLException{
		if(count < 1){
			return false;
		}	
		boolean flag = false;
		StoreSkuMap storeSkuMap = null;
		while (flag==false) {
			storeSkuMap = StoreSkuMap.dao.findById(skuCode,storeNo);
			
			if(storeSkuMap==null){
				return false;
			}
			if(storeSkuMap.getInt("pro_count")>=count){
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_store_sku_map SET pro_count = pro_count - ? ,");
				updateSql.append(" store_lock_count = store_lock_count - ?, ");
				updateSql.append(" version = version + 1,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND sku_code = ?");
				updateSql.append(" AND store_no = ?");
				int resultCount = Db.update(updateSql.toString(), count, count, storeSkuMap.getInt("version"), skuCode, storeNo);
				//更新成功
				if(resultCount!=0){
					flag = true;
					//添加记录
					StoreInOutRecord.dao.save(storeNo, skuCode, count, StoreInOutRecord.TYPE_ONLINE_ORDER_OUT, null);
				}
			}else{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 发货，减库存（一个云店，多个sku）
	 * @param storeNo
	 * @param skuCodeCountList
	 * @return
	 * @throws SQLException
	 */
	public boolean subtractCountForMany(String storeNo,List<Record> skuCodeCountList ) throws SQLException{

		//总的更新结果
		boolean result = true;
		for(Record r :skuCodeCountList ){
			String skuCode = r.getStr("skuCode");
			Integer count = r.getInt("count");
			//减库存
			if(!subsubtractCount(storeNo, skuCode, count)){
				result = false;
				break;
			}
		}
		return result;
		
	}
	
	/**
	 * 通过skuCode,storeNo获取 StoreSkuMap对象
	 * @param skuCode
	 * @param storeNo
	 * @return
	 * @author chenhg
	 * 2016年3月3日 下午2:53:38
	 */
	public StoreSkuMap getStoreSkuMapBySkuCodeAndStoreNo(String skuCode, String storeNo){
		return StoreSkuMap.dao.findFirst("select * from t_store_sku_map where sku_code = ? and store_no = ? ", skuCode, storeNo);
	}
	
	/**
	 * 是否可自提（是否有满足购买数量的商品SKU的O2O门店）
	 * @param proCount 购买数量
	 * @param skuCode  商品SKU识别码
	 * @return
	 * @author Jacob
	 * 2015年12月10日下午5:40:31
	 */
	public boolean isPickUp(Integer proCount,String skuCode){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	s.* ");
		sql.append(" FROM ");
		sql.append(" 	t_store s, ");
		sql.append(" 	t_o2o_sku_map osm ");
		sql.append(" WHERE ");
		sql.append(" 	s.no = osm.store_no ");
		//sql.append(" 	AND osm.pro_count >= ? ");
		sql.append(" 	AND osm.sku_code = ? ");
		return Store.dao.findFirst(sql.toString(),skuCode)!=null?true:false;
	}

	/**
	 * 对应云店的sku显示库存是否足够扣取(下单)：判断 实际库存-已锁定库存
	 * @param storeNo
	 * @param skuCode
	 * @param count：下单数量
	 * @return
	 */
	public boolean enoughStoreCount(String storeNo, String skuCode, int count){
		//防止非法操作
		if(count < 1){
			return false;
		}
		StoreSkuMap storeSkuMap = StoreSkuMap.dao.findById(skuCode,storeNo);
		
		if(storeSkuMap==null){
			return false;
		}else{
			int skuCount = storeSkuMap.getInt("pro_count") - storeSkuMap.getInt("store_lock_count");
			if(skuCount < count){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断库存是否充足
	 * @param storeNo
	 * @param skuList
	 * @return
	 */
	public boolean enoughStoreCountForMany(String storeNo, List<Record> skuList){
		for (Record sku : skuList) {
			if(!enoughStoreCount(storeNo, sku.getStr("skuCode"), sku.getInt("count"))){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 对应云店的sku实际库存是否足够扣取(发货)：直接判断 实际库存
	 * @param storeNo
	 * @param skuCode
	 * @param count：发货数
	 * @return
	 */
	public boolean enoughStoreCountForSend(String storeNo, String skuCode, int count){
		//防止非法操作
		if(count < 1){
			return false;
		}
		StoreSkuMap storeSkuMap = StoreSkuMap.dao.findById(skuCode,storeNo);
		
		if(storeSkuMap==null){
			return false;
		}else if(storeSkuMap.getInt("pro_count") < count){
				return false;
		}
		return true;
	}
	
	
	/**
	 * 增加仓库锁定库存数
	 * @param storeNo
	 * @param skuCode
	 * @param count
	 * @return
	 */
	public boolean addStoreLockCount(String storeNo, String skuCode, int count){
		//防止非法操作
		if(count < 1){
			return false;
		}
		boolean result = false;
		while(!result){
			StoreSkuMap storeSkuMap = StoreSkuMap.dao.findById(skuCode,storeNo);
			if(storeSkuMap==null){
				return false;
			}else{
				int skuCount = storeSkuMap.getInt("pro_count") - storeSkuMap.getInt("store_lock_count");
				if(skuCount >= count){//库存充足
					StringBuffer updateSql = new StringBuffer();
					updateSql.append(" UPDATE t_store_sku_map SET store_lock_count = store_lock_count + ? ,");
					updateSql.append(" version = version + 1,update_time = now()");
					updateSql.append(" WHERE");
					updateSql.append(" version = ?");
					updateSql.append(" AND sku_code = ?");
					updateSql.append(" AND store_no = ?");
					int resultCount = Db.update(updateSql.toString(),count,storeSkuMap.getInt("version"),skuCode,storeNo);
					
					if(resultCount!=0){//更新成功并添加库存进出记录
						StoreInOutRecord.dao.save(storeNo, skuCode, count, StoreInOutRecord.TYPE_ONLINE_ORDER_OUT, null);
						result = true;
					}
				}else{//库存不足
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 减去仓库锁定库存数
	 * @param storeNo
	 * @param skuCode
	 * @param count
	 * @return
	 */
	public boolean subtractStoreLockCount(String storeNo, String skuCode, int count){
		//防止非法操作
		if(count < 1){
			return false;
		}
		boolean result = false;
		while(!result){
			StoreSkuMap storeSkuMap = StoreSkuMap.dao.findById(skuCode,storeNo);
			if(storeSkuMap==null){
				return false;
			}else{
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_store_sku_map SET store_lock_count = store_lock_count - ? ,");
				updateSql.append(" version = version + 1,update_time = now()");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND sku_code = ?");
				updateSql.append(" AND store_no = ?");
				int resultCount = Db.update(updateSql.toString(),count,storeSkuMap.getInt("version"),skuCode,storeNo);
				
				if(resultCount!=0){//更新成功并添加库存进出记录
					StoreInOutRecord.dao.save(storeNo, skuCode, count, StoreInOutRecord.TYPE_ONLINE_ORDER_OUT, null);
					result = true;
				}
			}
		}
		
		return true;
	}
	
	public List<Record> productStockDetail(String skuCode) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select skm.pro_count-skm.store_lock_count as count, skm.store_no as storeNo, "
				         + "s.name as storeName, t1.name as prov, t2.name as city, "
				         + "t3.name as area, s.address");
		sql.append(" from t_store_sku_map skm");
		sql.append(" left join t_o2o_sku_map sm on skm.store_no = sm.store_no");
		sql.append(" and skm.sku_code = sm.sku_code");
		sql.append(" left join t_store s on skm.store_no = s.`no`");
		sql.append(" left join t_address t1 on t1.`code` = s.province_code");
		sql.append(" left join t_address t2 on t2.`code` = s.city_code");
		sql.append(" left join t_address t3 on t3.`code` = s.area_code");
		sql.append(" where skm.sku_code = ?");
		sql.append(" and ( s.type <> ? or (s.type = ? and sm.sku_code IS NOT NULL ))");
		sql.append(" order by skm.create_time desc");
		return Db.find(sql.toString(), skuCode, Store.TYPE_TOTAL, Store.TYPE_CLOUD);
	}
	
	/**
	 * 根据sku对比两间云店库存，返回更多库存的云店
	 * @return
	 */
	public String compareStroeCount(String skuCodes, String storeNo_One, String storeNo_Two) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select store_no as storeNo ")
		   .append("  from ( select SUM(pro_count) num, store_no FROM t_store_sku_map ")
		   .append("   where sku_code in ( ")
		   .append(skuCodes)
		   .append(" ) and store_no in ( ")
		   .append("'" + storeNo_One + "', ")
		   .append("'" + storeNo_Two + "' )")
		   .append(" group by sku_code, store_no ) a ")
		   .append(" order by num desc ");
		return Db.findFirst(sql.toString()).getStr("storeNo");
	}
}
