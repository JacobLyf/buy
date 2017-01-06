package com.buy.model.ad;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model - App广告
 * @author chenhg
 */
public class AppAd extends Model<AppAd> {

	private static final long serialVersionUID = 1L;
	
	public static final AppAd dao = new AppAd();
	
	/**
	 * 广告状态：显示
	 */
	public final static int STATUS_YES = 1;
	/**
	 * 广告状态：不显示
	 */
	public final static int STATUS_NO = 0;
	
	//广告类型(1:banner，2:e趣市场，3品牌推荐，4发现好店，5热门搜索词
	/**
	 * 广告类型：banner
	 */
	public final static int TYPE_BANNER = 1;
	/**
	 * 广告类型：e趣市场
	 */
	public final static int TYPE_EFUN = 2;
	/**
	 * 广告类型：品牌推荐
	 */
	public final static int TYPE_BRAND = 3;
	/**
	 * 广告类型：发现好店
	 */
	public final static int TYPE_GOOD_SHOP = 4;
	/**
	 * 广告类型：热门搜索词
	 */
	public final static int TYPE_HOT_WORD = 5;
	/**
	 * 广告类型：幸运一折购专区广告大图
	 */
	public final static int TYPE_EFUNAD_BIG = 6;
	/**
	 * 广告类型：幸运一折购专区广告小图
	 */
	public final static int TYPE_EFUNAD_SMALL = 7;
	/**
	 * 广告类型：e趣好货封面图
	 */
	public final static int TYPE_EFUNAD_EQUGOODS = 8;
	/**
	 * 广告类型：app抢钱banner广告
	 */
	public final static int TYPE_GETMONEY_BANNER = 9;

	/**
	 * 广告类型：app首页:开店,台湾馆,云店banner广告
	 * 我要开店:ad_position 1
	 * 台湾馆:ad_position 2
	 * e趣云店:ad_position 3
	 */
	public final static int TYPE_INDEX_BANNER = 10;
	public final static int INDEX_BANNER_OPENSHOP = 1;//我要开店
	public final static int INDEX_BANNER_TAIWAN = 2;//台湾馆
	public final static int INDEX_BANNER_ESTORE = 3;//e趣云店

	/**
	 * 广告类型：app启动页banner广告
	 */
	public final static int TYPE_INIT_BANNER = 11;

	/**
	 * 是否固定（0：否，1：是）
	 */
	public final static int FIXATION_YES = 1;
	public final static int FIXATION_NO = 0;
	
	/**
	 * 跳转类型（1：web页面，2：原生页面）
	 */
	public final static int JUMP_WEB = 1;
	public final static int JUMP_ORI = 2;
	
	/**
	 * 跳转位置（1：商品列表，2：商品详情，3：店铺列表，4：店铺详情）
	 */
	public final static int LOCATION_PRO_LIST = 1;
	public final static int LOCATION_PRO_DETAIL = 2;
	public final static int LOCATION_SHOP_LIST = 3;
	public final static int LOCATION_SHOP_DETAIL = 4;

	public String getIndexBanner(int position){
		String sql = "SELECT a.img_path imgPath FROM t_app_ad a WHERE a.type = ? AND a.ad_position = ? ";
		return Db.queryStr(sql,TYPE_INDEX_BANNER,position);
	}
	
}
