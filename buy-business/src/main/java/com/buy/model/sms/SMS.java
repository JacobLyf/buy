package com.buy.model.sms;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.auCodeRecord.AuCodeRecord;
import com.buy.plugin.sms.SMSUtil;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 *  Model - 短信
 */
public class SMS extends Model<SMS> {
	
	/** 短信前缀名，用于记录数据 */
	public final static String SMS_PREFIX = "【e趣商城】";

	/** 发短信状态 - 发送失败 */
	public final static int STATUS_FAIL  = 0;
	/** 发短信状态 - 发送成功 */
	public final static int STATUS_SUCCESS = 1;
	/** 发短信状态 - 发送中 */
	public final static int STATUS_SENDING = 1;
	
	/** 发短信状态 - 当前ip地址发送验证码频繁 */
	public final static int STATUS_IP_MAX = 2;
	/** 发短信状态 - 当前手机号码发送验证码频繁 */
	public final static int STATUS_MOBILE_MAX = 3;
	
	/** 密码类型 - 登录 */
	public final static int PASSWORD_TYPE_LGOIN = 1;
	/** 密码类型 - 支付 */
	public final static int PASSWORD_TYPE_PAY = 2;
	
	/** 验证码有效期 */
	public final static int EXPIRETIME_TIME = 10 * 60;	// 10min
	/** 验证码发送间隔 */
	public final static int INTERVAL_TIME = 2 * 60;		// 2min
	/** 验证码有效期标识 - 用于redis*/
	public final static String MARK_EXPIRE_TIME = "smsExpire_";
	/** 验证码发送间隔标识 - 用于redis */
	public final static String MARK_INTERVAL_TIME = "smsIntercal_";
	
	private static final long serialVersionUID = 1L;
	public static final SMS dao = new SMS();

	/** 短信平台 - 一信通 **/
	public static final int SMS_FROM_UMS86 = 1;
	/** 短信平台 - 巨辰科技 **/
	public static final int SMS_FROM_QYBOR = 2;
	
	
	/**
	 * 初始化
	 * @param mobile	手机号码
	 * @param content	发送短信内容
	 * @param code		短信类型编码
	 * @param checkCode	验证码
	 * @param remark	备注
	 * @return			短信对象
	 * @author Sylveon
	 */
	public SMS init(String mobile, String content, String code, String checkCode, String remark, int dataFrom) {
		return new SMS()
			.set("telephone",		mobile)
			.set("content", 		content)
			.set("check_code",		checkCode)
			.set("template_code",	code)
			.set("remark",			remark)
			.set("data_from", 		dataFrom)
			.set("create_time",		new Date());
	}
	
	/**
	 * 发送注册短信
	 * @param mobile	手机号码
	 * @param checkCode	验证码
	 * @author Sylveon
	 * @return			发短信结果（key 返回状态值（APP用），msg 提示）
	 */
	public HashMap<String, Object> sendRegistSms(String mobile, String checkCode, String ip, int dataFrom) {
		// 获取验证码的资格
		int status = SMS.dao.checkIsLegal(ip, mobile);
		if(status == STATUS_SUCCESS) {
			// 获取资格成功，并发送短信
			String[] datas = new String[]{checkCode};
			status = sendSMS(SmsAndMsgTemplate.SMS_REGISTER_CODE, datas, mobile, checkCode, mobile + "申请会员注册", dataFrom);
			AuCodeRecord.dao.saveAuCodeRecord(ip, mobile, checkCode);	// 保存验证码请求记录
		}
		HashMap<String, Object> map = dealSmsCodeResult(status);
		return map;
	}
	
	/**
	 * 发送通用验证码短信
	 * @param mobile	手机号码
	 * @param code		验证码
	 * @param minute	有效期分钟
	 * @param ip		IP地址
	 * @param remark	备注
	 * @return
	 */
	public HashMap<String, Object> sendCheckCodeSms(String mobile, String code, long minute, String ip, String remark, int dataFrom) {
		// 获取验证码的资格
		int status = SMS.dao.checkIsLegal(ip, mobile);
		if(status == STATUS_SUCCESS) {
			// 获取资格成功，并发送短信
			String[] datas = {code,minute+ ""};
			status = sendSMS(SmsAndMsgTemplate.SMS_CODE, datas, mobile, code, remark, dataFrom);
			AuCodeRecord.dao.saveAuCodeRecord(ip, mobile, code);	// 保存验证码请求记录
		}
		return dealSmsCodeResult(status);
	}
	
