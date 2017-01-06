package com.buy.common;




/**
 * Json返回数据类
 * @author 奕锋
 *
 */
public class JsonMessage {
	
	/**
	 * 成功
	 */
	public static final String SUCCESS = "0";
	/**
	 * 系统异常
	 */
	public static final String SYSTEM_EXCEPTION = "-1";
	/**
	 * 参数格式错误
	 */
	public static final String PARAM_TYPE_ERROR = "-2";
	/**
	 * 参数不能为空
	 */
	public static final String PARAM_NULL_ERROR = "-3";
	/**
	 * 非法token
	 */
	public static final String TOKEN_ILLEGAL = "-4";
	/**
	 * 非法签名
	 */
	public static final String SIGN_ERROR = "-4";
	/**
	 * token失效
	 */
	public static final String TOKEN_TIMEOUT = "-5";
	/**
	 * 数据异常
	 */
	public static final String DATA_EXCEPTION = "-6";
	/**
	 * 重复提交
	 */
	public static final String RESUBMIT_ERROR = "-7";
	/**
	 * 表情非法字符
	 */
	public static final String EMOJI_ILLEGAL = "-8";
	
	
	

	
	private String status = SUCCESS;//返回状态码
	private String msg="操作成功";//返回提示信息
	private Object data;//返回数据
	
	public JsonMessage(){
		super();
	}
	

	public String getStatus() {
		return status;
	}

	public JsonMessage setStatus(String status) {
		this.status = status;
		return this;
	}
	/**
	 * 设置状态码以及提示信息
	 * @param status
	 * @param msg
	 */
	public JsonMessage setStatusAndMsg(String status,String msg) {
		this.status = status;
		this.msg = msg;
		return this;
	}
	/**
	 * 系统异常
	 */
	public void systemException() {
		this.status = JsonMessage.SYSTEM_EXCEPTION;
		msg="系统异常";
	}
	/**
	 * 非法token
	 */
	public void tokenIllegal(){
		this.status = JsonMessage.TOKEN_ILLEGAL;
//		msg="非法token";
		msg="请先登录";
	}
	/**
	 * token失效
	 */
	public void tokenTimeOut(){
		this.status = JsonMessage.TOKEN_TIMEOUT;
		msg="请先登录";
	}
	/**
	 * 参数格式错误
	 */
	public void paramTypeError() {
		this.status = JsonMessage.PARAM_NULL_ERROR;
		msg="参数格式错误";
	}
	
	/**
	 * 参数不能为空
	 */
	public void paramNullError() {
		this.status = JsonMessage.PARAM_NULL_ERROR;
		msg="参数不能为空";
	}
	
	/**
	 * 非法签名
	 */
	public void signError() {
		this.status = JsonMessage.SIGN_ERROR;
		msg="非法签名";
	}
	
	/**
	 * 数据异常
	 */
	public void dataException(){
		this.status = JsonMessage.DATA_EXCEPTION;
		msg="数据异常";
	}

	/**
	 * 非法签名
	 */
	public void reSubmitError() {
		this.status = JsonMessage.RESUBMIT_ERROR;
		msg="请勿重复提交";
	}

	public String getMsg() {
		
		return msg;
	}

	public JsonMessage setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public Object getData() {
		return data;
	}

	public JsonMessage setData(Object data) {
		this.data = data;
		return this;
	}

}
