package com.buy.string;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.buy.regexutil.RegexUtil;

/**
 * 字符串帮助类
 * @author tanye
 * 2014-3-29
 * StringUtil.java
 * vesion 1.0
 */
public class StringUtil {
	
	/**
	 * 字符编码
	 */
	public final static String encoding = "UTF-8";
	
	public static String cutNull(String str){
		return null==str ? "":str.trim();
	}
	 
	public static String cutNullBlank(String str){
		return cutNull(str).replaceAll("\\s+", "");
	}
	
	public static String cutNullBlankCase(String str){
		return cutNullBlank(str).toUpperCase();
	}
	
	
	
	
	public static boolean isNull(String str){
		return (null==str || "".equals(str.trim()) || "null".equalsIgnoreCase(str));
	}
	
	public static boolean isNull(Collection c){
		return (null==c || 0==c.size());
	}
	
	public static boolean isNull(Map m){
		return (null==m || 0==m.size());
	}

	public static boolean isNull(Object o){
		return (null==o || "".equals(o.toString().trim()) || "null".equals(o.toString()));
	}
	public static boolean notNull(Object o){
		return !isNull(o);
	}
	
	public static boolean notNull(String str){
		return !isNull(str);
	}
	
	public static boolean notNull(Collection c){
		return !isNull(c);
	}
	
	public static boolean notNull(Map m){
		return !isNull(m);
	}
	public static boolean notNull(Object[] c){
		return !isNull(c);
		
	}
	/**
	 * 数据不能为空，包括里面的每一个值
	 * @param c
	 * @return
	 * @author huangzq
	 */
	public static boolean isNull(Object[] c){
		if(null != c&&c.length>0){
			for(Object o : c ){
				if(null == o){
					return true;
				}
			}
		}else{
			return true;
		}
		
		return false;
		
	}
	
	
	public static String getAutoIncrementNum(String currNum, int len){
		Long newNum = new Long(currNum)+1L;
		String newNumStr = newNum.toString();
		int moreZore = len-newNumStr.length();
		for(int i=0;i<moreZore;i++){
			newNumStr = "0"+newNumStr;
		}
		return newNumStr;
	}
	
	public static String getAutoIncrementNum(String currNum){
		return getAutoIncrementNum(currNum, currNum.length());
	}
	
	public static String htmlspecialchars(String str) {
		str = str.replaceAll("&", "&amp;");
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("\"", "&quot;");
		return str;
	}
	
	
	public static Integer convertObj2Int(Object obj,Integer defaultInt){
		if(null == obj){return defaultInt;}
		
		else{
			
			Integer resultInt = null;
			try{
				resultInt = Integer.parseInt(obj.toString().trim());
			}catch (Exception e) {
				resultInt = defaultInt;
			}
			return resultInt;
			
		}
	}
	
	
	public static Double convertObj2Double(Object obj,Double defaultDouble){
		if(null == obj){return defaultDouble;}
		
		else{
			
			Double resultDouble= null;
			try{
				resultDouble = Double.parseDouble(obj.toString().trim());
			}catch (Exception e) {
				resultDouble = defaultDouble;
			}
			return resultDouble;
			
		}
	}
	
	/**
	 * 随机获取传入数量的随机数
	 * @param len
	 * @return
	 * @throws
	 * @date 2015年9月25日下午4:35:09
	 */
	public static String getRandomNum(int len) {
		String result = "";
		Random r = new Random();
		for(int i=0;i<len;i++){
			result += r.nextInt(10);
		}
		return result;
	}
	
	/*public static String getConCode(int len){
		String result = DateUtils.formatDate(new Date(), "yyMMdd");
		Random r = new Random();
		for(int i=0;i<len;i++){
			result += r.nextInt(10);
		}
		return result;
	}*/
	
