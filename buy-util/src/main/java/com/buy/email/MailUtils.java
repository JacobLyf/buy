package com.buy.email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailUtils {

	private Logger maiLogger = LoggerFactory.getLogger(getClass());

	Properties mailProperties = null;
/*	Resource mailProperty = null;
	Map<String, Resource> templates = null;*/
	Map<String, String> template = null;

	/**
	 * 
	 * 方法说明：获取邮件的信息
	 * 
	 * @author wangyi
	 * @return Resource
	 */
	/*public Resource getMailProperty() {
		return mailProperty;
	}*/

	/***
	 * 
	 * 方法说明：设置邮件的信息
	 * 
	 * @author wangyi
	 * @param mailProperty
	 *            void
	 */
	/*public void setMailProperty(Resource mailProperty) {
		this.mailProperty = mailProperty;

		try {
			mailProperties = new Properties();
			mailProperties.load(mailProperty.getInputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}*/

	public static void main(String[] args) {
		/*
		 * MailUtils mu = new MailUtils(); String[][] bodyReplace = new
		 * String[2][2]; bodyReplace[0][0] = "#title#"; bodyReplace[0][1] =
		 * "uniasia";
		 * 
		 * bodyReplace[1][0] = "#content#"; bodyReplace[1][1] =
		 * "sdhfsdjfhsdjkfhsdjf";
		 */
		// if (bodyReplace == null) {
		// System.out.println("is null");
		// } else {
		// System.out.println("not null");
		// }
		// System.out.println(mu.buildMail(bodyReplace));
		// mu.sendMail(mu.buildMail(bodyReplace), true);
		// System.out.println(mu.buildMail(bodyReplace));
		// mu.sendMail(mu.buildMail(bodyReplace), true);
		/*
		 * SSOClient sso = new SSOClient().createClient(); UserInfo userInfo =
		 * new UserInfo(); userInfo.setUserId(2l);
		 * userInfo.setUserName("admin");
		 * userInfo.setPassword(StringUtil.encryptSha256("111111"));
		 * userInfo.setNick("管理员"); userInfo.setEmail("123@123.com");
		 * userInfo.setMobile("15865412320"); sso.registeSSOUser(userInfo);
		 * System.out.println("over~");
		 
		UserInfo u = new UserInfo();
		u.setUserName("xiao4");
		u.setPassword(StringUtil.encryptSha256("uniasia"));
		u.setEmail("123@123.com");
		u.setUserId(1l);
		SSOClient sso = SSOClient.createClient();*/
		/*sso.registeSSOUser(u);
		System.out.println("~~");*/
	}

	/**
	 * 
	 * 方法说明：通过邮件读取文件的字符流
	 * 
	 * @author xiaolong
	 * @param resource
	 * @return String
	 */
	/*public String readString(Resource resource) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "utf-8"));
			StringBuffer b = new StringBuffer();
			String aline = null;
			while ((aline = reader.readLine()) != null) {
				b.append(aline).append("\r\n");
			}
			return b.toString();
		} catch (Exception ex) {
			return "";
		}
	}*/

	/**
	 * 
	 * 方法说明：通过路径读取文件的字符流
	 * 
	 * @author wangyi
	 * @param path
	 * @return String
	 */
	public String readString(String path) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), "utf-8"));
			StringBuffer b = new StringBuffer();
			String aline = null;
			while (null != (aline = reader.readLine())) {
				b.append(aline).append("\r\n");
			}
			return b.toString();
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * 
	 * 方法说明：
	 * 
	 * @author wangyi
	 * @param bodyReplace
	 *            二维数组：0阶数组保存要替换的标识，1阶数组保存要替换的位置
	 * @return String
	 */
	public String buildMail(String[][] bodyReplace, String template) {
		String body = readString(template); // 模板位置和名称
		if (null != bodyReplace) {
			for (int i = 0; i < bodyReplace.length; i++) {
				body = body.replaceAll(bodyReplace[i][0], bodyReplace[i][1]);
			}
		}
		return body;
	}

	/***
	 * 
	 * 方法说明：发送邮件主体
	 * 
	 * @author wangyi
	 * @param content
	 * @param isLog
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean sendMail(String content, HtmlEmail email) {
		try {
			email.setHostName("smtp.ym.163.com");
			email.setAuthentication("qihua@keywa.com", "keywa1234");
			email.setFrom("qihua@keywa.com", "奇化网");
			email.setSubject("奇化网");
			email.setHtmlMsg(content);
			email.setCharset("utf-8");
			email.buildMimeMessage();
			email.sendMimeMessage();
		} catch (Exception ex) {
			maiLogger.error("邮件发送失败!错误信息:", ex);
			return false;
		}
		return true;
	}
}
