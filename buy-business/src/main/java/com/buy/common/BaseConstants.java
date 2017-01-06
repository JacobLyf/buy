package com.buy.common;


/**
 * 常量类
 * @author zhuoqi
 *
 */
public class BaseConstants {
    /** ajax 正确状态*/
    public static final String AJAX_STATE_OK = "200";
    /** ajax 正确 message*/
    public static final String AJAX_MESSAGE_OK = "操作成功";
    /** ajax 错误状态*/
    public static final String AJAX_STATE_ERROR = "300";
    /** ajax 错误 message*/
    public static final String AJAX_MESSAGE_ERROR = "程序错误，请联系管理员";
    /** ajax 错误状态*/
    public static final String AJAX_STATE_OUT_TIME = "301";
    /** ajax 错误 message*/
    public static final String AJAX_MESSAGE_OUT_TIME = "会话超时";
    /** ajax 返回方式： nav*/
    public static final String AJAX_FOR_NAV = "nav";
    /** ajax 返回方式： dialog*/
    public static final String AJAX_FOR_DIALOG = "dialog";
    /** ajax 返回方式： div*/
    public static final String AJAX_FOR_DIV = "div";
    /** ajax 不能删除 message*/
    public static final String AJAX_MESSAGE_NOT_DELETE = "数据已使用，不能删除";
    
    /**
     * 配置文件路径
     */
    public static final String CONFIG_PATH = "/mydata/opt/project/config/";
    
    
    public static final String salt = "sc~.6e@dZ8$s7_ad>d54da%^&*(sda*a5d89j:'8eadsFaJ7SADF65X86aLsr";
    
    /**
     * Session - 登录会员信息
     */
    public static final String SESSION_USER = "user_info";
    
    public static final String SESSION_SHOP = "shop_info";
    
    public static final String SESSION_AGENT = "agent_info";
    
    public static final String SESSION_SUPPLIER = "supplier_info";
    /**
     * 当前登录人id
     */
    public static final String SESSION_CURRENT_ID = "current_id";
    
    
    

    /**
     * 数据库 - 时间日期空值
     */
    public static final String SQL_DATETIME_NULL = "0000-00-00 00:00:00";
    /**
     * 总公司的店铺id
     */
    public static final int ZGS_SHOP_ID = 1;
    
    /**
     * web 路径
     */
    public static final String REQUEST_CTX = "ctx";
    /**
     * 图片访问路径
     */
    public static final String REQUEST_IMG_PATH = "imgPath";
    /**
     * 非图片访问路径
     */
    public static final String REQUEST_NO_IMG_PATH = "otherPath";
	/**
	 * 图片类型
	 */
	public static final String IMAGE_FILE_TYPE = ".jpg,.jpeg,.png,.gif";
	/**
	 * 压缩文件类型
	 */
	public static final String COMPRESS_FILE_TYPE = ".zip,.7z,.rar";
	/**
	 * APP文件类型 - 安卓
	 */
	public static final String APP_FILE_TYPE_ANDROID = ".apk";
    
    /**
     * 公共常量：是
     */
    public static final int YES = 1;
    /**
     * 公共常量：否
     */
    public static final int NO = 0;
    
    
    /**
     * 编码
     * @author Sylveon
     */
    public interface Encoding {
    	/**
    	 * 编码 - UTF-8 
    	 */
    	public static final String ENCODING_UTF8 = "utf-8";
    }
    
    /**
     * 上传图片存放文件夹
     * @author Sylveon
     */
    public interface UploadImgSort {
    	/**
    	 * 最小压缩限制：300K
    	 */
    	public final int MIN_COMPRESS_SIZE=300*1024;
    	/**
		 * 商品图片 - 图片存放文件夹
		 */
		public final String SORT_PRODUCT = "product";
    	/**
		 * 页面广告图片 - 图片存放文件夹
		 */
		public final String SORT_AD = "cms";
		/**
		 * 手机广告图片 - 图片存放文件夹
		 */
		public final String SORT_ADAPP = "cmsApp";
		/**
		 * 广告图片 - 图片存放文件夹
		 */
		public final String SORT_NEWS = "news";
		/**
		 * 消息图片 - 图片存放文件夹
		 */
		public final String SORT_MESSAGE = "msg";
		/**
		 * 消息列表LOGO - LOGO存放文件夹
		 */
		public final String SORT_MESSAGE_TYPE = "msgTypeLogo";
		/**
		 * 物流公司LOGO - LOGO存放文件夹
		 */
		public final String SORT_LOGISTICS_COMPANY = "logisticsCompanyLogo";
		/**
		 * 店铺LOGO - LOGO存放文件夹
		 */
		public final String SORT_SHOP = "shopLogo";
		/**
		 * 店铺类别LOGO - LOGO存放文件夹
		 */
		public final String SORT_SHOP_SORT = "shopSortLogo";
		/**
		 * 店铺申请 - 图片存放文件夹
		 */
		public final String SORT_SHOP_APPLY = "shopType";
		/**
		 * 商品分类LOGO - LOGO存放文件夹
		 */
		public final String SORT_PRODUCT_SORT = "productSort";
		/**
		 * 供货商LOGO - LOGO存放文件夹
		 */
		public final String SORT_SUPPLIER_LOGO = "supplierLogo";
		/**
		 * 会员级别LOGO - LOGO存放文件夹
		 */
		public final String SORT_USER_RANK = "userRankLogo";
		/**
		 * 品牌LOGO - LOGO存放文件夹
		 */
		public final String SORT_BRAND_LOGO = "brandLogo";
		
