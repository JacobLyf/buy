package com.buy.model.ad;

import java.util.ArrayList;
import java.util.List;

import com.buy.common.BaseConstants;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * Model - PC广告
 */
public class Ad extends Model<Ad>{
	
	private static final long serialVersionUID = 1L;
	
	public static final Ad dao = new Ad();
	
	/**
	 * 广告状态：投放
	 */
	public final static int STATUS_YES = 1;
	/**
	 * 广告状态：不投放
	 */
	public final static int STATUS_NO = 0;
	
	/**
	 * 广告位置：POS主界面
	 */
	public final static String  POSITION_POS_MAIN_IMGS = "posMainImgs";
	
	/**
	 * 广告位置：搜索顶部广告位
	 */
	public final static String  POSITION_SEARCH_HEAD = "searchHead";
	
	/**
	 * 广告位置：首页顶部广告位
	 */
	public final static String  POSITION_INDEX_HEAD = "indexHead";
	
	/**
	 * 广告位置：首页轮播大图
	 */
	public final static String  POSITION_BANNER = "indexBanner";
	
	/**
	 * 幸运一折购参与商品广告位
	 */
	public final static String  POSITION_LUCKY_BUY = "luckyBuy";
	
	/**
	 * 楼层间的横幅广告位
	 */
	public final static String  POSITION_TRANSVERSE_AD = "transverseAd";
	
	/**
	 * e趣特色--左上广告位
	 */
	public final static String  POSITION_EFUNFEATURES_UP_LEFT = "efunFeaturesUpLeft";
	/**
	 * e趣特色--右上广告位
	 */
	public final static String  POSITION_EFUNFEATURES_UP_RIGHT = "efunFeaturesUpRight";
	/**
	 * e趣特色--下部广告位
	 */
	public final static String  POSITION_EFUNFEATURES_DOWN = "efunFeaturesDown";
	
	/**
	 * e趣精选今日剁手价广告位
	 */
	public final static String  POSITION_EFUN_SPECIAL_OFFER = "efunChoiceSpecialOffer";
	/**
	 * e趣精选今日最热款广告位
	 */
	public final static String  POSITION_EFUN_HOT_SELL= "efunChoiceHotSell";
	/**
	 * e趣精选今日品牌精选广告位
	 */
	public final static String  POSITION_EFUN_GOOD_BRAND = "efunChoiceGoodBrand";
	/**
	 * e趣精选今日新品推荐广告位
	 */
	public final static String  POSITION_EFUN_NEW_BRAND = "efunChoiceNewBrand";
	/**
	 * e趣精选--左侧大图
	 */
	public final static String  POSITION_EFUN_LEFT_IMAGE_AD = "efunChoiceLeftImage";

	
	/**
	 * 1F潮流服装特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_1F_SPECIAL = "1FSpecial";
	public final static String POSITION_1F_COMMON = "1FCommon";
	public final static String POSITION_1F_TEXT = "1FText";
	
	/**
	 * 2F精品鞋包特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_2F_SPECIAL = "2FSpecial";
	public final static String POSITION_2F_COMMON = "2FCommon";
	public final static String POSITION_2F_TEXT = "2FText";
	
	/**
	 * 3F手机通讯特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_3F_SPECIAL = "3FSpecial";
	public final static String POSITION_3F_COMMON = "3FCommon";
	public final static String POSITION_3F_TEXT = "3FText";
	
	/**
	 * 4F电脑数码特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_4F_SPECIAL = "4FSpecial";
	public final static String POSITION_4F_COMMON = "4FCommon";
	public final static String POSITION_4F_TEXT = "4FText";
	
	/**
	 * 5F食品酒水特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_5F_SPECIAL = "5FSpecial";
	public final static String POSITION_5F_COMMON = "5FCommon";
	public final static String POSITION_5F_TEXT = "5FText";
	
	/**
	 * 6F美妆个护特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_6F_SPECIAL = "6FSpecial";
	public final static String POSITION_6F_COMMON = "6FCommon";
	public final static String POSITION_6F_TEXT = "6FText";
	/**
	 * 7F生活日用特别广告位、普通广告位、右上角文字广告
	 */
	public final static String POSITION_7F_SPECIAL = "7FSpecial";
	public final static String POSITION_7F_COMMON = "7FCommon";
	public final static String POSITION_7F_TEXT = "7FText";
	
