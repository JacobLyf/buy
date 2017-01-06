package com.buy.model.productApply;

import java.io.File;

import com.buy.model.img.Image;
import com.buy.string.StringUtil;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Model;

public class PublicSkuUpdateProperty extends Model<PublicSkuUpdateProperty>{

	private static final long serialVersionUID = -3512463482239746493L;

	public static final PublicSkuUpdateProperty dao = new PublicSkuUpdateProperty(); 
	
	/**
	 * 设置图片
	 * @param pv
	 * @param imgPath
	 */
	public void setImgPath(PublicSkuUpdateProperty pv){
		String imgPath = pv.getStr("img_path");
		//图片上传路径
		String imgUploadPath = PropKit.getProp("global.properties").get("image.upload.base.path");
		File file = new File(imgUploadPath+imgPath);
		if(file.exists()){
			Image.dao.enable(pv.getStr("img_path"));
			pv.set("img_path", imgPath);
		}else{
			return;
		}
		//中图路径
		String midPath = StringUtil.getMidPath(imgPath);
		file = new File(imgUploadPath+midPath);
		if(file.exists()){
			pv.set("mid_path", midPath);
		}else{
			pv.set("mid_path", imgPath);
		}
		//小图路径
		String smallPath = StringUtil.getSmallPath(imgPath);
		file = new File(imgUploadPath+smallPath);
		if(file.exists()){
			pv.set("small_path", smallPath);
		}else{
			pv.set("small_path", imgPath);
		}
	}
}