	/**
	 * 发送单条短信并记录短信
	 * @param mobile	手机号码
	 * @param content	发送内容
	 * @param code		发送类型编码
	 * @param checkCode	验证码
	 * @param remark	备注
	 * @return			发送结果
	 * @author Sylveon
	 */
	private int sendSmsAndSaveRecord(String mobile, String content, String code, String checkCode, String remark, int dataFrom) {
		// 设置信息属性
		SMS sms = dao.init(mobile, content, code, checkCode, remark, dataFrom);
		// 发送信息
		int statusCode = 0;
		try {
			Integer isSendSms = SysParam.dao.getIntByCode("is_sendSms");
			//判断是否发短信，1为发，0为否
			if(1==isSendSms){
				statusCode = SMSUtil.sendSMS(mobile, content, ""); 
			}
			if(0 == statusCode) {
				// 发送成功
				sms.set("status", SMS.STATUS_SUCCESS).set("status_code", statusCode);
			} else {
				// 发送失败
				sms.set("status", SMS.STATUS_FAIL).set("status_code", statusCode);
			}
		} catch (Exception e) {
			// MalformedURLException | UnsupportedEncodingException e
			// 发送失败并捕捉异常，以记录短信发送的内容及状态，不需要回滚
			e.printStackTrace();
			sms.set("status", SMS.STATUS_FAIL).set("status_code", statusCode);
			return STATUS_FAIL;
		}
		// 添加
		sms.save();
		return 0 == statusCode ? STATUS_SUCCESS : STATUS_FAIL;
	}
	
	/**
	 * 发送单条短信并记录短信
	 * @param smsList	消息记录集合
	 * @author Sylveon
	 */
	public void sendSmsAndSaveRecord(List<SMS> smsList, String template, int dataFrom) {
		for (SMS sms : smsList) {
			sms.set("data_from", dataFrom);
			
			String mobile = sms.getStr("telephone");
			String content = sms.getStr("content");
			
			// 发送信息
			int statusCode = 0;
			try {
				Integer isSendSms = SysParam.dao.getIntByCode("is_sendSms");
				//判断是否发短信，1为发，0为否
				if(1==isSendSms){
					statusCode = SMSUtil.sendSMS(mobile, content, ""); 
				}
				if(0 == statusCode) {
					// 发送成功
					sms.set("status", SMS.STATUS_SUCCESS).set("status_code", statusCode).set("template_code", template);
				} else {
					// 发送失败
					sms.set("status", SMS.STATUS_FAIL).set("status_code", statusCode).set("template_code", template);
				}
			} catch (Exception e) {
				// MalformedURLException | UnsupportedEncodingException e
				// 发送失败并捕捉异常，以记录短信发送的内容及状态，不需要回滚
				e.printStackTrace();
				sms.set("status", SMS.STATUS_FAIL).set("status_code", statusCode);
			}
		}
		
		String columns = "telephone, content, status, status_code, template_code, remark, create_time, data_from";
		String add = "INSERT INTO t_sms_record (" + columns + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		if (StringUtil.notNull(smsList) && smsList.size() > 0)
			Db.batch(add, columns, smsList, 100);
	}
	
	/**
	 * 批量发送短信并记录短信（短信内容一致）
	 * @param telephones
	 * @param content
	 * @param type
	 * @param authCode
	 * @param remark
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月22日下午8:20:39
	 */
	public void batchSendSmsAndSaveRecord(String [] telephones,String content,int type,String authCode,String remark, int dataFrom){
		int status = SMS.STATUS_SUCCESS;
		String failList = null;
		
		// 发送信息
		try {
			Map<String,String> resultMap = SMSUtil.batchSendSMS(telephones, content, "");
			int result = Integer.parseInt(resultMap.get("result"));
			failList = resultMap.get("faillist");//失败的号码列表
			if(0 != result) {
				// 发送失败
				status = SMS.STATUS_FAIL;
			}
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			// 发送失败并捕捉异常，以记录短信发送的内容及状态，不需要回滚
			e.printStackTrace();
			status = SMS.STATUS_FAIL;
		}
		
		//短信记录插入列表
		Object[][] params = new Object[telephones.length][];
		
		//插入sql
		String insertSql = "INSERT INTO t_sms_record (telephone,content,type_id,code,remark,create_time,status,data_from)  VALUES (?,?,?,?,?,now(),?)";
		
		//遍历传参
		for(int i = 0;i<telephones.length;i++){
			List<Object> childParamList = new ArrayList<Object>();
			// 设置属性
			childParamList.add(telephones[i]);
			childParamList.add(content);
			childParamList.add(type);
			childParamList.add(authCode);
			childParamList.add(remark);
			childParamList.add(dataFrom);
			
			if(null!=failList){
				if(failList.contains(telephones[i])){
					status = SMS.STATUS_FAIL;
				}else{
					status = SMS.STATUS_SUCCESS;
				}
			}
			childParamList.add(status);
			params[i] = childParamList.toArray();
		}
		Db.batch(insertSql, params, 50);
	}
	