		/**
		 * 精选市场图片- LOGO存放文件夹
		 */
		public final String SORT_REGION_LOGO= "regionLogo";
		
		/**
		 * 头像 - 会员 - 存放路径
		 */
		public final String SORT_AVATAR_MEMBR = "avatar_member";
		/**
		 * 头像 - 店铺 - 存放路径
		 */
		public final String SORT_AVATAR_SHOP = "avatar_shop";
		
		/**
		 * 店铺激活 - 店铺 - 存放路径
		 */
		public final String SORT_ATIVATE_SHOP = "ativate_shop";
		
		/**
		 * 店铺实名认证-店铺-存放路径
		 */
		public final String SORT_VERIFIED_SHOP = "verified_shop";
		/**
		 * 店铺认证 - 快速发货认证附件 - 存放路径
		 */
		public final String SORT_EXPRESS_SERVICES_SHOP = "cer_express_services";
		/**
		 * 店铺编辑器文件位置
		 */
		public final String SORT_EDITRO_SHOP = "shop_editor";
		/**
		 * 商品属性图片存放文件夹  
		 */
		public final String SORT_PRODUCT_PROPERTY = "productProperty";

		
		/**
		 * 店铺编辑器文件位置
		 */
		public final String SORT_EDITRO_PRODUCT = "product_editor";
		
		/**
		 * 会员-幸运一折购中奖-晒单图片存放文件夹
		 */
		public final String SORT_EFUN_EVALUATE="efun_evaluate";
		
		/**
		 * 会员 - 订单评价 - 晒单图片存放文件夹
		 */
		public final String SORT_ORDER_EVALUATE="order_evaluate";
		/**
		 * 页面幸运一折购图片 - 图片存放文件夹
		 */
		public final String SORT_EFUN = "efun";
		
		/**
		 * 手机幸运一折购图片 - 图片存在文件夹
		 */
		public final String SORT_EFUN_APP = "efunApp";
		/**
		 * pc礼品图片 - 图片存放文件夹
		 */
		public final String SORT_GIFT = "gift";
		/**
		 * App礼品图片 - 图片存放文件夹
		 */
		public final String SORT_GIFT_APP ="giftApp";
		
		/**
		 * 前台商品类目 - 类目图标存放文件夹
		 */
		public final String SORT_PRODUCT_FRONT_SORT = "productFrontSort";
		
		/**
		 * 公告编辑内容图片存放文件夹 
		 */
		public final String SORT_NOTICE_CONTENT = "notice_content_img";
		
		/**
		 * O2O云店店铺展示图片存放文件夹
		 */
		public final String SORT_O2O_STROE = "O2OStore";
		
		/**
		 * 类型：店铺模板图片存放文件夹
		 */
		public final String SHOP_TEMPLATE = "shopTemplate";
		
		/**
		 * 论坛帖子编辑内容图片存放文件夹 
		 */
		public final String SORT_BBS_TOPIC_CONTNET = "bbs_topic_content_img";
		
		/**
		 * 论坛帖子主题图片存放文件夹
		 */
		public final String SORT_BBS_TOPIC_COVER = "bbs_topic_cover_img";
		
		/**
		 * e趣学院编辑内容图片存放文件夹 
		 */
		public final String SORT_EFUN_COLLEGE_CONTENT = "efun_college_content_img";
		
		/**
		 * e趣学院主题图片存放文件夹
		 */
		public final String SORT_EFUN_COLLEGE_IMG = "efun_college_img";
		
		/**
		 * 帮助信息管理--编辑器图片存放文件夹
		 */
		public final String SORT_HELP_MESSAGE_IMG = "help_message_img";
		
		/**
		 * APP版本管理 - 安卓版本存放文件夹
		 */
		public final String SORT_APP_ANDROID = "app_android";
		
