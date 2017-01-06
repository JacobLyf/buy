package com.buy.common;

/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.buy.date.DateUtil;
import com.buy.model.user.User;
import com.buy.string.StringUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;

/**
 * ValidatorUtil.
 */
public abstract class Validator implements Interceptor {
	protected JsonMessage jsonMessage = new JsonMessage();
	private Controller controller;
	private Invocation invocation;
	//默认一个验证不通过则不往下验证
	private boolean shortCircuit = true;
	//默认验证不通过
	private boolean invalid = false;
	private String datePattern = null;
	
	// TODO set the DEFAULT_DATE_PATTERN in Const and config it in Constants. TypeConverter do the same thing.
	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	private static final String emailAddressPattern = "\\b(^['_A-Za-z0-9-]+(\\.['_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";
	
	protected void setShortCircuit(boolean shortCircuit) {
		this.shortCircuit = shortCircuit;
	}
	
	protected void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
	
	protected String getDatePattern() {
		return (datePattern != null ? datePattern : DEFAULT_DATE_PATTERN);
	}
	
	final public void intercept(Invocation invocation) {
		Validator validator = null;
		try {validator = getClass().newInstance();}
		catch (Exception e) {throw new RuntimeException(e);}
		
		validator.controller = invocation.getController();
		validator.invocation = invocation;
		
		try {validator.validate(validator.controller);} 
		catch (RuntimeException e) {
			//e.printStackTrace();
			/* should not be throw */
			//报错不通过验证
			validator.invalid = true;
			
		
		}			// short circuit validate need this
		
		if (validator.invalid)
			validator.handleError(validator.controller);
		else
			invocation.invoke();
	}
	
	/**
	 * Use validateXxx method to validate the parameters of this action.
	 */
	protected abstract void validate(Controller c);
	
	/**
	 * Handle the validate error.
	 * Example:<br>
	 * controller.keepPara();<br>
	 * controller.render("register.html");
	 */
	protected abstract void handleError(Controller c);
	