	/**
	 * 批量发送短信并记录短信（短信内容一致）
	 * @param telephones
	 * @param sms
	 * @author Sylveon
	 */
	public void batchSendSmsAndSaveRecord(String[] telephones, SMS sms, int dataFrom) {
		
		Integer isSend = SysParam.dao.getIntByCode("is_sendSms");
		if (null == isSend)
			isSend = 0;
		int status = SMS.STATUS_SUCCESS;
		String failList = null;
		
		// 发送信息
		try {
			if (1 == isSend) {
				Map<String,String> resultMap = SMSUtil.batchSendSMS(telephones, sms.getStr("content"), "");
				failList = resultMap.get("faillist");//失败的号码列表
			}
			
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			// 发送失败并捕捉异常，以记录短信发送的内容及状态，不需要回滚
			e.printStackTrace();
			status = SMS.STATUS_FAIL;
		} finally {
			String remark = sms.getStr("remark");
			remark = StringUtil.isNull(remark) ? "" : remark;
			String mobiles = "";
			
			// 添加短信记录
			if (SMS.STATUS_FAIL == status) {
				// 发送短信失败
				remark = "\n失败名单：" + StringUtil.arrayToString(",", telephones);
				sms
					.set("status", status)
					.set("remark", remark)
					.set("create_time", new Date())
					.set("data_from", dataFrom)
					.save();
			} else {
				// 发送短信成功，记录失败名单
				if (StringUtil.notBlank(failList)) {
					for(int i = 0; i < telephones.length; i++) {
						if(failList.contains(telephones[i]))
							mobiles += telephones[i] + ",";
					}
					// 失败名单
					int len = mobiles.length();
					if (len > 0) {
						remark += "\n失败名单：" + mobiles.substring(0, len - 1);
						remark += mobiles;
					}
				}
				
				sms
					.set("status", SMS.STATUS_SUCCESS)
					.set("remark", remark)
					.set("create_time", new Date())
					.save();
			}
		}
	}
	
	/**
	 * 批量发送短信(短信内容可不同）
	 * @param telephones
	 * @param content
	 * @param type
	 * @param authCode
	 * @param remark
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws MalformedURLException 
	 * @throws
	 * @author Eriol
	 * @date 2015年7月22日下午8:20:39
	 */
	public List<SMS> batchSendSms(List<SMS> smses, int dataFrom) throws MalformedURLException, UnsupportedEncodingException{
		// 发送信息
		for(SMS s:smses){
			s.set("data_from", dataFrom);
			
			String telephone = s.get("telephone");
			String content = s.get("content");
			int p = content.indexOf(SMS.SMS_PREFIX);
			content = content.substring(p+SMS.SMS_PREFIX.length(),content.length());
			Integer isSendSms = SysParam.dao.getIntByCode("is_sendSms");
			long result = 0;
			//判断是否发短信，1为发，0为否
			if(1==isSendSms){
				result = SMSUtil.sendSMS(telephone, content, "");
			}
			if(0 != result) {
				// 发送失败
				s.set("status",SMS.STATUS_FAIL);
			}else{
				s.set("status",SMS.STATUS_SUCCESS);
			}
		}
		return smses;
	}
	
	/**
	 * 判断是ip或者手机号码是否符合获取验证码的资格
	 * @param ip
	 * @param telephone
	 * @return map中status（1：合法，2或3则不合法）
	 * @author Eriol
	 * @date 2015年7月30日上午10:50:52
	 */
	public int checkIsLegal(String ip, String telephone) {
		String legalTime = DateUtil.DateToString(DateUtil.addDay(new Date(),-1),DateStyle.YYYY_MM_DD_HH_MM_SS);
		// 获取ip在24小时内的访问次数
		/*long ipCount = AuCodeRecord.dao.getReqCountByIp(ip, legalTime, telephone);
		if(ipCount >= BaseConstants.init.IP_MAX_COUNT)
			return STATUS_IP_MAX;*/
		// 获取手机在24小时内的访问次数
		long telCount = AuCodeRecord.dao.getReqCountByMobile(telephone, legalTime);
		if(telCount >= BaseConstants.init.TEL_MAX_COUNT)
			return STATUS_MOBILE_MAX;
		// 成功
		return STATUS_SUCCESS;
	}
	
