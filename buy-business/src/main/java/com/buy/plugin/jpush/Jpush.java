package com.buy.plugin.jpush;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.device.AliasDeviceListResult;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.push.model.notification.WinphoneNotification;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.report.ReceivedsResult;
import cn.jpush.api.report.ReceivedsResult.Received;

import com.buy.model.SysParam;
import com.buy.model.push.Push;
import com.buy.string.StringUtil;
import com.jfinal.log.Logger;

/**
 * 极光推送
 */
public class Jpush {
	
	static final Logger L = Logger.getLogger(Jpush.class);
	
	protected static final String KEY_APPKEY ="key";
	protected static final String KEY_MATER_SECRET = "secret";
	
	private static String iosSound = "sound.caf";		// 推送设备类型 - IOS - 通知提示声音
	private static int timeToLive = 86400;				// Options - 扩展字段 - 离线消息保留时长(秒)
	
	/**
	 * 推送平台
	 */
	public interface PushPlatform {
		/** ALL **/
		int ALL = 0;
		/** Android **/
		int ANDROID = 1;
		/** IOS **/
		int IOS = 2;
	}
	
	public static Push pushAll(int platform, String title, Push push) {
		Builder builder = PushPayload.newBuilder().setAudience(Audience.all());
		return pushByPlatform(platform, title, builder, push);
	}
	
	public static Push push4RegId(int platform, String regIds, String title, Push push) {
		Builder builder = PushPayload.newBuilder().setAudience(Audience.registrationId(regIds));
		return pushByPlatform(platform, title, builder, push);
	}
	
	public static Push push4RegId(int platform, List<String> regIds, String title, Push push) {
		Builder builder = PushPayload.newBuilder().setAudience(Audience.registrationId(regIds));
		return pushByPlatform(platform, title, builder, push);
	}
	
	public static Push push4Alias(int platform, String userId, String title, Push push) {
		Builder builder = PushPayload.newBuilder().setAudience(Audience.alias(userId));
		return pushByPlatform(platform, title, builder, push);
	}
	
	public static Push push4Alias(int platform, List<String> userIds, String title, Push push) {
		for(String s : userIds)
			L.info("推送会员ID：" + s);
		Builder builder = PushPayload.newBuilder().setAudience(Audience.alias(userIds));
		return pushByPlatform(platform, title, builder, push);
	}

	/**
	 * 推送
	 */
	static Push push(String title, Builder builder, Push push) {
    	boolean isApns = setIsApns();			// 设置APNs是否生产环境
    	
    	Integer jumpType = push.getInt("jump_type");
    	if (null == jumpType)
    	{
    		L.info("API类型 API");
    		jumpType = Push.JumpType.API;
    		push.set("jump_type", jumpType);
    	}
    	Integer jumpTo = push.getInt("jump_to");
    	if (null == jumpTo)
    	{
    		L.info("API类型 原生");
    		jumpTo = Push.JumpTo.APP_HOME;
    		push.set("jump_to", jumpTo);
    	}
    	String item = push.getStr("item");
    	HashMap<String, String> extras = getExtras(jumpType, jumpTo, item);
    	
    	// 配置推送参数
    	PushPayload payload = builder
        		// 推送平台
        		.setPlatform(Platform.all())
                
                // 推送通知内容体
                .setNotification(Notification.newBuilder()
                		// 推送通知内容
                		.setAlert(push.getStr("content"))
                		
                		// Android
                		.addPlatformNotification(AndroidNotification.newBuilder()
                				.setTitle(title)				// 通知标题
//                				.setBuilderId(builderId)		// 通知栏样式ID
                				.addExtras(extras)				// 扩展字段
                				.build())
                				
                		// IOS
                		.addPlatformNotification(IosNotification.newBuilder()
                				.setSound(iosSound)				// 通知提示声音
//                				.setBadge(1)					// 应用角标/，默认1
//                				.setContentAvailable(false)		// 推送唤醒
//                				.setCategory("")				// APNs中的category字段值（支持IOS8）
                				.addExtras(extras)				// 扩展字段
                				.build())
                				
                		// WinPhone样式
                		.addPlatformNotification(WinphoneNotification.newBuilder()
                				.setTitle(title)				// 通知标题
//                				.setOpenPage(null)				// 点击打开的页面名称
                				.addExtras(extras)				// 扩展字段
                				.build())
                	.build())
                	
                // 推送消息内容体
//              .setMessage(Message.newBuilder()
//            		   .setTitle("")							// 消息标题
//            		   .setContentType("")						// 消息内容类型
//            		   .setMsgContent("")						// 消息内容本身
//            		   .addExtras(extras)						// 扩展字段
//            		   .build())
                	
                // 推送参数
                .setOptions(Options.newBuilder()
//                		.setSendno(0)							// 推送序号
                		.setTimeToLive(timeToLive)				// 离线消息保留时长(秒)
//                		.setOverrideMsgId(0L)					// 要覆盖的消息ID
                		.setApnsProduction(isApns)				// APNs是否生产环境
//                		.setBigPushDuration(0)					// 定速推送时长(分钟
                		.build())
                		
        	.build();
        
    	// 推送
        return mainPush(payload, push);
    }
    
