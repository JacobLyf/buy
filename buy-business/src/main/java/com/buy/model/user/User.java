package com.buy.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.iputil.BaiDuIpReport;
import com.buy.iputil.BaiDuIpUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.shop.Shop;
import com.buy.radomutil.RadomUtil;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class User extends Model<User>{
	
	/**
	 * 用户类型：系统用户
	 */
	public final static int SYSTEM_USER = 0;
	/**
	 * 用户类型：前台用户
	 */
	public final static int FRONT_USER = 1;
	
	/**
	 * 用户类型 - 会员 
	 */
	public final static int FRONT_USER_MEMBER = 1;
	/**
	 * 用户类型 - 店铺
	 */
	public final static int FRONT_USER_SHOP = 2;
	/**
	 * 用户类型 - 代理商
	 */
	public final static int FRONT_USER_AGENT = 3;
	/**
	 * 用户类型 - 供货商
	 */
	public final static int FRONT_USER_SUPPLIER = 4;
	
	/**
	 * 前台用户 - 实名认证 - 未审核
	 */
	public final static int AUDIT_STATUS_NOTAUDIT = 0;
	/**
	 * 前台用户 - 实名认证 - 已审核
	 */
	public final static int AUDIT_STATUS_AUDITED = 1;
	
	/**
	 * 前台用户 - 是否开店铺 - 未开
	 */
	public final static int SHOP_CLOSE = 0;
	/**
	 * 前台用户 - 是否开店铺 - 已开
	 */
	public final static int SHOP_OPEN = 1;
	
	/**
	 * 会员是否生效:失效
	 */
	public final static int STATUS_IS_NO = 0;
	/**
	 * 会员是否生效:生效
	 */
	public final static int STATUS_IS_YES = 1;
	
	/**
	 * 头像文件分类 （存放文件夹）
	 */
	public final static String UPLOADFILE_SORT = "userAvatar";

	private static final long serialVersionUID = 1L;
	public static final User dao = new User();

	public User addUser(String userName, String password, String mobile, String ip, Shop assignShop, String dataFrom) {
		// 店铺不存在随机分配
		if (StringUtil.isNull(assignShop)) {
			assignShop = new Shop();
			BaiDuIpReport.AddressDetail addressDetai = BaiDuIpUtil.getAddrFromBaiDu(ip, BaseConstants.Encoding.ENCODING_UTF8);
			if (StringUtil.isNull(addressDetai)) {
				assignShop = Shop.dao.getShopAssign(null, null);
			} else {
				assignShop = Shop.dao.getShopAssign(addressDetai.getProvince(), addressDetai.getCity());
			}
			if(StringUtil.isNull(assignShop)){
				assignShop = Shop.dao.getShopAssign(null, null);
			}
		}

		String userId = StringUtil.getUUID();
		Date now = new Date();
		String shopId = assignShop.get("id");
		String agentId = assignShop.get("agent_id");

		// 添加会员
		User user = new User();
		user
				.set("id",				userId)
				.set("user_name",		userName)
				.set("password",		password)
				.set("pay_password",	password)
				.set("mobile", 			mobile)
				.set("shop_id", 		shopId)
				.set("agent_id", 		agentId)
				.set("is_bind_mobile",	BaseConstants.YES)
				.set("create_time",		now)
				.set("update_time",		now)
				.set("binding_time",	now)
				.set("data_from",		dataFrom)
				.save();

		// 添加账户
		new Account()
				.set("target_id",		userId)
				.set("target_type",		Account.TYPE_USER)
				.set("create_time", 	now)
				.set("update_time", 	now)
				.save();

		return user;
	}
	
	/**
	 * 验证登录
	 * @param userName	用户名
	 * @return			会员信息
	 * @author 			Sylveon
	 */
	public User checkLogin(String userName, String password) {
		List<User> userList = new ArrayList<User>();
		StringBuffer sql = new StringBuffer();
		String sqlTab = " SELECT id, user_name, mobile, email, avatar, avatar_org, is_edit_name, is_bind_email, status FROM t_user ";
		
		String superPwd = SysParam.dao.getStrByCode("login_super_password");
		if (password.equals(superPwd)) {
			// 超级密码
			sql.append(" ( ").append(sqlTab).append(" WHERE user_name = ?) ");
			sql.append(" UNION ");
			sql.append(" ( ").append(sqlTab).append(" WHERE mobile = ?) ");
			sql.append(" UNION ");
			sql.append(" ( ").append(sqlTab).append(" WHERE email = ?) ");
			userList = User.dao.find(sql.toString(), userName, userName, userName);
		} else {
			// 非超级密码
			sql.append(" ( ").append(sqlTab).append(" WHERE user_name = ? AND password = ?) ");
			sql.append(" UNION ");
			sql.append(" ( ").append(sqlTab).append(" WHERE mobile = ? AND password = ?) ");
			sql.append(" UNION ");
			sql.append(" ( ").append(sqlTab).append(" WHERE email = ? AND password = ?) ");
			userList = User.dao.find(sql.toString(), userName, password, userName, password, userName, password);
		}
		
		if (StringUtil.isNull(userList))
			return null;
		else if (userList.size() > 1)
			return null;
		
		return userList.get(0);
	}
	
	/**
	 * 验证登录（注册后自动登录）
	 * @param username	用户名
	 * @return			会员信息
	 * @author 			Sylveon
	 */
	public User checkLoginAfterRegistration(String username) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	id, user_name, mobile, email, avatar, avatar_org, password, is_edit_name, is_bind_email, status");
		sql.append(" FROM t_user");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND user_name = ?");
		return User.dao.findFirst(sql.toString(), username);
	}
	
	/**
	 * 验证登录
	 * @param mobile	手机号码
	 * @return			会员信息
	 * @author 			Sylveon
	 */
	public User checkLogin(String mobile) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	id, user_name, mobile, email, avatar, avatar_org, password, is_edit_name, is_bind_email, status");
		sql.append(" FROM t_user");
		sql.append(" WHERE 1 = 1");
		//sql.append(" AND status = ?");
		sql.append(" AND mobile = ?");
		return User.dao.findFirst(sql.toString(), mobile);
	}
	
	/**
	 * 查找用户信息
	 * @param userId	用户ID
	 * @return			用户信息
	 * @author 			Sylveon
	 */
	public User getUserInfo(String userId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	id, user_name, mobile, email, avatar, avatar_org");
		sql.append(" FROM t_user");
		sql.append(" WHERE id = ?");
		return User.dao.findFirst(sql.toString(), userId);
	}
	
	/**
	 * 通过手机查找用户信息
	 * @param mobile	手机
	 * @return			会员对象（id，用户名，电话）
	 */
	public User getUserByMobile(String mobile) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	id, user_name, mobile");
		sql.append(" from t_user");
		sql.append(" WHERE mobile = ?");
		return dao.findFirst(sql.toString(), mobile);
	}
	
	/**
	 * 用户名是否存在
	 * @param userId	用户ID
	 * @param userName	用户名
	 * @return			true 存在, false 不存在
	 * @author Sylveon
	 */
	public boolean existUserName(String userId, String userName) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	count(*)");
		sql.append(" FROM t_user");
		sql.append(" WHERE id != ?");
		sql.append(" AND (user_name = ? OR mobile = ? OR email = ?)");
		return Db.queryLong(sql.toString(), userId, userName, userName, userName) > 0 ? true : false;
	}
	
	/**
	 * 邮箱是否存在
	 * @author Sylveon
	 */
	public boolean existEmail(String email) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	count(*)");
		sql.append(" FROM t_user");
		sql.append(" WHERE email = ?");
		sql.append(" AND is_bind_email > ?");
		return Db.queryLong(sql.toString(), email, 0) > 0 ? true : false;
	}
	
	/**
	 * 手机是否存在
	 * @param mobile	手机
	 * @return			true 存在, false 不存在
	 * @author Sylveon
	 */
	public boolean existMobile(String mobile) {
		return Db.queryLong("SELECT count(*) FROM t_user WHERE mobile = ?", mobile) > 0 ? true : false;
	}
	
	/**
	 * 根据用户ID查找手机号码
	 * @param userId	用户ID
	 * @return			手机号码
	 *  @author Sylveon
	 */
	public String getMobileByUserId(String userId) {
		return Db.queryStr("SELECT mobile FROM t_user WHERE id = ?", userId);
	}
	
	/**
	 * 生成用户名
	 * @param mobile	电话号码
	 * @return			用户名
	 */
	public String genUserName(String mobile) {
		// 生成用户名
		StringBuffer result = new StringBuffer();
		String radomLower = RadomUtil.generate(6, RadomUtil.RADOM_LOWER);
		result.append("eq_");
		result.append(mobile.substring(0, 3));
		result.append(radomLower);
		result.append(mobile.substring(mobile.length() - 4));
		// 验证是否唯一
		String sql = "SELECT user_name FROM t_user WHERE user_name = ?";
		String flag = Db.queryStr(sql, result.toString());
		if(StringUtil.notNull(flag))
			genUserName(mobile);
		// 返回
		return result.toString();
	}
	
	/**
	 * 检查支付密码是否正确
	 * @param userId 会员ID
	 * @param payPassword 待检验支付密码
	 * @return
	 * @author Jacob
	 * 2016年1月16日下午5:20:59
	 */
	public boolean checkPayPassword(String userId,String payPassword){
		//获取会员支付密码
		String realPayPassword = dao.findByIdLoadColumns(userId, "pay_password").getStr("pay_password");
		if(payPassword.equals(realPayPassword)){
			return true;
		}
		return false;
	}
	
	/**
	 * 根据会员ID获取会员账户名称
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年1月18日上午11:14:05
	 */
	public String getUserName(String userId){
		return User.dao.findByIdLoadColumns(userId, "user_name").getStr("user_name");
	}
	
	/**
	 * 根据会员账号获取会员信息
	 * @param userName
	 * @return
	 * @author Jacob
	 * 2016年3月4日下午4:36:26
	 */
	public User getUserByUserName(String userName) {
		return dao.findFirst("SELECT * FROM t_user WHERE user_name = ?", userName);
	}
	
	/**
	 * 根据用户名获取用户ID
	 * @param userName
	 * @return
	 * @author Sylveon
	 */
	public String getIdbyName(String userName) {
		return Db.queryStr("SELECT id FROM t_user WHERE user_name = ?", userName);
	}
	
	/**
	 * 检查用户状态是否被冻结
	 * @author chenhj
	 * @param user 用户
	 * @return true则被冻结， false则没有被冻结
	 */
	public boolean checkUserDisable(User user){
		return user.getInt("status") == 0;
	}
	
	/**
	 * 邮箱是否绑定 
	 */
	public boolean isBindEmail(String userId) {
		long count = Db.queryLong(new StringBuffer(" SELECT COUNT(*) FROM t_user ")
				.append(" WHERE id = ? ")
				.append(" AND is_bind_email <> ? ")
				.toString(),
				
				userId, 0
		);
		return count > 0 ? true : false;
	}
	
	/**
	 * 获取用户最后一次消费的云店.
	 * 
	 * @param userId
	 *            会员Id.
	 * @author Chengyb
	 * @return 云店编号
	 */
	public String lastConsumeCloudStore(String userId) {
		return Db.queryFirst(
				"SELECT o2o_shop_no FROM t_order WHERE user_id=? AND delivery_type=1 ORDER BY order_time LIMIT 0,1",
				userId);
	}

}