package com.buy.qrCode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class QrCodeDemo {

	public void generateQRCode(String text, OutputStream stream) throws Exception {
        int width = 100; 
        int height = 100; 
        // 二维码的图片格式 
        String format = "jpg"; 
        Hashtable hints = new Hashtable(); 
        // 内容所使用编码 
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); 
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, 
                BarcodeFormat.QR_CODE, width, height, hints);
        //生成二维码
        MatrixToImageWriter.writeToStream(bitMatrix, format, stream);
	}
	
	public static void main(String[] args) {
		QrCodeDemo agentShopController = new QrCodeDemo();
		OutputStream stream;
		try {
			stream = new FileOutputStream("D:\\test.png");
			agentShopController.generateQRCode("http://m.eq28.cn/ProductView/261792.html", stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
