package com.buy.encryption;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * 
 * @author wuzebin
 *
 */
public class MD5Builder {
	  public static String getMD5Str(String str) {  
	        return getMD5Str(str, "UTF-8");
	 }
	  
	  public static String getMD5Str(String str,String charsetname) {  
	        MessageDigest messageDigest = null;  
	        try {  
	            messageDigest = MessageDigest.getInstance("MD5");  
	            messageDigest.reset();  
	            messageDigest.update(str.getBytes(charsetname));  
	        } catch (NoSuchAlgorithmException e) {  
	            System.out.println("NoSuchAlgorithmException caught!");  
	            System.exit(-1);  
	        } catch (UnsupportedEncodingException e) {  
	            e.printStackTrace();  
	        }  
	        byte[] byteArray = messageDigest.digest();  
	        StringBuffer md5StrBuff = new StringBuffer();  
	        for (int i = 0; i < byteArray.length; i++) {              
	            if (1 == Integer.toHexString(0xFF & byteArray[i]).length())  
	                md5StrBuff.append('0').append(Integer.toHexString(0xFF & byteArray[i]));  
	            else  
	                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
	        }  
	        return md5StrBuff.toString();  
	 } 
	  
}
