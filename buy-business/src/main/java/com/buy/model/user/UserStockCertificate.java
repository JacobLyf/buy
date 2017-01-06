package com.buy.model.user;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.ImageIcon;

import com.buy.date.DateStyle;
import com.buy.date.DateUtil;
import com.jfinal.plugin.activerecord.Model;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class UserStockCertificate extends Model<UserStockCertificate> {

	private static final long serialVersionUID = 1L;
	public static final UserStockCertificate dao = new UserStockCertificate();
	/**
	 * 获取股权凭证图
	 * @param certificateNo
	 * @param userName
	 * @param idCardNo
	 * @param out
	 * @author huangzq
	 */
	public void getImage(UserStockCertificate detail,OutputStream out){
		String certificateNo = detail.getStr("certificate_no");
		String userName = detail.getStr("user_name");
		String idCardNo = detail.getStr("idcard_no");
		String auditTime = DateUtil.DateToString(detail.getDate("audit_time"), DateStyle.YYYY_MM_CN);
		ImageIcon imgIcon = new ImageIcon(UserStockCertificate.class.getClassLoader().getResource("certificate.jpg"));
		Image theImg = imgIcon.getImage();
		int width = theImg.getWidth(null) == -1 ? 200 : theImg.getWidth(null);
		int height = theImg.getHeight(null) == -1 ? 200 : theImg.getHeight(null);
		BufferedImage bimage = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bimage.createGraphics();
		g.drawImage(theImg, 0, 0, null);
		g.setColor(Color.red);
		g.setFont(new Font("宋体", Font.BOLD, 15)); // 字体、字型、字号
		g.drawString(certificateNo, 401, 361); //凭证编号
		g.setColor(Color.black);
		g.setFont(new Font("宋体", Font.BOLD, 17)); // 字体、字型、字号
		g.drawString(userName, 196, 757); //姓名
		g.setFont(new Font("宋体", Font.BOLD, 15)); // 字体、字型、字号
		g.drawString(idCardNo, 353, 756); //身份证号
		g.setFont(new Font("宋体", Font.BOLD, 15)); // 字体、字型、字号
		g.drawString(auditTime, 413, 786); //日期
		g.dispose();
		try {
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bimage);
			param.setQuality(100, true);
			encoder.encode(bimage, param);
			out.flush();
			out.close();
		} catch (Exception e) {
			
		}
		
		
	}

}
