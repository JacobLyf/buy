package com.buy.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.buy.tool.ToolWeb;
import com.jfinal.handler.Handler;
import com.jfinal.kit.PropKit;

/**
 * 全局Handler，设置一些通用功能
 * 描述：主要是一些全局变量的设置，再就是日志记录开始和结束操作
 */
public class GlobalHandler extends Handler {
	
	private static Logger log = Logger.getLogger(GlobalHandler.class);
	
	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {

//		log.info("设置 web 路径");
		String ctx = ToolWeb.getContextPath(request);
		request.setAttribute(BaseConstants.REQUEST_CTX, ctx);
		
//		log.info("设置图片访问 路径");
		request.setAttribute(BaseConstants.REQUEST_IMG_PATH, ctx+PropKit.use("global.properties").get("image.view.base.path"));
		//设置非图片访问路径
		request.setAttribute(BaseConstants.REQUEST_NO_IMG_PATH, ctx+PropKit.use("global.properties").get("other.view.base.path"));
		
		nextHandler.handle(target, request, response, isHandled);
		
	}
	
	
}
