package com.buy.model.product;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.date.DateUtil;
import com.buy.image.ImageUtil;
import com.buy.plugin.fuAvatar.FuAvatarResult;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.http.HttpServletRequest;

public class ProductImg extends Model<ProductImg>{
	
	/**
	 * 商品图片
	 */
	private static final long serialVersionUID = 1L;
	public static final ProductImg dao = new ProductImg();
	/**
	 * 备份后缀
	 */
	public static String ORG_SUFFIX = "_org";
	/**
	 * 中图后缀
	 */
	public static String MID_SUFFIX = "_mid";
	/**
	 * 小图后缀
	 */
	public static String SMALL_SUFFIX = "_small";
	/**
	 * 中图最大高度
	 */
	public static int MID_MAX_HEIGHT = 418;
	/**
	 * 小图最大高度
	 */
	public static int SMALL_MAX_HEIGHT = 60;
	/**
	 * 状态：启用
	 */
	public static int STATUS_ENABLE = 1;
	/**
	 * 状态：禁用
	 */
	public static int STATUS_DISABLE= 0;
	
	/**
	 * 获取商品图片(大、中、小图)
	 */
	public List<Record> getProductImg(int proId) {
		StringBuffer sql = new StringBuffer();
		sql.append("  SELECT ");
		sql.append("   c.big_path, ");
		sql.append("   c.small_path, ");
		sql.append("   c.mid_path ");
		sql.append("  FROM t_pro_img c ");
		sql.append("  WHERE c.product_id = ? ");
		sql.append("  ORDER BY c.is_main desc ");
		return Db.find(sql.toString(), proId);
	}
	
	/**
	 * 解除商品图片关联
	 * @param imgIds
	 * @author huangzq
	 */
	public void clearImg(Integer productId,Integer... imgIds){
		if(productId!=null&&StringUtil.notNull(imgIds)){
			StringBuffer sql = new StringBuffer();
			sql.append("update  t_pro_img set product_id = null where id in (");
			for(Integer id : imgIds){
				sql.append(id);
				sql.append(',');
			}
			sql.setLength(sql.length()-1);
			sql.append(") and product_id = ?");
			Db.update(sql.toString(), productId);
		}
	}
	/**
	 * 根据商品id获取图片
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public List<ProductImg> findByProductId(int productId){
		
		return ProductImg.dao.find("select *  from t_pro_img where product_id = ? order by is_main desc , sort_num asc",productId);
		
	}
	/**
	 * 关联商品图片
	 * @param productId
	 * @param imgIds
	 * @author huangzq
	 */
	public void updateProductImg(Integer productId ,Integer[] imgIds){
		for(int i = 0;i<imgIds.length;i++){
			ProductImg img = new ProductImg();
			img.set("id", imgIds[i]);
			img.set("product_id", productId);
			img.set("sort_num", i+1);
			img.update();
		}
	}
	/**
	 * 更新商品主图
	 * @param productId
	 * @param imgId
	 * @author huangzq
	 */
	public void updateMainImg(Integer productId,Integer imgId){
		Product product = new Product();
		product.set("id", productId);
		ProductImg productImg = ProductImg.dao.findById(imgId);
		if(productImg!=null){
			//去掉其他图片的主图标识
			Db.update("update t_pro_img  img set img.is_main = ? where img.product_id = ?",BaseConstants.NO,productId);
			//设置中图为主图
			product.set("product_img", productImg.getStr("mid_path"));
			//标识主图
			productImg.set("is_main", BaseConstants.YES);			
			product.update();
			productImg.update();
		}
	}

	/**
	 * 商品图片记录添加
	 */
	public ProductImg addProductImg(String orgPath, String bigPath, String midPath, String smallPath, int status, String createUserId) {
		ProductImg img = new ProductImg()
			.set("org_path",		orgPath)
			.set("big_path",		bigPath)
			.set("mid_path",		midPath)
			.set("small_path",		smallPath)
			.set("status",			status)
			.set("create_user_id",	createUserId)
			.set("create_time",		new Date());
		img.save();
		return img;
	}

	/**
	 * 商品图片记录添加 - 可用
	 */
	public ProductImg addProductImgByEnable(String orgPath, String bigPath, String midPath, String smallPath, String createUserId) {
		return addProductImg(orgPath, bigPath, midPath, smallPath, STATUS_ENABLE, createUserId);
	}

