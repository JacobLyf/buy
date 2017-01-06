package com.buy.html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.buy.string.StringUtil;
import com.jfinal.kit.HttpKit;


public class Htmlbuilder {  
	private Logger L = Logger.getLogger(Htmlbuilder.class);
	
    HttpClient httpClient = null; //HttpClient实例  
    GetMethod getMethod =null; //GetMethod实例  c
    BufferedWriter fw = null;  
    String page = null;  
    String webappname = null;  
    BufferedReader br = null;  
    InputStream in = null;  
    StringBuffer sb = null;  
    String line = null;   
    //构造方法  
    public Htmlbuilder(String webappname){  
        this.webappname = webappname;  
          
    }  
      
    /**  
     * 根据模版及参数产生静态页面
     * @param url  要生成目标页面的访问链接
     * @param htmlFileName  html生成的全路径
     * @return
     * @throws
     * @author Eriol
     * @date 2016年4月10日下午3:00:22
     */
    public boolean createHtmlByHttpClient(String url,String htmlFileName){  
        boolean status = false;   
        int statusCode = 0;  
        try{  
            //创建一个HttpClient实例充当模拟浏览器  
            httpClient = new HttpClient();  
            //设置httpclient读取内容时使用的字符集  
            httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"UTF-8");           
            //创建GET方法的实例  
            getMethod = new GetMethod(url);  
            //使用系统提供的默认的恢复策略，在发生异常时候将自动重试3次  
            getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());  
            //设置Get方法提交参数时使用的字符集,以支持中文参数的正常传递  
            getMethod.addRequestHeader("Content-Type","text/html;charset=UTF-8");  
            //执行Get方法并取得返回状态码，200表示正常，其它代码为异常  
            statusCode = httpClient.executeMethod(getMethod);             
            if (statusCode!=200) {  
                L.info("静态页面引擎在解析"+url+"产生静态页面"+htmlFileName+"时出错!");  
            }else{  
                //读取解析结果  
                sb = new StringBuffer();  
                in = getMethod.getResponseBodyAsStream();  
                br = new BufferedReader(new InputStreamReader(in,"UTF-8"));  
                while((line=br.readLine())!=null){  
                    sb.append(line+"\n");  
                }  
                if(br!=null)br.close();  
                page = sb.toString();  
                //将页面中的相对路径替换成绝对路径，以确保页面资源正常访问  
                page = formatPage(page);  
                //将解析结果写入指定的静态HTML文件中，实现静态HTML生成  
                writeHtml(htmlFileName,page);  
                status = true;  
            }             
        }catch(Exception ex){  
            L.error("静态页面引擎在解析"+url+"产生静态页面"+htmlFileName+"时出错");
            L.error(ex);
        }finally{  
        	 if(fw!=null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}       
        }  
        return status;  
    }  
    
    /**
     * 生成静态页面，通过jfinal的httpKit
     * @param url
     * @param htmlFileName
     * @return
     * @throws
     * @author Eriol
     * @date 2016年7月24日上午9:43:40
     */
    public boolean createHtmlPage(String url,String htmlFileName){  
        boolean status = false;   
        try{  
        	String htmlStr = HttpKit.get(url);
            if (StringUtil.isBlank(htmlStr)) {  
                L.info("静态页面引擎在解析"+url+"产生静态页面"+htmlFileName+"时出错!");  
            }else{  
                //将页面中的相对路径替换成绝对路径，以确保页面资源正常访问  
                page = formatPage(htmlStr);  
                //将解析结果写入指定的静态HTML文件中，实现静态HTML生成  
                writeHtml(htmlFileName,page);  
                status = true;
                L.info(htmlFileName+"【静态页面解析生成】");
            }             
        }catch(Exception ex){  
            L.error("静态页面引擎在解析"+url+"产生静态页面"+htmlFileName+"时出错");
            L.error(ex);
        }finally{  
        	 if(fw!=null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}       
            //释放http连接  
        }  
        return status;  
    }  
      
    //将解析结果写入指定的静态HTML文件中  
    private synchronized void writeHtml(String htmlFileName,String content) throws Exception{  
        fw = new BufferedWriter(new FileWriter(htmlFileName));  
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(htmlFileName),"UTF-8");  
        fw.write(page);   
        if(fw!=null)fw.close();       
    }  
      
    //将页面中的相对路径替换成绝对路径，以确保页面资源正常访问  
    private String formatPage(String page){       
        page = page.replaceAll("\\.\\./\\.\\./\\.\\./", webappname+"/");  
        page = page.replaceAll("\\.\\./\\.\\./", webappname+"/");  
        page = page.replaceAll("\\.\\./", webappname+"/");            
        return page;  
    }  
      
    //测试方法  
    public static void main(String[] args){
    	String domain = "http://localhost:8080/eq-web";
    	String realPath = "F:/apache-tomcat-7.0.42/webapps/eq-web/";
    	Htmlbuilder hb = new Htmlbuilder("webapp");
    	
		hb.createHtmlPage(domain+"/index/sort", realPath+"html/index/sort.html");
		hb.createHtmlPage(domain+"/initBase/indexFooterHelp", realPath+"html/index/footer.html");
		hb.createHtmlPage(domain+"/initBase/indexBanner", realPath+"html/index/banner.html");
		hb.createHtmlPage(domain+"/initBase/indexEfunChoice", realPath+"html/index/efun_choice.html");
		hb.createHtmlPage(domain+"/initBase/indexKeywWord", realPath+"html/index/search_keyword.html");
//		hb.createHtmlPage(domain+"/index/bulletinList", realPath+"html/index/bullet_list.html");
          
    }  
  
}  