    /**
	 * 推送 - ios
	 */
    static Push push4Ios(String title, Builder builder, Push push) {
    	boolean isApns = setIsApns();			// 设置APNs是否生产环境
    	
    	Integer jumpType = push.getInt("jump_type");
    	if (null == jumpType)
    	{
    		jumpType = Push.JumpType.API;
    		push.set("jump_type", jumpType);
    	}
    	Integer jumpTo = push.getInt("jump_to");
    	if (null == jumpTo)
    	{
    		jumpTo = Push.JumpTo.APP_HOME;
    		push.set("jump_to", jumpTo);
    	}
    	String item = push.getStr("item");
    	HashMap<String, String> extras = getExtras(jumpType, jumpTo, item);
    	
    	// 配置推送参数
    	PushPayload payload = builder
        		// 推送平台
        		.setPlatform(Platform.ios())
                
                // 推送通知内容体
                .setNotification(Notification.newBuilder()
                		// 推送通知内容
                		.setAlert(push.getStr("content"))
                				
                		// IOS
                		.addPlatformNotification(IosNotification.newBuilder()
                				.setSound(iosSound)	// 通知提示声音
                				.addExtras(extras)	// 扩展字段
                				.build())
                				
                	.build())
                	
                // 推送参数
                .setOptions(Options.newBuilder()
                		.setTimeToLive(timeToLive)	// 离线消息保留时长(秒)
                		.setApnsProduction(isApns)	// APNs是否生产环境
                		.build())
                		
        	.build();
        
    	// 推送
        return mainPush(payload, push);
    }
    
    /**
	 * 推送 - Android
	 */
    static Push push4Android(String title, Builder builder, Push push) {
    	Integer jumpType = push.getInt("jump_type");
    	if (null == jumpType)
    	{
    		jumpType = Push.JumpType.API;
    		push.set("jump_type", jumpType);
    	}
    	Integer jumpTo = push.getInt("jump_to");
    	if (null == jumpTo)
    	{
    		jumpTo = Push.JumpTo.APP_HOME;
    		push.set("jump_to", jumpTo);
    	}
    	String item = push.getStr("item");
    	HashMap<String, String> extras = getExtras(jumpType, jumpTo, item);
    	
    	// 配置推送参数
    	PushPayload payload = builder
        		// 推送平台
        		.setPlatform(Platform.android())
                
                // 推送通知内容体
                .setNotification(Notification.newBuilder()
                		// 推送通知内容
                		.setAlert(push.getStr("content"))
                		
                		// Android
                		.addPlatformNotification(AndroidNotification.newBuilder()
                				.setTitle(title)		// 通知标题
                				.addExtras(extras)		// 扩展字段
                				.build())
                				
                	.build())
                	
                // 推送参数
                .setOptions(Options.newBuilder()
                		.setTimeToLive(timeToLive)		// 离线消息保留时长(秒)
                		.build())
                		
        	.build();
    		
        return mainPush(payload, push);
    }
    
    /**
	 * 推送 - 根据平台
	 */
    public static Push pushByPlatform(int platform, String title, Builder builder, Push push) {
    	switch (platform) {
			case PushPlatform.ALL:
				L.info("所有平台推送");
				push.set("platform", PushPlatform.ALL);
				return push(title, builder, push);
				
			case PushPlatform.IOS:
				L.info("IOS推送");
				push.set("platform", PushPlatform.IOS);
				return push4Ios(title,  builder, push);
				
			case PushPlatform.ANDROID:
				L.info("Android推送");
				push.set("platform", PushPlatform.ANDROID);
				return push4Android(title, builder, push);

			default:
				L.info("默认推送推送");
				return push(title, builder, push);
		}
    }
    
    public static boolean checkRegistrationId(String registrationId) {
    	try {
    		HashMap<String, String> info = getPushAccount();
    		JPushClient jpushClient = new JPushClient(info.get(KEY_MATER_SECRET), info.get(KEY_APPKEY));
    		jpushClient.getDeviceTagAlias(registrationId);
			return true;
		} catch (Exception e) {
			return false;
		}
    }
    