	/**
	 * 商品详情页广告位
	 */
	public final static String POSITION_PRODUCT_ONE = "productOne";
	
	/**
	 * 幸运一折购详情页广告位
	 */
	public final static String POSITION_EFUN_PRODUCT_ONE = "efunProductOne";
	
	/**
	 * 幸运一折购奖区
	 */
	public final static String POSITION_EFUN_PRICE_AD = "efunHotPrice";

	/**
	 * PC我要开店
     */
	public final static String POSITION_OPEN_SHOP = "openShop";
	/**
	 * wap/app我要开店
     */
	public final static String POSITION_WAP_APP_OPEN_SHOP = "wappOpenShop";
	
	/**
	 * 商家登录
	 */
	public final static String POSITION_LOGIN_MERCHANT = "loginMerchant";
	/**
	 * 会员登录广告位
	 */
	public final static String POSITION_LOGIN_USER = "loginUser";

	/**
	 * 对redis缓存的广告数据、新加入的广告数据重新排序;排序越小越在前面展示
	 * @param oldList
	 * @param newAd
	 * @return
	 * @author chenhg
	 * 2016年3月1日 下午2:48:03
	 */
	public List<Record> orderAd(List<Record> oldList,Record newAd){
		List<Record> newList = new ArrayList<Record>();
		//==========分析=============//
		// 1、newAd不在Redis中：a.排序最小 b.排序最大 c.不是最小，也不是最大
		// 2、newAd在Redis中：去掉旧的，新的加入来
		//==========分析=============//
		boolean flag = true;
		Integer newAdSortNum = Integer.valueOf(newAd.get("sortNum").toString());
		String newAdId = newAd.get("id").toString();
		
		for(int i = 0; i < oldList.size(); i++){
			Record record = oldList.get(i);
			String currAdId = record.get("id").toString();
			Integer currSortNum = Integer.valueOf(record.get("sortNum").toString());
			if(flag && currSortNum > newAdSortNum){
				newList.add(newAd);
				flag = false;//已经加入
			}
			if(!newAdId.equals(currAdId)){//newAd在Redis中：把旧的去掉
				newList.add(record);
			}
		}
		//flag 为true时，证明 新的广告 要排在最后
		if(flag){
			newList.add(newAd);
		}
		
		return newList;
	}
	
	/**
	 * 删除 不发布的广告
	 * @param oldList
	 * @param adId
	 * @return
	 * @author chenhg
	 * 2016年3月1日 下午3:54:50
	 */
	public List<Record> removeRecord(List<Record> oldList,String adId){
		for(Record record : oldList){
			if(adId.equals(record.get("id").toString())){
				oldList.remove(record);
				break;
			}
		}
		return oldList;
	}
	
	/**
	 * 返回已经发布的广告信息
	 * @return
	 * @author chenhg
	 * 2016年9月1日 下午4:19:25
	 */
	public List<Record> getAllReleaseAd(){
		String sql = " SELECT a.*, b.`no` FROM t_ad a LEFT JOIN t_ad_type b ON a.type_id = b.id WHERE  a.`status` = 1 ORDER BY b.id,a.sort_num ";
		return Db.find(sql);
	}
	
	
	/**
	 *  获取广告信息通用方法（查数redis的方式）
	 * @param showNum
	 * @param adTypeId
	 * @return
	 * @author chenhg
	 * 2016年3月1日 下午2:59:55
	 */
	public List<Record> getAdList(Integer showNum, String adTypeName){
		List<Record> adMessage = new ArrayList<Record>(showNum);
		Cache adCash = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
		String key = BaseConstants.Redis.KEY_AD + adTypeName;
		 
		if(adCash.exists(key)){
			List<Record> list = adCash.get(key);
			if(list.size() > showNum){
				for(int i = 0; i < showNum; i++){
					adMessage.add(list.get(i));
				}
			}else{
				adMessage = list;
			}
		}else{//空
			return null;
		}
		return adMessage;
	}
	
	
	/**
	 * 返回广告记录
	 * @param showNum
	 * @param adTypeName
	 * @return
	 * @author chenhg
	 * 2016年8月15日 下午7:03:13
	 */
	public Record getAdRecord(Integer showNum, String adTypeName){
		List<Record> adList = Ad.dao.getAdList(showNum, adTypeName);
		if(adList != null && adList.size() > 0){
			return adList.get(0);
		}
		return null;
	}
	
}