		/**
		 * 商家版APP版本管理 - 安卓版本存放文件夹
		 */
		public final String SORT_MAPP_ANDROID = "mapp_android";
		
		
    }
    /**
     * 文件存放路径
     * @author huangzq
     *
     */
    public interface UploadFileSort{
    	/**
    	 * 代理商资料
    	 */
    	public static final String SORT_UPLOADFILEPATH = "agent_file";
    	
    }
    /**
     * redis缓存
     * @author huangzq
     *
     */
    public interface Redis {
    	/**
    	 * PC商品分类
    	 */
    	public final String CACHE_PC_FRONT_SORT = "pc_front_sort";
    	/**
    	 * App商品分类
    	 */
    	public final String CACHE_APP_FRONT_SORT = "app_front_sort";
    	/**
    	 * PC商品前台三级分类搜索属性
    	 */
    	public final String CACHE_PC_SEARCH_PROPERTY = "pc_search_property";
    	/**
    	 * APP商品前台三级分类搜索属性
    	 */
    	public final String CACHE_APP_SEARCH_PROPERTY = "app_search_property";
    	/**
    	 * 短网址
    	 */
    	public final String CACHE_SHORT_URL = "short_url";
    	/**
    	 * PC session和app token  
    	 */
    	public final String CACHE_PC_APP_SESSION = "pc_app_session";
    	/**
    	 * 我的足迹
    	 */
    	public final String CACHE_FOOT_PRINT = "foot_print";
    	/**
    	 * 最近搜索
    	 */
    	public final String CACHE_RECENT_SEARCH_DATA = "recent_search_data";
    	/**
    	 * 其它数据
    	 */
    	public final String CACHE_OTHER_DATA = "other_data";
    	
    	/**
    	 * 购物车数量
    	 */
    	public final String CACHE_CART_COUNT = "cart_count";
    	
    	/**
    	 * 幸运一折购-购物车数量
    	 */
    	public final String CACHE_EFUN_CART_COUNT = "efun_cart_count";
    	/**
    	 * 幸运一折购数据
    	 */
    	public final String CACHE_EFUN_DATA = "efun_data";
    	
    	/**
    	 * 小神仙key前缀
    	 */
    	public final String KEY_XSX= "xsx_";
    	/**
    	 * 广告位前缀
    	 */
    	public final String KEY_AD= "ad_";
    	
    	/**
    	 * 会员猜你喜欢
    	 */
    	public final String KEY_GYL = "gyl_";
    	
    	/**
    	 * APP猜你喜欢
    	 */
    	public final String KEY_APP_GYL = "app_gyl_";
    	
    	/**
    	 * 24小时热卖商品
    	 */
    	public final String KEY_TFHS = "tfhs_";
    	
    	/**
    	 * PC-幸运一折购专区各类别奖区数据
    	 */
    	public final String KEY_PC_EFUN_PRIZE = "pc_efun_prize";
    	
    	/**
    	 * APP-幸运一折购专区各类别奖区数据
    	 */
    	public final String KEY_APP_EFUN_PRIZE = "app_efun_prize";
    	
    	/**
    	 * 提现金额限制前缀
    	 */
    	public final String KEY_CASH_WITHDRAW ="withdraw_";
    	
    }

	/**
	 * 所在地 
	 * @author Sylveon
	 */
	public interface Location {
		/**
		 * 省
		 */
		public static final String LOCATION_PROVINCE = "province";
		/**
		 * 市
		 */
		public static final String LOCATION_CITY = "city";
		/**
		 * 区
		 */
		public static final String LOCATION_AREA = "area";
	}

	/**
	 * 初始化参数
	 * @author huangzq
	 *
	 */
	public interface init{
		/**
		 * 登陆有效时间（毫秒）
		 */
		public static final long LOGIN_EFFECTIVETIME = 2 * 60 * 60 * 1000;
		/**
		 * 注册有效时间（毫秒）
		 */
		public static long REGISTER_EFFECTIVETIME = 10 * 60 * 1000;	
		/**
		 * 浏览记录失效时间(月份)，默认是1个月
		 */
		public static final int BROWSE_EXPIRE_MONTH = 1;
		/**
		 * 幸运一折购最大份数
		 */
		public static final int EFUN_MAX_NUM = 28;
		/**
		 * 幸运一折购每期每日截止购买时间
		 */
		public static final String EFUN_END_TIME = "14:30";
		/**
		 * 同个ip获取验证码最大的次数
		 */
		public static final int IP_MAX_COUNT = 5;
		/**
		 * 同个手机号码获取验证码的最大次数
		 */
		public static final int TEL_MAX_COUNT = 5;
		/**
		 * 发送短信时间间隔
		 */
		public static final long SMS_TIME_INTERVAL = 2 * 60 * 1000;
	
		
	}
	/**
	 * Pos接口
	 * @author huangzq
	 *
	 */
	public interface Pos{
		public static final String POS_DOMAIN="http://119.29.35.201/eq-pos/";
	}
	
	/**
	 * 商城平台
	 * @author Sylveon
	 */
	public interface DataFrom {
		/**
		 * 数据来源-PC
		 */
		public static final int PC = 1;
		/**
		 * 数据来源-APP
		 */
		public static final int APP = 2;
		/**
		 * 数据来源-线下云店
		 */
		public static final int OFFLINE = 3;
		/**
		 * 数据来源-WAP(扫一扫项目)
		 */
		public static final int WAP = 4;
		/**
		 * 数据来源-商家APP
		 */
		public static final int MAPP = 5;
		/**
		 * 数据来源-定时器
		 */
		public static final int TIMER = 6;
	}

	public enum DataSource {
		UNDEFINDED, PC, IOS, ANDROID, WAP, POS, MANAGER, TIMER
	}

}