	/**
	 * Add message when validate failure.
	 */
	protected void addError(String errorKey, String errorMessage) {
		invalid = true;
		jsonMessage.setStatusAndMsg(errorKey, errorMessage);
		controller.setAttr(errorKey, errorMessage);
		if (shortCircuit) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * Return the controller of this action.
	 */
	protected Controller getController() {
		return controller;
	}
	
	/**
	 * Return the action key of this action.
	 */
	protected String getActionKey() {
		return invocation.getActionKey();
	}
	
	/**
	 * Return the controller key of this action.
	 */
	protected String getControllerKey() {
		return invocation.getControllerKey();
	}
	
	/**
	 * Return the method of this action.
	 */
	protected Method getActionMethod() {
		return invocation.getMethod();
	}
	
	/**
	 * Return view path of this controller.
	 */
	protected String getViewPath() {
		return invocation.getViewPath();
	}
	
	/**
	 * Validate Required. Allow space characters.
	 */
	protected void validateRequired(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (value == null || "".equals(value))	// 经测试,form表单域无输入时值为"",跳格键值为"\t",输入空格则为空格" "
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate Required for urlPara.
	 */
	protected void validateRequired(int index, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value == null /* || "".equals(value) */)
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate required string.
	 */
	protected void validateRequiredString(String field, String errorKey, String errorMessage) {
		if (StrKit.isBlank(controller.getPara(field)))
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate required string for urlPara.
	 */
	protected void validateRequiredString(int index, String errorKey, String errorMessage) {
		if (StrKit.isBlank(controller.getPara(index)))
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate integer.
	 */
	protected void validateInteger(String field, int min, int max, String errorKey, String errorMessage) {
		validateIntegerValue(controller.getPara(field), min, max, errorKey, errorMessage);
	}
	
	/**
	 * Validate integer for urlPara.
	 */
	protected void validateInteger(int index, int min, int max, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n")))
			value = "-" + value.substring(1);
		validateIntegerValue(value, min, max, errorKey, errorMessage);
	}
	
	private void validateIntegerValue(String value, int min, int max, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			int temp = Integer.parseInt(value.trim());
			if (temp < min || temp > max)
				addError(errorKey, errorMessage);
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate integer.
	 */
	protected void validateInteger(String field, String errorKey, String errorMessage) {
		validateIntegerValue(controller.getPara(field), errorKey, errorMessage);
	}
	
	/**
	 * Validate integer for urlPara.
	 */
	protected void validateInteger(int index, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n")))
			value = "-" + value.substring(1);
		validateIntegerValue(value, errorKey, errorMessage);
	}
	
	private void validateIntegerValue(String value, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Integer.parseInt(value.trim());
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate long.
	 */
	protected void validateLong(String field, long min, long max, String errorKey, String errorMessage) {
		validateLongValue(controller.getPara(field), min, max, errorKey, errorMessage);
	}
	
	/**
	 * Validate long for urlPara.
	 */
	protected void validateLong(int index, long min, long max, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n")))
			value = "-" + value.substring(1);
		validateLongValue(value, min, max, errorKey, errorMessage);
	}
	
	private void validateLongValue(String value, long min, long max, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			long temp = Long.parseLong(value.trim());
			if (temp < min || temp > max)
				addError(errorKey, errorMessage);
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate long.
	 */
	protected void validateLong(String field, String errorKey, String errorMessage) {
		validateLongValue(controller.getPara(field), errorKey, errorMessage);
	}
	
	/**
	 * Validate long for urlPara.
	 */
	protected void validateLong(int index, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n")))
			value = "-" + value.substring(1);
		validateLongValue(value, errorKey, errorMessage);
	}
	
	private void validateLongValue(String value, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Long.parseLong(value.trim());
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate double.
	 */
	protected void validateDouble(String field, double min, double max, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			double temp = Double.parseDouble(value.trim());
			if (temp < min || temp > max)
				addError(errorKey, errorMessage);
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate double.
	 */
	protected void validateDouble(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Double.parseDouble(value.trim());
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate date. Date formate: yyyy-MM-dd
	 */
	protected void validateDate(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			new SimpleDateFormat(getDatePattern()).parse(value.trim());	// Date temp = Date.valueOf(value); 为了兼容 64位 JDK
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate date.
	 */
	protected void validateDate(String field, Date min, Date max, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Date temp = new SimpleDateFormat(getDatePattern()).parse(value.trim());	// Date temp = Date.valueOf(value); 为了兼容 64位 JDK
			if (temp.before(min) || temp.after(max))
				addError(errorKey, errorMessage);
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate date. Date formate: yyyy-MM-dd
	 */
	protected void validateDate(String field, String min, String max, String errorKey, String errorMessage) {
		// validateDate(field, Date.valueOf(min), Date.valueOf(max), errorKey, errorMessage);  为了兼容 64位 JDK
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(getDatePattern());
			validateDate(field, sdf.parse(min.trim()), sdf.parse(max.trim()), errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate equal field. Usually validate password and password again
	 */
	protected void validateEqualField(String field_1, String field_2, String errorKey, String errorMessage) {
		String value_1 = controller.getPara(field_1);
		String value_2 = controller.getPara(field_2);
		if (value_1 == null || value_2 == null || (! value_1.equals(value_2)))
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate equal string.
	 */
	protected void validateEqualString(String s1, String s2, String errorKey, String errorMessage) {
		if (s1 == null || s2 == null || (! s1.equals(s2)))
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate equal integer.
	 */
	protected void validateEqualInteger(Integer i1, Integer i2, String errorKey, String errorMessage) {
		if (i1 == null || i2 == null || (i1.intValue() != i2.intValue()))
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate email.
	 */
	protected void validateEmail(String field, String errorKey, String errorMessage) {
		validateRegex(field, emailAddressPattern, false, errorKey, errorMessage);
	}
	
	/**
	 * Validate URL.
	 */
	protected void validateUrl(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			value = value.trim();
			if (value.startsWith("https://"))
				value = "http://" + value.substring(8); // URL doesn't understand the https protocol, hack it
			new URL(value);
		} catch (MalformedURLException e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate regular expression.
	 */
	protected void validateRegex(String field, String regExpression, boolean isCaseSensitive, String errorKey, String errorMessage) {
        String value = controller.getPara(field);
        if (value == null) {
        	addError(errorKey, errorMessage);
        	return ;
        }
        Pattern pattern = isCaseSensitive ? Pattern.compile(regExpression) : Pattern.compile(regExpression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        	addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate regular expression and case sensitive.
	 */
	protected void validateRegex(String field, String regExpression, String errorKey, String errorMessage) {
		validateRegex(field, regExpression, true, errorKey, errorMessage);
	}
	
	/**
	 * Validate string.
	 */
	protected void validateString(String field, int minLen, int maxLen, String errorKey, String errorMessage) {
		validateStringValue(controller.getPara(field), minLen, maxLen, errorKey, errorMessage);
	}
	
	/**
	 * Validate string for urlPara
	 */
	protected void validateString(int index, int minLen, int maxLen, String errorKey, String errorMessage) {
		validateStringValue(controller.getPara(index), minLen, maxLen, errorKey, errorMessage);
	}
	
	private void validateStringValue(String value, int minLen, int maxLen, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		if (value.length() < minLen || value.length() > maxLen)
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate token created by Controller.createToken(String).
	 */
	protected void validateToken(String tokenName, String errorKey, String errorMessage) {
		if (controller.validateToken(tokenName) == false)
			addError(errorKey, errorMessage);
	}
	
	/**
	 * Validate token created by Controller.createToken().
	 */
	protected void validateToken(String errorKey, String errorMessage) {
		if (controller.validateToken() == false)
			addError(errorKey, errorMessage);
	}
	
	/**
	 * validate boolean.
	 */
	protected void validateBoolean(String field, String errorKey, String errorMessage) {
		validateBooleanValue(controller.getPara(field), errorKey, errorMessage);
	}
	
	/**
	 * validate boolean for urlPara.
	 */
	protected void validateBoolean(int index, String errorKey, String errorMessage) {
		validateBooleanValue(controller.getPara(index), errorKey, errorMessage);
	}
	
	private void validateBooleanValue(String value, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		value = value.trim().toLowerCase();
		if ("1".equals(value) || "true".equals(value))
			return ;
		else if ("0".equals(value) || "false".equals(value))
			return ;
		addError(errorKey, errorMessage);
	}
	/**
	 * 获取验证结果
	 * @return 有效：true 无效：false
	 * @author huangzq
	 */
	public boolean getValidate(){
		return !invalid;
	}
	/**
	 * 验证参数是否为空
	 * @param fieldName
	 * @author huangzq
	 */
	public void validateRequired(String... fieldNames){
		for(String field : fieldNames ){
			String value = controller.getPara(field);
			
			if (StringUtil.hasEmojiCharacter(value))
				addError(JsonMessage.EMOJI_ILLEGAL, field+"存在非法字符");
			
			if (StringUtil.isBlank(value))	// 经测试,form表单域无输入时值为"",跳格键值为"\t",输入空格则为空格" "
				addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
			}
	}
	/**
	 * 验证字符串（数组）
	 * @param field
	 * @author Sylveon
	 */
	protected void validateRequiredArray(String field) {
		Object[] array = controller.getParaValues(field);	// 普通数组
		if (StringUtil.isNull(array) ){	// 苹果数组
			array = controller.getParaValues(field + "[]");
		}
		if (StringUtil.isNull(array)){
			addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
		}
		try {
			for (Object i : array) {
				String temp = i + "";
				String.valueOf(temp);
				
				if (StringUtil.hasEmojiCharacter(temp))
					addError(JsonMessage.EMOJI_ILLEGAL, field+"存在非法字符");
			}
			controller.setAttr(field, array);
		} catch (Exception e) {
			addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
		}
	}
	
	/**
	 * 验证int数组
	 * @param field
	 * @author huangzq
	 * 2017年1月1日 下午6:58:34
	 *
	 */
	protected void validateIntArray(String field) {
		try {
			Integer [] array = controller.getParaValuesToInt(field);	// 普通数组
			if (StringUtil.isNull(array) ){	// 苹果数组
				array = controller.getParaValuesToInt(field + "[]");
			}
			if (StringUtil.isNull(array)){
				addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
			}
			
			controller.setAttr(field, array);
		} catch (Exception e) {
			addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
		}
		
	}
	
	
	/**
	 * 验证整数
	 * @param fieldNames
	 * @author huangzq
	 */
	protected void validateInteger(String... fieldNames) {
		for(String field : fieldNames ){
			validateIntegerValue(controller.getPara(field), JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
		}
	}
	/**
	 * 验证整数（数组）
	 * @param field
	 * @author Sylveon
	 */
	protected void validateIntegerArray(String field) {
		Object[] array = controller.getParaValues(field);	// 普通数组
		if (StringUtil.isNull(array) || array.length == 0)	// 苹果数组
			array = controller.getParaValues(field + "[]");
		if (StringUtil.isNull(array) || array.length == 0)
			addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
		
		try {
			for (Object i : array) {
				String temp = i + "";
				Integer.valueOf(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
		}
	}
	/**
	 * 验证double
	 * @param fieldNames
	 * @author huangzq
	 */
	protected void validateDouble(String... fieldNames) {
		for(String field : fieldNames ){
			String value = controller.getPara(field);
			if (StrKit.isBlank(value)) {
				addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
				return ;
			}
			try {
				Double.parseDouble(value.trim());
			}
			catch (Exception e) {
				addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
				return;
			}
		}
	}
	/**
	 * 验证日期
	 * @param fieldNames
	 * @author huangzq
	 */
	protected void validateDate(String... fieldNames){
		for(String field : fieldNames ){
			
			String value = controller.getPara(field);
			if (StrKit.isBlank(value)) {
				addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
				return ;
			}
			if(!DateUtil.isDate(value)){
				addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
				return;
			}
		}
	}
	/**
	 * 验证JSON
	 * @param fieldNames
	 * @author huangzq
	 */
	protected void validateJson(String... fieldNames){
		for(String field : fieldNames ){
			
			String value = controller.getPara(field);
			if (StrKit.isBlank(value)) {
				addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
				return ;
			}
			try{
				JSONArray.parse(value);
			}catch(JSONException e){
				addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
			}
		}
	}
	/**
	 * 验证金钱格式
	 * @param fieldNames
	 * @author huangzq
	 */
	protected void validateMoney(String... fieldNames){
		for(String field : fieldNames ){
			
			String value = controller.getPara(field);
			if (StrKit.isBlank(value)) {
				addError(JsonMessage.PARAM_NULL_ERROR, field+"参数不能为空");
				return ;
			}
			if(!StringUtil.isMoney(value)){
				addError(JsonMessage.PARAM_TYPE_ERROR, field+"参数格式错误");
				return;
			}
		}
	}
	
	/**
	 * 验证手机号码格式.
	 * 
	 * @param field
	 * @param errorKey
	 * @param errorMessage
	 * @author Chengyb
	 */
	protected void validateMobile(String field, String errorKey, String errorMessage) {
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,1,5-9])|(17[6，7,8]))\\d{8}$");
		Matcher m = p.matcher(controller.getPara(field));
		if (!m.matches()) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * 返回json
	 * 
	 * @author huangzq
	 */
	public void returnJson() {
		if(this.isIE()){
			this.getController().render(new JsonRender(jsonMessage).forIE());
		}else{
			this.getController().renderJson(jsonMessage);
		}

	}
	
	/**
	 * 判断是否IE
	 * @return
	 * @author huangzq
	 */
	public boolean  isIE(){  
		String userAgent = this.getController().getRequest().getHeader( "USER-AGENT" );
		if(userAgent!=null){
			return userAgent.toLowerCase().indexOf( "trident" ) >  0  ?  true  :  false ;  
		}else{
			return false;
		}
	}
	
	/**
	 * 获取前端传来Json字符串转成Model列表
	 * @param modelClass
	 * @param modelName
	 * @return
	 * @author huangzq
	 * @param <T>
	 */
    public <T> List<T> getModelsForJson(Class<T> modelClass, String paraName) {
        List<T> list = new ArrayList<T>();
        String jsonStr = getController().getPara(paraName);
    	List<Map> mapList = JSONArray.parseArray(jsonStr, Map.class);    	
        for (Map m : mapList) {
        	if(StringUtil.notNull(m)){
        		Object modelObject = null;
        		try {
        			modelObject = modelClass.newInstance();
        		} catch (Exception e) {
        			throw new RuntimeException(e);
        		}
        		Model model = (Model)modelObject;
	            for(Object key : m.keySet() ){
	            	model.set((String)key, m.get(key));
	            }
	            list.add((T)model);
        	}
        }
        return list;
    }
    
    /**
     * 获取前端传来的Json字符串转成Record列表
     * @param modelName
     * @return
     * @author huangzq
     */
    public List<Record> getRecordsForJson(String paraName) {
    	String jsonStr = getController().getPara(paraName);
    	List<Map> mapList = JSONArray.parseArray(jsonStr, Map.class);
        List<Record> list = new ArrayList<Record>();
        for (Map m : mapList) {
        	if(StringUtil.notNull(m)){
        		Record r = new Record();
	            for(Object key : m.keySet() ){
	            	r.set((String)key, m.get(key));
	            }
	            list.add(r);
        	}
        }
        return list;
    }
	
	/**
     * 获取前端传来的数组对象并响应成Record列表
     * @param modelName
     * @return
     * @author huangzq
     */
    public List<Record> getRecords(String modelName) {
        List<String> nos = getModelsNoList(modelName);
        List<Record> list = new ArrayList<Record>();
        for (String no : nos) {
            Record r = getRecord(modelName + "[" + no + "]");
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }
    
    /**
     * 获取以modelName开头的参数，并自动赋值给record对象
     * @param modelName
     * @return
     * @author huangzq
     */
    public Record getRecord(String modelName) {
        String modelNameAndDot = modelName + ".";
        Record model = new Record();
        boolean exist = false;
        Map<String, String[]> parasMap = getController().getRequest().getParameterMap();
        for (Entry<String, String[]> e : parasMap.entrySet()) {
            String paraKey = e.getKey();
            if (paraKey.startsWith(modelNameAndDot)) {
                String paraName = paraKey.substring(modelNameAndDot.length());
                String[] paraValue = e.getValue();
                Object value = paraValue[0] != null ? (paraValue.length == 1 ? paraValue[0]
                        : StringUtil.arrayToString("", paraValue))
                        : null;
                model.set(paraName, value);
                exist = true;
            }
        }
        if (exist) {
            return model;
        } else {
            return null;
        }
    }
    /**
     * 提取model对象数组的标号
     * @param modelName
     * @return
     * @author huangzq
     */
    private List<String> getModelsNoList(String modelName) {
        // 提取标号
        List<String> list = new ArrayList<String>();
        String modelNameAndLeft = modelName + "[";
        Map<String, String[]> parasMap = getController().getRequest().getParameterMap();
        for (Entry<String, String[]> e : parasMap.entrySet()) {
            String paraKey = e.getKey();
            if (paraKey.startsWith(modelNameAndLeft)) {
                String no = paraKey.substring(paraKey.indexOf('[') + 1,
                        paraKey.indexOf(']'));
                if (!list.contains(no)) {
                    list.add(no);
                }
            }
        }
        return list;
    }

	/**
	 * 验证APP来源
	 */
	protected void validateAppDataFrom(String fieldName) {
		String value = controller.getPara(fieldName);
		if (StrKit.isBlank(value)) {
			addError(JsonMessage.PARAM_NULL_ERROR, fieldName + "参数不能为空");
			return;
		}
		if (!BaseConstants.DataSource.IOS.toString().equals(value) &&
				!BaseConstants.DataSource.ANDROID.toString().equals(value)) {
			addError(JsonMessage.DATA_EXCEPTION, fieldName + "参数错误");
			return;
		}
	}
	
	/**
	 * 获取当前用户session
	 * @return
	 * @author huangzq
	 */
	public User getCurrentUser(){
	
		User user = (User) controller.getSession().getAttribute(BaseConstants.SESSION_USER);
		if(StringUtil.isNull(user))
			return null;
		// 查询用户信息
		String userId = user.getStr("id");
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	id, user_name, avatar_org, avatar, mobile");
		sql.append(" FROM t_user");
		sql.append(" WHERE id = ?");
		return User.dao.findFirst(sql.toString(), userId);
	}
	
	/**
	 * 获取当前用户ID
	 * @return
	 * @author Sylveon
	 */
	@SuppressWarnings("rawtypes")
	public String getCurrentUserId() {
		
		User user = (User) controller.getSession().getAttribute(BaseConstants.SESSION_USER);
		if(StringUtil.isNull(user))
			return null;
		return user.getStr("id");
	}

}