	/**
	 * 生成32位的UUID
	 */
	public static String getUUID(){
		return  UUID.randomUUID().toString().replaceAll("-", "");
	}

	
	public static String format(Object o,int numAfterPoint ){
		
		boolean isNum = o instanceof Integer || o instanceof Long || o instanceof Double ||o instanceof Float;
		if(!isNum) return null;
		
		
		String result = "";
		String f = "0";
		int num = 0;
		num = numAfterPoint < 0 ? 0 : numAfterPoint;
		
		if(num>0){
			f +=".";
			for(int i=0;i<num;i++){
				f+="0";
			}
		}
		
		DecimalFormat format = new DecimalFormat(f);
		format.setRoundingMode(RoundingMode.HALF_UP); 
		result = format.format(o);
		
		return result;
	}


	public static String format_maxPonitNum(Object o,int maxPointNum ){
		
		boolean isNum = o instanceof Integer || o instanceof Long || o instanceof Double ||o instanceof Float;
		if(!isNum) return null;
		
		
		String result = "";
		String f = "#";
		int num = 0;
		num = maxPointNum < 0 ? 0 : maxPointNum;
		
		if(num>0){
			f +=".";
			for(int i=0;i<num;i++){
				f+="#";
			}
		}
		
		DecimalFormat format = new DecimalFormat(f);
		format.setRoundingMode(RoundingMode.HALF_UP); 
		result = format.format(o);
		
		return result;
	}
	
	public static String replacePicShortUrl2Full(String appUrlBase,String content) {
		return replacePicShortUrl(appUrlBase, content).get(0);
	}
	
	public static String getFirstPicFullUrl(String appUrlBase,String content){
		return replacePicShortUrl(appUrlBase, content).get(1);
		
	}
	
	/**
	 * 判断两个字段是否相等
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean equals(String str1,String str2){
		boolean b = false;
		if (StringUtils.equals(str1, str2)) {
			b = true;
		}
		return b;
	}
	
	public static List<String> replacePicShortUrl(String appUrlBase,String content) {
		List<String> list = new ArrayList<String>();
		String firstPic = null;
		
		//String regEx = "<img src=\\\"[^\\\"]+\\\"";  
		String regEx = "src=\\\"[^\\\"]+\\\"";  
		Pattern pat = Pattern.compile(regEx);  
		Matcher mat = pat.matcher(content);  
		
		while(mat.find()){
			String oneItem = mat.group();
			String toReplaceItem = oneItem.substring(5,oneItem.length()-1);
			if(!toReplaceItem.startsWith("http")){
				String  realUrl = oneItem.substring(0,5)+appUrlBase+toReplaceItem +oneItem.substring(oneItem.length()-1,oneItem.length());
				content = content.replace(oneItem, realUrl);
			}
			if(null==firstPic){firstPic= (toReplaceItem.startsWith("http")?  "" :appUrlBase)+ oneItem.substring(5,oneItem.length()-1) ;}
		}
		
		list.add(content);
		list.add(firstPic);
		return list;
	}
	

	/**
	 * 是否数量（非零的正整数）
	 * @return
	 */
	public static boolean isCount(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
	    Pattern pattern = Pattern.compile("[1-9]\\d*"); 
	    return pattern.matcher(str.trim()).matches(); 
	}
	