	/**
	 * 商品图片记录添加 - 禁用
	 */
	public ProductImg addProductImgByDisable(String orgPath, String bigPath, String midPath, String smallPath, String createUserId) {
		return addProductImg(orgPath, bigPath, midPath, smallPath, STATUS_DISABLE, createUserId);
	}

	/**
	 * 富文本上传商品图片 - 上传单张
	 */
	public FuAvatarResult uploadImgByFullAvatar(HttpServletRequest request, String imageUploadPath, String createUserId) {
		FuAvatarResult result = new FuAvatarResult();

		Date now = new Date();
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;

		try {
			String filePrefix = new StringBuffer(BaseConstants.UploadImgSort.SORT_PRODUCT).append(File.separator)
					.append(DateUtil.getYear(now)).append(File.separator)
					.append(DateUtil.getMonth(now)).append(File.separator)
					.append(DateUtil.getDay(now))
					.toString();
			String fileName = StringUtil.getUUID();
			String fileSuffix = ".jpg";
			String filePath = imageUploadPath + filePrefix;

			// 创建文件夹
			File file = new File(filePath);
			if(!file.exists())
				file.mkdirs();

			// 上传图片
			String contentType = request.getContentType();
			if(contentType.indexOf("multipart/form-data") >= 0 ) {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				FileItemIterator fileItems = upload.getItemIterator(request);

				// 遍历表单域
				while(fileItems.hasNext()) {
					FileItemStream fileItem = fileItems.next();

					String realFileName = fileName + fileSuffix;
					String imagePath = (filePrefix + "/" + realFileName).replace("\\", "/");
					String fullFilePath = filePath + File.separator + realFileName;

					inputStream = new BufferedInputStream(fileItem.openStream());
					outputStream = new BufferedOutputStream(new FileOutputStream(new File(fullFilePath)));
					Streams.copy(inputStream, outputStream, true);

					// 大于100k的进行压缩
					String bigImg = imagePath;			// 大图路径
					String midImg = imagePath;			// 中图路径
					String smallImg = imagePath;		// 小图路径

					// 验证CMYK
					File newFile = new File(imageUploadPath+bigImg);
					if(ImageUtil.isCMYK(newFile)) {
						addProductImgByDisable(imageUploadPath+bigImg, "", "", "", createUserId);
						return result.setMsg("不支持CMYK模式的图片，请使用PS转成RGB模式").setSuccess(false);
					}

					String temp = filePrefix + "/" + fileName;
					if (newFile.length() > BaseConstants.UploadImgSort.MIN_COMPRESS_SIZE) {
						// 压缩小图
						ImageUtil.resize(newFile, fileName + ProductImg.SMALL_SUFFIX, ProductImg.SMALL_MAX_HEIGHT, null);
						smallImg = temp + ProductImg.SMALL_SUFFIX + fileSuffix;
						// 压缩中图
						ImageUtil.resize(newFile, fileName + ProductImg.MID_SUFFIX, ProductImg.MID_MAX_HEIGHT, null);
						midImg = temp + ProductImg.MID_SUFFIX + fileSuffix;
						// 压缩原图
						ImageUtil.resize(newFile, fileName, null, null);
					}

					// 设置备份图
					ImageUtil.resize(newFile, fileName + ProductImg.ORG_SUFFIX, null, 1F);
					String orcImg = temp + ProductImg.ORG_SUFFIX + fileSuffix;

					// 添加商品图片
					ProductImg img = addProductImgByEnable(orcImg, bigImg, midImg, smallImg, createUserId);

					// 设置返回参数
					Integer imgId = img.getNumber("id").intValue();	// 图片ID
					List<String> urlList = new ArrayList<>();				// 路径集合
					urlList.add(imagePath);
					result.setImgId(imgId).setSourceUrl(imagePath).setAvatarUrls(urlList);
					break;
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			return result.setSuccess(false);			// 上传失败
		}

		finally {
			try {
				inputStream.close();
				outputStream.flush();
				outputStream.close();

				return result.setSuccess(true);		// 上传成功
			}

			catch (Exception e) {
				return result.setSuccess(false);	// 上传失败
			}
		}
	}

}
