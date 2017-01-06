package com.buy.avatar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.buy.date.DateUtil;
import com.buy.string.StringUtil;

/**
 * 富头像插件工具
 */
public class FuAvatarUtil {

	public static FuAvatarResult upload(HttpServletRequest request, String imageUploadPath, String sort) {
		// 参数
		FuAvatarResult result = null;									// 结果
		BufferedInputStream	inputStream = null;
		BufferedOutputStream outputStream = null;
		List<String> urlList = null;									// 路径集合
		
		try {
			// 实例化
			result = new FuAvatarResult();								// 上传图片结果
			Date now = new Date();										// 当前日期
			// 头像存放路径
			String temp = sort + File.separator
					+ DateUtil.getYear(now) + File.separator
					+ DateUtil.getMonth(now) + File.separator
					+ DateUtil.getDay(now);
			String filePath = imageUploadPath + temp;
			
			// 创建文件夹
			File file = new File(filePath);
			if(!file.exists())
				file.mkdirs();
			
			// 上传图片
			urlList = new ArrayList<>();
			String contentType = request.getContentType();
			if (contentType.indexOf("multipart/form-data") >= 0) {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				FileItemIterator fileItems = upload.getItemIterator(request);
				// 定义一个变量用以储存当前头像的序号
				int avatarNumber = 0;
				// 文件重命名
				String tepmFileName = StringUtil.getUUID();
				// 遍历表单域
				while (fileItems.hasNext()) {
					FileItemStream fileItem = fileItems.next();
					String fieldName = fileItem.getFieldName();
					
					if (fieldName.startsWith("_avatar")) {
						String realFileName = "";
						if(0 == avatarNumber) {
							realFileName = tepmFileName + ".jpg";
							result.setSourceUrl((temp + "/" + realFileName).replace("\\", "/"));		// 设置返回路径
						}

						else {
							realFileName = tepmFileName + "_small.jpg";
						}

						urlList.add((temp + "/" + realFileName).replace("\\", "/"));
						avatarNumber++;
						inputStream = new BufferedInputStream(fileItem.openStream());
						String fullFilePath = filePath + File.separator + realFileName;
						outputStream = new BufferedOutputStream(new FileOutputStream(new File(fullFilePath)));
						Streams.copy(inputStream, outputStream, true);
					}
				}
			}
			result.setAvatarUrls(urlList);
		}
		
		// 上传失败
		catch (Exception e) {
			return result.setSuccess(false);
		}
		
		finally {
			try {
				inputStream.close();
	            outputStream.flush();
	            outputStream.close();

				return result.setSuccess(true);
			}
			
			// 上传失败
			catch (Exception e) {
				return result.setSuccess(false);
			}
		}
	}
	
}
