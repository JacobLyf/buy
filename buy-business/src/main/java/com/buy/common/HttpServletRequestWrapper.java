package com.buy.common;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;

public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper{
	 
    public HttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }
 
    /**
     * 重写并过滤getParameter方法
     */
    @Override
    public String getParameter(String name) {
        return StringEscapeUtils.escapeHtml(super.getParameter(name));
    }
     
    /**
     * 重写并过滤getParameterValues方法
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (null == values){
            return null;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = StringEscapeUtils.escapeHtml(values[i]);
        }
        return values;
    }
     
    /**
     * 重写并过滤getParameterMap方法
     */
    @Override
    public Map getParameterMap() {
        Map paraMap = super.getParameterMap();
        // 对于paraMap为空的直接return
        if (null == paraMap || paraMap.isEmpty()) {
            return paraMap;
        }
        for (Object obj : paraMap.entrySet()) {
        	Entry entry = (Entry) obj;
            String key = (String) entry.getKey();
            String[] values     = (String[]) entry.getValue();
            if (null == values) {
                continue;
            }
            String[] newValues  = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                newValues[i] = StringEscapeUtils.escapeHtml(values[i]);
            }
            paraMap.put(key, newValues);
        }
        return paraMap;
    }
}