	/**
	 * 设置验证码有效时间（毫秒）
	 * @param 	expireTime	有效时间
	 * @return	发短信有效时间
	 * @author 	Sylveon
	 */
	public Date setCheckCodeExpireTime(long expireTime) {
		return DateTime.now().plus(expireTime).toDate();
	}
	
	/**
	 * 设置发短信有效时间（毫秒）
	 * @return	发短信有效时间
	 * @author 	Sylveon
	 */
	public Date setSmsExpireTime() {
		return DateTime.now().plus(BaseConstants.init.SMS_TIME_INTERVAL).toDate();
	}
	
	/**
	 * 处理验证码短信结果
	 * @param status	状态值
	 * @return			结果集合
	 * @author 	Sylveon
	 */
	private HashMap<String, Object> dealSmsCodeResult(int status) {
		HashMap<String, Object> map = new HashMap<>();
		switch (status) {
		case STATUS_SUCCESS:
			map.put("key", 0);
			map.put("msg", "发送成功");
			break;
		case STATUS_FAIL:
			map.put("key", 1);
			map.put("msg", "发送失败");
			break;
		case STATUS_IP_MAX:
			map.put("key", 2);
			map.put("msg", "当前ip地址发送验证码次数已满");
			break;
		case STATUS_MOBILE_MAX:
			map.put("key", 3);
			map.put("msg", "当前手机号码发送验证码次数已满");
			break;
		}
		return map;
	}
	
	/**
	 * 获取密码类型（String）
	 * @param passwordType	密码类型
	 * @return				密码类型字符串
	 * @author 	Sylveon
	 */
	public static String getPasswordTypeStr(int passwordType) {
		switch (passwordType) {
		case SMS.PASSWORD_TYPE_LGOIN:
			return "登录密码";
		case SMS.PASSWORD_TYPE_PAY:
			return "支付密码";
		default:
			return null;
		}
	}
	
	/**
	 * 初始化发送短信
	 * @param smsCode
	 * @param datas
	 * @param mobile
	 * @param checkCode
	 * @param remark
	 * @return
	 * @author Sylveon
	 */
	public Ret initSendSMS(String smsCode,String[] datas,String mobile,String checkCode,String remark) {
		return new Ret()
			.put("smsCode", smsCode)
			.put("datas", datas)
			.put("mobile", mobile)
			.put("checkCode", checkCode)
			.put("remark", remark)
		;
	}
	
	/**
	 * 发送短信并记录短信记录
	 * @param smsType 短信类型
	 * @param datas  //替换的内容，按顺序插入
	 * @param mobile
	 * @param code
	 * @param remark
	 * @throws
	 * @author Eriol
	 * @date 2015年11月19日下午7:12:40
	 */
	public int sendSMS(String smsCode, String[] datas, String mobile, String checkCode, String remark, int dataFrom){
		String content = SmsAndMsgTemplate.dao.getContentByType(smsCode);							// 短信内容
		content = SMSContent.dealSmsContent(content, datas);										// 处理短信内容替换为业务内容
		return SMS.dao.sendSmsAndSaveRecord(mobile, content, smsCode, checkCode, remark, dataFrom);	// 发送并记录短信
	}
	
	/**
	 * 重新发送短信
	 */
	public int updateSmsByResend(int id, String mobile, String content) {
		SMS sms = SMS.dao.findFirst("SELECT * FROM t_sms_record WHERE id = ? FOR UPDATE", id)
				.set("telephone", mobile).set("content", content);
		// 发送信息
		int statusCode = 0;
		try {
			Integer isSendSms = SysParam.dao.getIntByCode("is_sendSms");
			//判断是否发短信，1为发，0为否
			if(1==isSendSms){
				statusCode = SMSUtil.sendSMS(mobile, content, ""); 
			}
			if(0 == statusCode) {
				// 发送成功
				sms.set("status", SMS.STATUS_SUCCESS).set("status_code", statusCode);
			} else {
				// 发送失败
				sms.set("status", SMS.STATUS_FAIL).set("status_code", statusCode);
			}
		} catch (Exception e) {
			// MalformedURLException | UnsupportedEncodingException e
			// 发送失败并捕捉异常，以记录短信发送的内容及状态，不需要回滚
			e.printStackTrace();
			sms.set("status", SMS.STATUS_FAIL).set("status_code", statusCode);
			return STATUS_FAIL;
		}
		// 添加
		sms.update();
		return 0 == statusCode ? STATUS_SUCCESS : STATUS_FAIL;
	}
	
	public static void main(String[] args) {
		
		//SMS.dao.sendRegistSms("12345678901", "ohayo", "192.168.1.1");
//		SMS.dao.batchSendSmsAndSaveRecord(new String[]{"13430271786","13430271777"}, "test", 1, "", "");
	}
	
}
