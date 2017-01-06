package com.buy.qrCode;

import java.io.OutputStream;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class QrCodeUtil {
	/**
	 * 二维码生成
	 * @param text 
	 * @param stream
	 * @param format
	 * @param width
	 * @param height
	 * @throws Exception
	 * @author huangzq
	 */
	@SuppressWarnings("rawtypes")
	public static void generateQRCode(String text, OutputStream stream,String format,int width,int height) throws Exception {

        Hashtable hints = new Hashtable(); 
        // 内容所使用编码 
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); 
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, 
                BarcodeFormat.QR_CODE, width, height, hints);
        //生成二维码
        MatrixToImageWriter.writeToStream(bitMatrix, format, stream);
        stream.flush();
        stream.close();
	}

}
