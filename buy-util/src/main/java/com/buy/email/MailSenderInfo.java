package com.buy.email;

import java.util.Properties;

import com.buy.common.Ret;

public class MailSenderInfo {
	 
	private String mailServerHost;			// 发送邮件的服务器的IP(或主机地址)
	private String mailServerPort = "25";	// 发送邮件的服务器的端口
	private String fromAddress;				// 发件人邮箱地址
	private String toAddress;				// 收件人邮箱地址
	private String userName;				// 登陆邮件发送服务器的账号
	private String password;				// 登陆邮件发送服务器的密码
	private boolean validate = true;		// 是否需要身份验证
	private String subject;					// 邮件主题
	private String content;	 				// 邮件的文本内容
	private String[] attachFileNames;		// 邮件附件的文件名
	
	/**
	 * 构造
	 */
	public MailSenderInfo() {
		
	}
	
	/**
	 * 邮件信息构造带参 
	 * @param toAddress	用户邮箱
	 * @param subject	标题
	 * @param content	内容
	 */
	public MailSenderInfo(String toAddress, String subject, String content) {
		this.mailServerHost = "smtp.qiye.163.com";
		this.mailServerPort = "25";
		this.validate = true;
		this.fromAddress = "";
		this.userName = "";
		this.password = "";
		
		this.toAddress = toAddress;
		this.subject = subject;
		this.content = content;
	}
	
	/**
	 * 邮件信息构造带参 
	 * @param mailServerHost	发送邮件的服务器的IP(或主机地址)
	 * @param mailServerPort	发送邮件的服务器的端口
	 * @param toAddress			用户邮箱
	 * @param subject			标题
	 * @param content			内容
	 */
	public MailSenderInfo(String mailServerHost, String mailServerPort, String userName, String password, String fromAddress, String toAddress, String subject, String content) {
		this.mailServerHost = mailServerHost;
		this.mailServerPort = mailServerPort;
		this.validate = true;
		this.fromAddress = fromAddress;
		this.userName = userName;
		this.password = password;
		this.toAddress = toAddress;
		this.subject = subject;
		this.content = content;
	}

	/**
	 * 发邮件属性
	 */
	public Properties getProperties() {  
		  Properties p = new Properties();  
		  p.put("mail.smtp.host", this.mailServerHost);  
		  p.put("mail.smtp.port", this.mailServerPort);  
		  p.put("mail.smtp.auth", validate ? "true" : "false");
		  
		  p.put("mail.smtp.auth", validate ? "true" : "false");
		  p.put("mail.smtp.auth", validate ? "true" : "false");
		  p.put("mail.smtp.auth", validate ? "true" : "false");
		  return p;
	}
	
	/**
	 * 处理邮箱内容
	 * @param content
	 * @param r
	 * @return
	 */
	public static String dealEmailContent(String content, Ret r) {
		return content
					.replace("#username#", 	(String) r.get("username"))
					.replace("#datetime#", 	(String) r.get("datetime"))
					.replace("#url#", 		(String) r.get("url"))
					.replace("#urlName#", 	(String) r.get("urlName"));
	}
	  
	 /*
	  * get / set 
	  */
	
	 public String getMailServerHost() {  return mailServerHost; }
	 public void setMailServerHost(String mailServerHost) {  this.mailServerHost = mailServerHost; }
	 public String getMailServerPort() {  return mailServerPort; }
	 public void setMailServerPort(String mailServerPort) {  this.mailServerPort = mailServerPort; }
	 public boolean isValidate() {  return validate; }
	 public void setValidate(boolean validate) {  this.validate = validate; }
	 public String[] getAttachFileNames() {  return attachFileNames; }
	 public void setAttachFileNames(String[] fileNames) {  this.attachFileNames = fileNames; }
	 public String getFromAddress() {  return fromAddress; }
	 public void setFromAddress(String fromAddress) {  this.fromAddress = fromAddress; }
	 public String getPassword() {  return password; }
	 public void setPassword(String password) {  this.password = password; }
	 public String getToAddress() {  return toAddress; }
	 public void setToAddress(String toAddress) {  this.toAddress = toAddress; }
	 public String getUserName() {  return userName; }
	 public void setUserName(String userName) {  this.userName = userName; }
	 public String getSubject() {  return subject; }
	 public void setSubject(String subject) {  this.subject = subject; }
	 public String getContent() {  return content; }
	 public void setContent(String textContent) {  this.content = textContent; }
	
}