	/**
	 * 是否是数字
	 * @return
	 */
	public static boolean isNumber(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]\\d*"); 
		return pattern.matcher(str.trim()).matches(); 
	}
	
	/**
	 * 是否整数
	 * @return
	 */
	public static boolean isInt(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
	    Pattern pattern = Pattern.compile("-?\\d*"); 
	    return pattern.matcher(str.trim()).matches(); 
	}
	
	
	/**
	 * 判断是否金钱
	 * @param str
	 * @return
	 */
	public static boolean isMoney(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]{0,2}"); 
	    return pattern.matcher(str.trim()).matches();
	}
	
	/**
	 * 判断是否email
	 * @param str
	 * @return
	 */
	public static boolean isEmail(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\\.([a-zA-Z0-9_-])+)+$"); 
		return pattern.matcher(str).matches();
	}
	
	/**
	 * 是否电话号码
	 * @param str
	 * @return
	 */
	public static boolean isPhone(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
		str = str.replace(".", "").replace("E10", "");
		Pattern pattern = Pattern.compile("^13\\d{9}||15[8,9]\\d{8}$"); 
		return pattern.matcher(str).matches();
	}
	/**
	 * 是否账号（字母开头，允许5-16字节，允许字母数字下划线）
	 * @param str
	 * @return
	 * @author huangzq
	 */
	public static boolean isAccount(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{4,15}$"); 
		return pattern.matcher(str).matches();
	}
	
	/**
	 * 是否手机
	 * @param str
	 * @return
	 */
	public static boolean isMobile(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}

		Pattern pattern = Pattern.compile("^1[3-9]\\d{9}$"); 
		//Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$"); //这个限制太多了
		return pattern.matcher(str).matches();
	}
	
	/**
	 * 是否电话号码
	 * @param str
	 * @return
	 */
	public static boolean isTel(String str){
		if (StringUtil.isNull(str)) {
			return false;
		}

		Pattern pattern = Pattern.compile("^(\\d{3}-?\\d{8})|(\\d{4}-?\\d{7})$"); 
		return pattern.matcher(str).matches();
	}
	
	
	/**
	 * 是否是邮编
	 * @return
	 */
	public static boolean isPostCode(String str){
		
		if (StringUtil.isNull(str)) {
			return false;
		}
		Pattern pattern = Pattern.compile("[1-9]\\d{5}(?!\\d)"); 
		return pattern.matcher(str).matches();
	}
	

	
 
    
    public static boolean isBlank(String str) {
    	return (StringUtils.isBlank(str)||"null".equals(str));
    }
    public static boolean notBlank(String str) {
    	return ! isBlank(str);
    }
	
	/**
	 * 获取随机数
	 * @return
	 */
	public static String getRandom(){
		String num = (Math.round(Math.random()*(new Double(8900000000.0))+1000000000))+"";
		return num;
	}
	/**
	 * 根据文件名获取文件类型：如： .jpg 
	 * @param fileName
	 * @return
	 */
	public static String getFileType(String fileName){
		if(null != fileName&&fileName.contains(".")){
			return fileName.substring(fileName.lastIndexOf(".")) ;
		}
		return null;
	}

	
	/**
	 * 根据前缀自动生成唯一编码
	 * @param codePrefix
	 * @return
	 */
	public static String getUnitCode(String codePrefix){
		return codePrefix+System.currentTimeMillis()+RandomStringUtils.random(3, false, true);
	}
	
	/**
	 * 数组转化成字符串
	 * @param split 分割符
	 * @param objectList 目标数组
	 * @return
	 * @author huangzq
	 */
	public static String arrayToString(String split,Object... objectList){
		String ids = "";
		if(notNull(objectList)){
			for(Object id : objectList){
				ids += id+split;
			}
			if (ids.indexOf(split) > 0)
				ids = ids.substring(0, ids.length()-1);
		}
		return ids;
	}
	/**
	 * 数组转化成字符串
	 * @param split 分割符
	 * @param arrStr 目标数组
	 * @return
	 * @author Sylveon
	 */
	public static String arrayToStringForSql(String split, String[] arrStr){
		String ids = "";
		if(notNull(arrStr)){
			for(Object id : arrStr){
				ids += "'" + id + "'" + split;
			}
			if (ids.indexOf(split) > 0)
				ids = ids.substring(0, ids.length()-1);
		}
		return ids;
	}
	/**
	 * 列表转成字符串
	 * @param split
	 * @param objectList
	 * @return
	 * @author huangzq
	 */
    public static  String listToString(String split,Collection objectList){
		String ids = "";
	
			for(Object id : objectList){
				ids += id+split;
			}
			if (ids.indexOf(split) > 0)
				ids = ids.substring(0, ids.length()-1);
		
		return ids;
	 }
    /**
	 * 列表转成字符串
	 * @param split
	 * @param listStr
	 * @return
	 * @author Sylveon
	 */
    public static  String listToStringForSql(String split,List<String> listStr){
		String ids = "";
			if(notNull(listStr)){
				for(Object id : listStr){
					ids += "'" + id + "'" + split;
				}
				if (ids.indexOf(split) > 0)
					ids = ids.substring(0, ids.length()-1);
			}
		
		return ids;
	 }
    /**
     * 字符串转成列表
     * @param split 分隔符
     * @param str  带有分隔符的字符串（不带分隔符的时候为一个）
     * @return
     * @author Jacob
     * 2015年12月3日下午7:19:37
     */
	public static List<String> stringToList(String split,String str){
		List<String> strList = new ArrayList<String>();
		if(notNull(str)&&str.length()>0){
			String[] strArray = str.split(split);
			for(String s : strArray){
				strList.add(s);
			}
		}
		return strList;
	}

	/** 
	 *  
	 * @param strURL 要调用的url地址 
	 * @param strCharset 设置字符编码方式,为null，设置为utf8 
	 * @return 
	 * @throws Exception 
	 */  
	    public static String postRequest(String strURL, String strCharset)  
	            throws Exception  
	    {  
	        if ((null == strURL) || (0 == strURL.length()))  
	            return null;  
	        if ((null == strCharset) || (0 == strCharset.length()))  
	        {  
	            strCharset = "UTF-8";  
	        }  
	        String[] arrContent = (String[]) null;  
	        if (strURL.indexOf("?") > -1)  
	        {  
	            arrContent = string2Array(  
	                    strURL.substring(strURL.indexOf("?") + 1), '&', false);  
	            strURL = strURL.substring(0, strURL.indexOf("?"));  
	        }  
	  
	        StringBuffer sb = new StringBuffer();  
	        HttpURLConnection con = null;  
	        try  
	        {  
	            URL url = new URL(strURL);  
	            con = (HttpURLConnection) url.openConnection();  
	            con.setDoOutput(true);  
	            con.setDoInput(true);  
	            con.setInstanceFollowRedirects(true);  
	            con.setRequestMethod("POST");  
	            con.addRequestProperty("Content-type",  
	                    "application/x-www-form-urlencoded");  
	            con.setUseCaches(false);  
	            con.connect();  
	  
	            if ((null != arrContent) && (arrContent.length > 0))  
	            {  
	                StringBuffer sbContent = new StringBuffer();  
	                for (int i = 0; i < arrContent.length; i++)  
	                {  
	                    if ((null == arrContent[i])  
	                            || (arrContent[i].indexOf("=") == -1))  
	                        continue;  
	                    sbContent.append(  
	                            arrContent[i].substring(0, arrContent[i]  
	                                    .indexOf("="))).append('=');  
	                    sbContent.append(  
	                            URLEncoder.encode(arrContent[i]  
	                                    .substring(arrContent[i].indexOf("=") + 1),  
	                                    strCharset)).append('&');  
	                }  
	                DataOutputStream out = new DataOutputStream(con  
	                        .getOutputStream());  
	                out.writeBytes(sbContent.toString());  
	                out.flush();  
	                out.close();  
	            }  
	  
	            BufferedReader reader = new BufferedReader(new InputStreamReader(  
	                    con.getInputStream(), strCharset));  
	            String line;  
	            while (null != (line = reader.readLine()))  
	            {  
	                sb.append(line);  
	            }  
	            con.disconnect();  
	            return sb.toString();  
	        } catch (Exception e)  
	        {  
	            System.out.print(e.getMessage());  
	            sb.append(e.getMessage());  
	            String str1 = sb.toString();  
	            return str1;  
	        } finally  
	        {  
	            if (null != con)  
	                con = null;  
	        }  
	    }  
	  
	    public static String[] string2Array(String s, char delim, boolean trim)  
	    {  
	        if (0 == s.length())  
	            return new String[] {};  
	        List<String> a = new ArrayList<String>();  
	        char c;  
	        int start = 0, end = 0, len = s.length();  
	        for (; end < len; ++end)  
	        {  
	            c = s.charAt(end);  
	            if (c == delim)  
	            {  
	                String p = s.substring(start, end);  
	                a.add(trim ? p.trim() : p);  
	                start = end + 1;  
	            }  
	        }  
	        // grab the last element  
	        String p = s.substring(start, end);  
	        a.add(trim ? p.trim() : p);  
	        return (String[]) a.toArray(new String[a.size()]);  
	    } 
	    /**
	     * 字符串转整形数组
	     * @param s
	     * @param split
	     * @return
	     * @author huangzq
	     */
	    public static Integer[] stringToArray(String s ,String split){
	    	if(notNull(s)){
	    		 String[] array = s.split(split);
	    		 Integer [] num=new Integer[array.length];
	    		 for(int i=0;i<num.length;i++){
	    	            num[i]=Integer.parseInt(array[i]);
	    	     }
	    		return num;
	    	}
	    	return null;
	    }
	    public static String toUnicode(String gbString) {   
	        char[] utfBytes = gbString.toCharArray();   
	              String unicodeBytes = "";   
	               for (int byteIndex = 0; byteIndex < utfBytes.length; byteIndex++) {   
	                    String hexB = Integer.toHexString(utfBytes[byteIndex]);   
	                      if (hexB.length() <= 2) {   
	                          hexB = "00" + hexB;   
	                     }   
	                      unicodeBytes = unicodeBytes + "\\u" + hexB;   
	                  }   
	                  System.out.println("unicodeBytes is: " + unicodeBytes);   
	                  return unicodeBytes;   
	    }

	    /**
	     * 正则表达式 验证
	     * @param reg	正则表达式
	     * @param check	验证内容
	     * @return		true 通过；false 未通过
	     * @author Sylveon
	     */
	    public static boolean checkByRegex(String reg, Object check) {
	    	String checkStr = null;
	    	if(null == check) {
				return false;
			} else {
				checkStr = check.toString().trim();
			}
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(checkStr);
			return matcher.matches();
	    }
	    /**
		 * 数组字符串是否包含某个key
		 * @param array 
		 * @param key
		 * @param split 分割符
		 * @return
		 * @author huangzq
		 */
		public static boolean arrayContain(String arrayStr ,String key ,String split){
			if(notNull(arrayStr)){
				String[] array  = arrayStr.split(split);
				for(String s : array){
					
					if(s.trim().equals(key)){
						return true;
					}
				}
			}
			return false;
		}
		 
		/**
		 * 判断数组中是否有重复
		 * @param array
		 * @return 有重复：false 不重复：true
		 * @author chenhg
		 * 2016年4月10日 下午2:14:36
		 */
		public static boolean checkRepeat(String[] array){
		    Set<String> set = new HashSet<String>();
		    for(String str : array){
		        set.add(str);
		    }
		    if(set.size() != array.length){
		        return false;
		    }else{
		        return true;
		    }
		}
		
		/**
		 * 清楚html标签
		 * @param html
		 * @param len
		 * @return
		 */
		public static String removeHtml(String html, Integer len) {
			if (StringUtil.isNull(html))
				return "";
			// 清除标签
			html = html
					.replaceAll("&nbsp;", " ")				// 空格
					.replaceAll("\\&[a-zA-Z]{0,9};", "")	// &字符
					.replaceAll("<!--[^>]*>", " ")			// 注释
					.replaceAll("<[^>]*>", "\n");			// 标签
			// 处理空行和长度
			String result = "";
			for (String s : html.split("\n")) {
				if (StringUtil.notBlank(s))
					result += s + "\t";
				
				int tempLen = result.length();
				if (len!=null&& tempLen > len)
					return result.substring(0, len) + "...";
			}
			return result;
		}
	
	/*
	 *  去除数组中重复的记录
	 */
	public static List<String> listUnique(List<String> list) {
		List<String> newList = new LinkedList<String>();
		for (int i = 0; i < list.size(); i++) {
			if (!newList.contains(list.get(i))) {
				newList.add(list.get(i));
			}
		}
		return newList;
	}
	/**
	 * 获取中图路径
	 * @param imgPath
	 * @author huangzq
	 */
	public static String getMidPath(String imgName){
		String fileType = imgName.substring(imgName.lastIndexOf("."));
		String midName = imgName.substring(0, imgName.lastIndexOf("."))+"_mid"+fileType;
		return midName;
	}
	/**
	 * 获取小图路径
	 * @param imgPath
	 * @author huangzq
	 */
	public static String getSmallPath(String imgName){
		String fileType = imgName.substring(imgName.lastIndexOf("."));
		String midName = imgName.substring(0, imgName.lastIndexOf("."))+"_small"+fileType;
		return midName;
	}

	/**
	 * 字符串数组转int数组
	 * @param strArr
	 * @return
	 * @author chenhg
	 * 2016年8月18日 下午9:20:30
	 */
	public static int[] StringArrToIntArr(Object[] strArr){
		if(strArr == null || strArr.length ==0){
			return null;
		}
		
		int[] intArr = new int[strArr.length];
		for(int i=0; i < intArr.length; i++){
			if(isInt(strArr[i].toString())){
				intArr[i] = Integer.valueOf(strArr[i].toString());
			}else{
				return null;
			}
		}
		
		return intArr;
		
	}
	
	/**
	 * 整型数组转换为字符串型数组
	 * @param intArr
	 * @return
	 */
	public static String[] IntArrToStringArr(Integer[] intArr){
		if(intArr == null || intArr.length ==0){
			return null;
		}
		
		String[] strArr = new String[intArr.length];
		for(int i=0; i < intArr.length; i++){
			if(notNull(intArr[i])){
				strArr[i] = intArr[i].toString();
			}else{
				return null;
			}
		}
		
		return strArr;
	}
	
	/***
	 * 隐藏会员用户名中间字符.
	 * 
	 * @author Chengyb
	 */
	public static String hideUserName(String userName) {
		if(userName.startsWith("eq_") && userName.length() == 16) { // 系统生成的会员名.
			return userName.substring(0, 6) + "******" + userName.substring(12, 16);
		}else if(userName.length() < 4) {
			return userName.substring(0, 1) + "******" ;
		}else if(userName.length() < 10) {
			return userName.substring(0, 3) + "******" + userName.substring(4, userName.length());
		} else {
			return userName.substring(0, 5) + "******" + userName.substring(8, userName.length());
		}
	}
	
	/**
	 * 检查字符串是否有特殊字符
	 * @param inputString
	 * @return
	 */
	public static boolean hasSpecialString(String inputString) {
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern pattern = Pattern.compile(regEx);
		return pattern.matcher(inputString).lookingAt();
	}
	
	public static boolean hasEmojiCharacter(String source) {
		if (isNull(source))
			return false;
		
		Pattern emoji = Pattern.compile (RegexUtil.REG_EMOJI);
		Matcher matcher = emoji.matcher(source);
		return matcher.find();
	}
	
	/**
	 * 去掉小数多余的0
	 */
	public static String removeTailZero (BigDecimal val) {
		if (StringUtil.isNull(val))
			return  null;
		return val.stripTrailingZeros().toPlainString();

	}
	
	public static void main(String[] args) {
		boolean a = hasEmojiCharacter("\\U0001F604");
		System.out.println(a);
	}
	
}