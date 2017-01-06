package com.buy.model.product;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/** 
 * @author wangy
 * @date 2015年9月30日 下午1:23:43 
 */
public class ProductSignboardMap extends Model<ProductSignboardMap> {

	/**
	 * 商品标识映射
	 */
	private static final long serialVersionUID = 1L;
	public static final ProductSignboardMap dao = new ProductSignboardMap();
	
	public static final int SIGNTYPE_BRANDSELECTION = 1;//品牌精选
	public static final int SIGNTYPE_TODAYSPIKE = 2;//今日秒杀
	public static final int SIGNTYPE_NEWLISTING = 3;//新品上市
	public static final int SIGNTYPE_OVERSEAS = 4;//境外精品
	public static final int SIGNTYPE_HOTSALE = 5;//热卖推荐
	public static final int SIGNTYPE_HOTSTORE = 6;//e趣云店热销榜
	public static final int SIGNTYPE_EFUNPOPULAR = 7;//人气一折购
	
	public boolean isTodaySpikeProduct (Integer proId) {
		String sql = "select count(*) as count from t_pro_tag_map where 1 = 1 "
				   + "and tag_type = " + SIGNTYPE_TODAYSPIKE + " and product_id = " + proId;
		return Db.findFirst(sql).getLong("count") > 0 ? true : false;
	}
}
