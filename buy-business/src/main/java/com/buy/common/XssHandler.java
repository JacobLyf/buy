package com.buy.common;


import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;

public class XssHandler extends Handler {
    
    // 排除的url，使用的target.startsWith匹配的
    private String exclude;
     
    public XssHandler(String exclude) {
        this.exclude = exclude;
    }
 
    @Override
    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, boolean[] isHandled) {
        // 对于非静态文件，和非指定排除的url实现过滤
        if (target.indexOf(".") == -1 && !target.startsWith(exclude)){
        	
            request = new HttpServletRequestWrapper(request);
        }
        nextHandler.handle(target, request, response, isHandled);
    }
}