    static String getAlias() {
    	HashMap<String, String> info = getPushAccount();
		JPushClient jpushClient = new JPushClient(info.get(KEY_MATER_SECRET), info.get(KEY_APPKEY));
    	AliasDeviceListResult result = null;
    	try {
			result = jpushClient.getAliasDeviceList("774c56e1117046f782f8e9f3d19a6f07", null);
			System.out.println(result);
		} catch (APIConnectionException | APIRequestException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public static void main(String[] args) {
    	getAlias();
	}
    
    
    /**
     * 推送主方法
     */
    private static Push mainPush(PushPayload payload, Push push) {
    	HashMap<String, String> info = getPushAccount();
		JPushClient jpushClient = new JPushClient(info.get(KEY_MATER_SECRET), info.get(KEY_APPKEY));
        PushResult pushResult = new PushResult();
        
        try {
        	pushResult = jpushClient.sendPush(payload);
        	
        	return push
        		.set("msg_id", 		pushResult.msg_id + "")
        		.set("sendno", 		pushResult.sendno + "");
		}  catch (APIConnectionException e) {
    		L.error("Connection error, should retry later", e);
    		return push.set("error_msg", "推送连接错误");
    	} catch (APIRequestException e) {
    		L.error("Should review the error, and fix the request", e);
            L.error("HTTP Status: " + e.getStatus());
            L.error("Error Code: " + e.getErrorCode());
            L.error("Error Message: " + e.getErrorMessage());
            
            StringBuffer error = new StringBuffer();
            error.append("HTTP Status:" + e.getStatus());
            error.append(", Error Code:" + e.getErrorCode());
            error.append(", Error Message:" + e.getErrorMessage());
            return push.set("error_msg", error.toString());
		}
    }
    
    /**
     * 推送送达
     * @param msgIds
     * @return
     */
    public static List<Push> received(String msgIds) {
    	HashMap<String, String> info = getPushAccount();
		JPushClient jpushClient = new JPushClient(info.get(KEY_MATER_SECRET), info.get(KEY_APPKEY));
    			
    	// 推送结果
    	List<Push> pushList = new ArrayList<Push>();
    	ReceivedsResult receivedsResult;
		try {
			receivedsResult = jpushClient.getReportReceiveds(msgIds);
			List<Received> receivedList = receivedsResult.received_list;
			
			for (Received r : receivedList) {
				Push push = new Push();
				push.set("msg_id", r.msg_id + "").set("to_count", r.android_received + r.ios_apns_sent);
				pushList.add(push);
			}
		} catch (APIConnectionException e) {
			L.error("Connection error, should retry later", e);
		}
		catch (APIRequestException e) {
			L.error("Should review the error, and fix the request", e);
            L.error("HTTP Status: " + e.getStatus());
            L.error("Error Code: " + e.getErrorCode());
            L.error("Error Message: " + e.getErrorMessage());
            
            StringBuffer error = new StringBuffer();
            error.append("HTTP Status:" + e.getStatus());
            error.append(", Error Code:" + e.getErrorCode());
            error.append(", Error Message:" + e.getErrorMessage());
		}
		return pushList;
    }
    
    /**
	 * 设置扩展参数
	 */
	public static HashMap<String, String> getExtras(int jumpType, int jumpTo, String item) {
		HashMap<String, String> extras = new HashMap<String, String>();
		extras.put("jumpType",	jumpType + "");	// 跳转类型
		extras.put("jumpTo",	jumpTo + "");	// 跳转位置
		extras.put("jumpInfo",	item);			// 跳转类型的信息
		return extras;
	}
	
	/**
	 * 设置APNs是否生产环境
	 */
	static boolean setIsApns() {
    	Integer isApns = SysParam.dao.getIntByCode("push_isApns");
    	if (StringUtil.isNull(isApns))
    		return false;
    	else if (1 == isApns)
    		return true;
    	else
    		return false;
	}
	
	static HashMap<String, String> getPushAccount() {
		// TEST
		HashMap<String, String> result = new HashMap<String, String>();
		result.put(KEY_APPKEY, "239a2016e7bd11b50e36c3cd");
		result.put(KEY_MATER_SECRET, "8e60fbfc1b43fb55d4c3231f");
		
		Integer isPush = SysParam.dao.getIntByCode("is_push");
		if (StringUtil.notNull(isPush) && 1 == isPush) {
			// REAL
			result.put(KEY_APPKEY, "750cffe161b85e66bafe9c7e");
			result.put(KEY_MATER_SECRET, "2d8953f317f589ccfb665806");
		}
		
		return result;
	}
	
}
