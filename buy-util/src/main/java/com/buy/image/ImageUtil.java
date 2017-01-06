package com.buy.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.buy.string.StringUtil;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageUtil {

	/** 允许最大宽度 */
	public static int MAX_WIDTH = 230;

	/** 允许最大高度 */
	public static int MAX_HEIGHT = 240;

	/**
	 * 默认压缩图后缀
	 */
	public static final String SUFFIX_SMALL = "_small";

	/**
	 * 获取网络图片
	 * 
	 * @param url
	 *            网络图片地址
	 * @param timeOut
	 *            超时时间
	 * @return
	 * @throws Exception
	 */
	public static InputStream getImage(URL url, Integer timeOut)
			throws Exception {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(timeOut);
		return conn.getInputStream(); // 通过输入流获取图片数据
	}

	/**
	 * 对图片进行缩小处理
	 * 
	 * @param in
	 *            原图文件流
	 * @param x
	 *            输出文件【长】
	 * @param y
	 *            输出文件【宽】
	 * @return
	 */
	public static BufferedImage disposeImage(InputStream in, int x, int y) {

		BufferedImage image = null;
		try {
			image = ImageIO.read(in); // 得到图片
		} catch (IOException e) {
		}

		if (null != image) {
			int old_w = image.getWidth(); // 得到源图宽
			int old_h = image.getHeight(); // 得到源图长

			/* 在新的画布上生成原图的缩略图 */
			BufferedImage tempImg = new BufferedImage(old_w, old_h,
					BufferedImage.TYPE_INT_RGB); // 根据原图的大小生成空白画布
			Graphics2D g = tempImg.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, old_w, old_h);
			g.drawImage(image, 0, 0, old_w, old_h, Color.white, null);
			g.dispose();

			BufferedImage newImg = new BufferedImage(x, y,
					BufferedImage.TYPE_INT_RGB);
			// 对图片进行缩小
			newImg.getGraphics().drawImage(
					tempImg.getScaledInstance(x, y, Image.SCALE_SMOOTH), 0, 0,
					null);
			return newImg;
		}
		return null;
	}

	/**
	 * 转换Image数据为byte数组
	 * 
	 * @param image
	 * @param format
	 *            格式字符串.如"jpeg","png"
	 * @return
	 */
	public static byte[] imageToBytes(BufferedImage image, String format) {
		Graphics sbg = image.getGraphics();
		sbg.drawImage(image, 0, 0, null);
		sbg.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, format, out);
		} catch (IOException e) {
		}
		return out.toByteArray();
	}

	/**
	 * 对图片进行缩小处理
	 * 
	 * @param srcImage
	 * @param outImgFile
	 * @param new_w
	 * @param new_h
	 * @param per
	 */
	private static void disposeImage(BufferedImage srcImage, File outImgFile,
			int new_w, int new_h, float per) {

		// BufferedImage src = getImage(srcImgPath); // 得到图片

		int old_w = srcImage.getWidth(); // 得到源图宽
		int old_h = srcImage.getHeight(); // 得到源图长

		/* 在新的画布上生成原图的缩略图 */
		BufferedImage tempImg = new BufferedImage(old_w, old_h,
				BufferedImage.TYPE_INT_RGB); // 根据原图的大小生成空白画布
		Graphics2D g = tempImg.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, old_w, old_h);
		g.drawImage(srcImage, 0, 0, old_w, old_h, Color.white, null);
		g.dispose();

		BufferedImage newImg = new BufferedImage(new_w, new_h,
				BufferedImage.TYPE_INT_RGB);
		// 对图片进行缩小
		newImg.getGraphics().drawImage(
				tempImg.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0,
				0, null);
		// 输出图片文件
		outputImage(newImg, outImgFile, per);
	}

	/**
	 * 图片压缩，通过指定的长度、宽度以及质量比进行压缩
	 * 
	 * @param srcImgFile
	 *            原图片文件
	 * @param outImgFile
	 *            目标图片文件
	 * @param new_w
	 *            新的图片长度
	 * @param new_h
	 *            新的图片宽度
	 * @param per
	 *            质量比（取值为0-1）
	 */
	public static void compress(File srcImgFile, File outImgFile, int new_w,
			int new_h, float per) {
		BufferedImage src = getImage(srcImgFile); // 得到图片
		disposeImage(src, outImgFile, new_w, new_h, per);
	}

	/**
	 * 绘制新图片
	 * 
	 * @param sourceImgFile
	 *            源图片（File）
	 * @param new_w
	 *            新图片宽度
	 * @param new_h
	 *            新图片高度
	 * @return 新图片（BufferedImage）
	 */
	private static BufferedImage disposeImage(BufferedImage sourceImgFile,
			int new_w, int new_h) {
		// 源图大小
		int old_w = sourceImgFile.getWidth();
		int old_h = sourceImgFile.getHeight();
		// 绘制源图
		BufferedImage tempImg = new BufferedImage(old_w, old_h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = tempImg.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, old_w, old_h);
		g.drawImage(sourceImgFile, 0, 0, old_w, old_h, Color.white, null);
		g.dispose();
		// 绘制新图
		BufferedImage newImg = new BufferedImage(new_w, new_h,
				BufferedImage.TYPE_INT_RGB);
		newImg.getGraphics().drawImage(
				tempImg.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0,
				0, null);
		// 返回新图
		return newImg;
	}

	/**
	 * 图片压缩，通过指定的与原图的比例以及质量比进行压缩
	 * 
	 * @param srcImgFile
	 *            原图片文件
	 * @param outImgFile
	 *            目标图片文件
	 * @param ratio
	 *            与原来图片的尺寸比
	 * @param per
	 *            质量比（取值为0-1）
	 */
	public static void compress(File srcImgFile, File outImgFile, float ratio,
			float per) {
		BufferedImage src = getImage(srcImgFile); // 得到图片
		int old_w = src.getWidth(); // 得到源图宽
		int old_h = src.getHeight(); // 得到源图长
		// 根据图片尺寸压缩比得到新图的尺寸
		int new_w = (int) Math.round(old_w * ratio);
		int new_h = (int) Math.round(old_h * ratio);
		disposeImage(src, outImgFile, new_w, new_h, per);
	}

	/**
	 * 图片压缩，通过指定的最大长度以及质量比进行压缩，生成的图片的长度和宽度中的大者等于该最大长度
	 * 
	 * @param srcImgFile
	 *            原图片文件
	 * @param outImgFile
	 *            目标图片文件
	 * @param maxLength
	 *            最大边长
	 * @param per
	 *            质量比（取值为0-1）
	 */
	public static void compress(File srcImgFile, File outImgFile,
			int maxLength, float per) {
		BufferedImage src = getImage(srcImgFile); // 得到图片
		int old_w = src.getWidth(); // 得到源图宽
		int old_h = src.getHeight(); // 得到源图长

		int new_w = 0; // 得到源图长
		int new_h = 0; // 新图的宽

		if (old_w > old_h) {
			// 图片要缩放的比例
			new_w = maxLength;
			new_h = (int) Math.round(old_h * ((float) maxLength / old_w));
		} else {
			new_w = (int) Math.round(old_w * ((float) maxLength / old_h));
			new_h = maxLength;
		}
		disposeImage(src, outImgFile, new_w, new_h, per);
	}

	/**
	 * 压缩图（按新的宽度和高度）
	 * 
	 * @param sourceImgFile
	 *            源图片（File）
	 * @param new_w
	 *            新宽度
	 * @param new_h
	 *            新高度
	 * @param quality
	 *            质量比（取值为0-1）
	 * @return 新图片路径
	 */
	public static String compress(File sourceImgFile, int new_w, int new_h,
			float quality) {
		// 获取图片
		BufferedImage sourceImage = getImage(sourceImgFile);
		if (null == sourceImage) {
			return null;
		}
		String old_name = sourceImgFile.getName(); // 源图名称
		String base_url = sourceImgFile.getParent(); // 名称以上路径
		String new_name = getCompressName(old_name, null); // 新图名称
		String new_url = base_url + "/" + new_name; // 新图路径
		// 重绘
		File newImgFile = new File(new_url);
		BufferedImage newImage = disposeImage(sourceImage, new_w, new_h);
		// 创建压缩图文件
		boolean result = outputImage(newImage, newImgFile, quality);
		// 结果
		return result ? new_url : null;
	}

	/**
	 * 压缩图（按比例）
	 * 
	 * @param sourceImgFile
	 *            源图片（File）
	 * @param ratio
	 *            源图和新图比例
	 * @param quality
	 *            质量比（取值为0-1）
	 * @param newImgName
	 *            图片新名称（空则按默认规则在原图名加后缀）
	 * @return 新图片路径
	 */
	public static String compress(File sourceImgFile, float ratio,
			float quality, String newImgName) {
		// 获取图片
		BufferedImage sourceImage = getImage(sourceImgFile); // 源图
		if (null == sourceImage) {
			return null;
		}
		BufferedImage newImage = null; // 新图

		// 图片大小
		int old_w = sourceImage.getWidth(); // 源图宽度
		int old_h = sourceImage.getHeight(); // 源图高度
		int new_w = (int) Math.round(old_w * ratio); // 新图宽度
		int new_h = (int) Math.round(old_h * ratio); // 新图高度
		// 路径
		String old_name = sourceImgFile.getName(); // 源图名称
		String base_url = sourceImgFile.getParent(); // 名称以上路径
		String new_name = getCompressName(old_name, newImgName); // 新图名称
		String new_url = base_url + "/" + new_name; // 新图路径
		// 重绘
		File newImgFile = new File(new_url);
		newImage = disposeImage(sourceImage, new_w, new_h);
		// 创建压缩图文件
		boolean result = outputImage(newImage, newImgFile, quality);
		// 结果
		return result ? new_url : null;
	}

	/**
	 * 压缩图（按最大宽度）
	 * 
	 * @param sourceImgFile
	 *            源图片（File）
	 * @param maxLength
	 *            最大宽度
	 * @param quality
	 *            质量比（取值为0-1）
	 * @param newImgName
	 *            图片新名称（空则按默认规则在原图名加后缀）
	 * @return 新图片路径
	 */
	public static String compress(File sourceImgFile, Integer maxLength,
			String newImgName) {
		// 获取图片
		BufferedImage sourceImage = getImage(sourceImgFile);
		if (null == sourceImage) {
			return null;
		}
		// 源图大小
		int old_w = sourceImage.getWidth();
		int old_h = sourceImage.getHeight();
		// 新图大小
		int new_w = old_w;
		int new_h = old_h;
		if (maxLength == null) {
			maxLength = new_w;
		}
		if (old_w > old_h) {
			// 图片要缩放的比例
			new_w = maxLength;
			new_h = (int) Math.round(old_h * ((float) maxLength / old_w));
		} else {
			new_w = (int) Math.round(old_w * ((float) maxLength / old_h));
			new_h = maxLength;
		}
		// 路径
		String old_name = sourceImgFile.getName(); // 源图名称
		String base_url = sourceImgFile.getParent(); // 名称以上路径
		String new_name = getCompressName(old_name, newImgName); // 新图名称
		String new_url = base_url + File.separator + new_name; // 新图路径
		// 重绘
		File newImgFile = new File(new_url);
		BufferedImage newImage = disposeImage(sourceImage, new_w, new_h);
		// 质量比为1
		float quality = 0.9f;
		// 创建压缩图文件
		boolean result = outputImage(newImage, newImgFile, quality);
		// 结果
		return result ? new_url : null;
	}

	/**
	 * 将指定图片读取到BufferedImage
	 * 
	 * @param srcImgPath
	 *            图片路径
	 * @return
	 */
	public static BufferedImage getImage(String srcImgPath) {
		return getImage(new File(srcImgPath));
	}

	/**
	 * 将指定图片读取到BufferedImage
	 * 
	 * @param srcImgFile
	 *            图片文件
	 * @return
	 */
	public static BufferedImage getImage(File srcImgFile) {
		BufferedImage srcImage = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(srcImgFile);
			srcImage = javax.imageio.ImageIO.read(in);
		} catch (IOException e) {
			System.out.println("读取图片文件出错！" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return srcImage;
	}

	/**
	 * 将图片按指定质量比输出到指定位置，
	 * 
	 * @param newImg
	 *            源图片
	 * @param outputPath
	 *            目标路径
	 * @param quality
	 *            质量比
	 */
	public static void outputImage(BufferedImage newImg, String outputPath,
			float quality) {
		outputImage(newImg, new File(outputPath), quality);
	}

	/**
	 * 将图片按指定质量比输出到指定文件，
	 * 
	 * @param newImg
	 *            源图片
	 * @param outputFile
	 *            目标文件
	 * @param quality
	 *            质量比
	 */
	public static boolean outputImage(BufferedImage newImg, File outputFile,
			float quality) {
		FileOutputStream newimage = null;
		// 判断输出的文件夹路径是否存在，不存在则创建
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
		// 输出到文件流
		try {
			newimage = new FileOutputStream(outputFile);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
			JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(newImg);
			// 对图片进行质量压缩
			jep.setQuality(quality, true);
			encoder.encode(newImg, jep);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (ImageFormatException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (null != newimage) {
					newimage.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static int getMaxLength(File srcImgFile) {
		BufferedImage image = getImage(srcImgFile);
		int old_w = image.getWidth(); // 得到源图宽
		int old_h = image.getHeight(); // 得到源图长
		int lower_len = MAX_WIDTH;
		if (MAX_WIDTH > MAX_HEIGHT) {
			lower_len = MAX_HEIGHT;
		}
		if (old_w == old_h) {
			if (old_w >= lower_len) {
				return lower_len;
			}
			return old_w;
		} else if (old_w > old_h) {
			if (old_w >= MAX_WIDTH) {
				return MAX_WIDTH;
			}
			return old_w;
		} else {
			if (old_h >= MAX_HEIGHT) {
				return MAX_HEIGHT;
			}
			return old_h;
		}
	}

	/**
	 * 获取图片宽度
	 */
	public static int getWidth(File srcImgFile) {
		BufferedImage image = getImage(srcImgFile);
		return image.getWidth();
	}

	/**
	 * 获取图片高度
	 */
	public static int getHeight(File srcImgFile) {
		BufferedImage image = getImage(srcImgFile);
		return image.getHeight();
	}

	private static String getCompressName(String oldName, String newImgName) {
		int position = oldName.lastIndexOf(".");
		String suffix = oldName.substring(position);
		if (null == newImgName || "".equals(newImgName)) {
			return (oldName.substring(0, position) + ImageUtil.SUFFIX_SMALL + suffix);
		} else {
			return (newImgName + suffix);
		}
	}
	/**
	 * 
	 * @param originalFile 原图
	 * @param resizedFile 新图
	 * @param newHeight 高度（null表示按原来高度不变）
	 * @param quality（null表示按0.9）
	 * @throws IOException
	 * @author huangzq
	 */
	public static void resize(File originalFile, String newName,
			Integer newHeight, Float quality) throws IOException {
		newName = getCompressName(originalFile.getName(), newName);
		File resizedFile = new File(originalFile.getParent() + File.separator
				+ newName);
		if(quality==null){
			quality = 0.9f;
		}
		if (quality > 1) {
			throw new IllegalArgumentException(
					"Quality has to be between 0 and 1");
		}

		ImageIcon ii = new ImageIcon(originalFile.getCanonicalPath());
		Image i = ii.getImage();
		Image resizedImage = null;
		int iWidth = i.getWidth(null);
		int iHeight = i.getHeight(null);
		if (newHeight == null) {
			newHeight = iHeight;
		}

	/*	if (iWidth > iHeight) {
			resizedImage = i.getScaledInstance(newHeight, (newHeight * iHeight)
					/ iWidth, Image.SCALE_SMOOTH);
		} else {
			resizedImage = i.getScaledInstance((newHeight * iWidth) / iHeight,
					newHeight, Image.SCALE_SMOOTH);
		}*/
		resizedImage = i.getScaledInstance((newHeight * iWidth) / iHeight,
				newHeight, Image.SCALE_SMOOTH);

		// This code ensures that all the pixels in the image are loaded.
		Image temp = new ImageIcon(resizedImage).getImage();

		// Create the buffered image.
		BufferedImage bufferedImage = new BufferedImage(temp.getWidth(null),
				temp.getHeight(null), BufferedImage.TYPE_INT_RGB);

		// Copy image to buffered image.
		Graphics g = bufferedImage.createGraphics();

		// Clear background and paint the image.
		g.setColor(Color.white);
		g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
		g.drawImage(temp, 0, 0, null);
		g.dispose();

		// Soften.
		float softenFactor = 0.05f;
		float[] softenArray = { 0, softenFactor, 0, softenFactor,
				1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0 };
		Kernel kernel = new Kernel(3, 3, softenArray);
		ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		bufferedImage = cOp.filter(bufferedImage, null);

		// Write the jpeg to a file.
		FileOutputStream out = new FileOutputStream(resizedFile);

		// Encodes image as a JPEG data stream
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

		JPEGEncodeParam param = encoder
				.getDefaultJPEGEncodeParam(bufferedImage);

		param.setQuality(quality, true);

		encoder.setJPEGEncodeParam(param);
		encoder.encode(bufferedImage);
	}
	/**
	 * 判断图片是否cmyk模式
	 *
	 * @author huangzq 
	 * @date 2016年7月19日 下午4:33:30
	 * @param filename
	 * @return
	 */
	public static boolean isCMYK(File file) 
    { 
        boolean result = false; 
        BufferedImage img = null; 
        try 
        { 
            img = ImageIO.read(file); 
        } 
        catch (IOException e) 
        { 
            System.out.println(e.getMessage()); 
            result = true;
        } 
        if (img != null) 
        { 
            int colorSpaceType = img.getColorModel().getColorSpace().getType(); 
            result = colorSpaceType == ColorSpace.TYPE_CMYK; 
        } 
 
        return result; 
    } 
	

	public static void main(String[] args) {
		/*
		 * File src = new File("d:\\image004.png"); File dest = new
		 * File("d:\\image004_1.jpg"); ImageUtil.compress(src, dest, 1F, 1F);
		 */
		//File src = new File("C:\Users\allon\Desktop\图片\首页圣餐用酒.jpg");
		//ImageUtil.compress(src, 0.1F, 0.8F, null);
		File file  =  new File("E:\\1.jpg");
		System.out.println(ImageUtil.isCMYK(file));
	}

}
