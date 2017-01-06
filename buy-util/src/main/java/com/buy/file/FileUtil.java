/**
 *
 *  @filename  FileUtil.java
 *  @author  <a href="mailto:loger.luo@m-time.com">Loger</a>
 *  @version  2.0
 */
/**
 * 
 */
package com.buy.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.buy.string.StringUtil;
import com.jfinal.upload.UploadFile;



/**
 * @author Loger
 * 
 */
public class FileUtil {
	private final static String TXT_EXTENSION = "txt";
	private final static String XLS_EXTENSION = "xls";
	private final static String ZIP_EXTENSION = "zip";
	private final static String RAR_EXTENSION = "rar";
	
	/**
	 * 图片类型
	 */
	public static final String IMAGE_FILE_TYPE = ".jpg,.jpeg,.png,.gif";

	public static boolean isTXTFile(String fileName) {
		String extension = getFileExtension(fileName);
		return TXT_EXTENSION.equalsIgnoreCase(extension);
	}

	public static boolean isXLSFile(String fileName) {
		String extension = getFileExtension(fileName);
		return XLS_EXTENSION.equalsIgnoreCase(extension);
	}

	public static boolean isZIPFile(String fileName) {
		String extension = getFileExtension(fileName);
		return ZIP_EXTENSION.equalsIgnoreCase(extension);
	}

	public static boolean isRARFile(String fileName) {
		String extension = getFileExtension(fileName);
		return RAR_EXTENSION.equalsIgnoreCase(extension);
	}

	public static String getFileExtension(String fileFullName) {
		int index = fileFullName.lastIndexOf(".");
		if (index != -1 && index != fileFullName.length()) {
			return fileFullName.substring(index + 1).toLowerCase();
		}
		return null;
	}

	public static String getFileNameWithExtension(String fileFullName) {
		String fileName = fileFullName;
		int index = getLastIndexOfSeperator(fileFullName);
		if (index != -1 && index != fileFullName.length()) {
			fileName = fileFullName.substring(index + 1);
		}
		return fileName;
	}

	public static String getFileNameNoExtension(String fileFullName) {
		String fileName = fileFullName;
		int index = getLastIndexOfSeperator(fileFullName);
		if (index != -1 && index != fileFullName.length()) {
			fileName = fileName.substring(index + 1);
		}
		index = fileName.lastIndexOf(".");
		if (index != -1 && index != fileFullName.length()) {
			fileName = fileName.substring(0, index);
		}
		return fileName;
	}

	protected static int getLastIndexOfSeperator(String fileFullName) {
		int index = fileFullName.lastIndexOf("\\");
		int index1 = fileFullName.lastIndexOf("/");
		return index > index1 ? index : index1;
	}

	public static void deleteFileOrDir(File file) {
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
			if (subFiles.length > 0) {
				for (int i = 0; i < subFiles.length; i++) {
					deleteFileOrDir(subFiles[i]);
				}
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param fullFileName
	 * @param source
	 * @param destDir
	 * @return String(文件名)
	 */
	public static String upLoadFile(String fullFileName, InputStream source,
			String destDir) {
		// 以时间为文件名
		String newFileName = Long.toString(System.currentTimeMillis(), 16)
				+ "." + getFileExtension(fullFileName);
		try {
			copyFileAndMakeDir(source, destDir, newFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFileName;
	}

	/**
	 * 上传文件
	 * 
	 * @param newFileName
	 *            新文件名（带后缀）
	 * @param source
	 * @param destDir
	 * @return String(文件名)
	 */
	public static String uploadFileWithNewName(String newFileName,
			InputStream source, String destDir) {
		// 以时间为文件名

		try {
			copyFileAndMakeDir(source, destDir, newFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFileName;
	}

	public static void makeParent(File file) throws IOException {
		File parent = file.getParentFile();
		if (null == parent) {
			return;
		} else if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new IOException("make dir [ " + parent.getAbsolutePath()
						+ " ] fail");
			}
		}
	}
	/**
	 * 复制文件
	 * @param source
	 * @param destDir
	 * @param newFileName
	 * @return
	 * @throws IOException
	 */
	public static String copyFile(InputStream source, String destDir,
			String newFileName) throws IOException {
		File dir = new File(destDir);
		if (!dir.exists()) {
			throw new IOException("dest dir (" + destDir + ") does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IOException("dest dir (" + destDir + ") is not a folder");
		}
		String destFileFullName = null;
		BufferedOutputStream out = null;
		try {
			destFileFullName = destDir + File.separator + newFileName;
			out = new BufferedOutputStream(new FileOutputStream(
					destFileFullName));
			byte[] buffer = new byte[8192];
			int bytesRead = 0;
			while ((bytesRead = source.read(buffer, 0, 8192)) != -1) {
				out.write(buffer, 0, bytesRead);
			}

		} finally {
			if (null != out) {
				out.close();
			}
		}
		return destFileFullName;
	}
	/**
	 * 复制文件
	 * @param sourceFileFullName
	 * @param destDir
	 * @param newFileName
	 * @return
	 * @throws IOException
	 */
	public static String copyFile(String sourceFileFullName, String destDir,
			String newFileName) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFileFullName);
			return copyFile(in, destDir, newFileName);
		} finally {
			if (null != in) {
				in.close();
			}
		}

	}
	/**
	 * 复制文件
	 * @param sourceFileFullName
	 * @param destDir
	 * @param newFileName
	 * @return
	 * @throws IOException
	 */
	public static String copyFileAndMakeDir(String sourceFileFullName,
			String destDir, String newFileName) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFileFullName);
			return copyFileAndMakeDir(in, destDir, newFileName);
		} finally {
			if (null != in) {
				in.close();
			}
		}

	}
	/**
	 * 复制文件
	 * @param sourceFileFullName
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public static String copyFile(String sourceFileFullName, String destDir)
			throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFileFullName);
			return copyFile(in, destDir,
					getFileNameWithExtension(sourceFileFullName));
		} finally {
			if (null != in) {
				in.close();
			}
		}
	}

	public static String copyFileAndMakeDir(String sourceFileFullName,
			String destDir) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFileFullName);
			return copyFileAndMakeDir(in, destDir,
					getFileNameWithExtension(sourceFileFullName));
		} finally {
			if (null != in) {
				in.close();
			}
		}
	}

	public static String copyFileAndMakeDir(InputStream source, String destDir,
			String newFileName) throws IOException {
		File dir = new File(destDir);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("can not make dest dir (" + destDir + ")");
			}
		}
		return copyFile(source, destDir, newFileName);
	}

	

	/**
	 * 判断网络文件是否存在
	 * 
	 * @return
	 */
	public static boolean judgeFileExists(URL url) {
		int state = 404;
		try {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			state = connection.getResponseCode();
		} catch (IOException e) {
			state = 404;
		}
		return 200 == state ? true : false;
	}

	/**
	 * FileUtils Constructor
	 * 
	 * @param
	 * @see
	 */
	protected FileUtil() {
		super();
	}

	/**
	 * 创建目录
	 * 
	 * @param dirPath
	 */
	public static void createDir(String dirPath) {
		File f = new File(dirPath);
		if (!f.exists()) {
			f.mkdirs();
		}
	}


	/**
	 * 文件复制
	 * 
	 * @param srcFile源文件
	 * @param destFilePath
	 *            目标文件
	 */
	public static void copyFiles(File srcFile, String destFilePath) {

		InputStream is = null;
		OutputStream os = null;

		try {
			is = new FileInputStream(srcFile);
			os = new FileOutputStream(destFilePath);

			byte[] buff = new byte[1024 * 1024];
			int len = 0;
			while (-1 != (len = is.read(buff))) {
				os.write(buff, 0, len);
			}
			os.flush();

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != os) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 递归刪除文件，文件夾
	 * 
	 * @param dirPath
	 */
	public static boolean delete(String dirPath) {
		File f = new File(dirPath);

		if (!f.exists()) {
			return false;
		}

		if (f.isDirectory()) {
			return true;
/*
			File[] fileArr = f.listFiles();
			for (File item : fileArr) {
				delete(item.getAbsolutePath());
			}
			return f.delete();*/

		} else {
			return f.delete();
		}
	}

	 /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String readFileByLines(String fileName,String rtf) {
    	if(null == rtf){
    		rtf = "utf=8";
    	}
    	StringBuffer ruleString = new StringBuffer();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            //以行为单位读取文件内容，一次读一整行
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),rtf));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while (null != (tempString = reader.readLine())) {
                // 显示行号
            	ruleString.append(tempString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return ruleString.toString();
    }
	
    /**
     * 单个文件判断是否为图片
     * @param fileType
     * @return true：是图片
     * @author chenhg
     * 2016年8月9日 上午11:51:35
     */
    public static boolean isImageType(String fileType){
		for(String type : IMAGE_FILE_TYPE.split(",")){
			if(type.equalsIgnoreCase(fileType)){
				return true;
			}
		}
		return false;
	}
    
    /**
     * 多个上传文件，判断是否为都为图片格式
     * @param picFile
     * @return true：都为图片
     * @author chenhg
     * 2016年8月9日 上午11:52:14
     */
    public static boolean allFilesIsImg(List<UploadFile> picFile){
		for(int i=0;i<picFile.size();i++){
			String curFileName = picFile.get(i).getFileName(); 
			String fileType = StringUtil.getFileType(curFileName);
			if(!isImageType(fileType)){
				return false;
			}
		}
		return true;
    }
    
    /**
     * 图片打包成压缩包
     * @param picFile
     * @param zipFileName
     * @return
     * @author chenhg
     * 2016年8月9日 上午11:56:33
     */
    public static Map<String, String> imgsToZip(List<UploadFile> picFile, String zipFileName){
    	Map<String, String> result = new HashMap<String, String>();
    	//判断是否都是图片
    	boolean isImg = allFilesIsImg(picFile);
    	if(!isImg){
    		result.put("status", "1");
    		result.put("msg", "不能包含非图片文件");
    		return result;
    	}
    	result = filesToZip(picFile, zipFileName);
    	return result;
    }
    
    /**
     * 上传文件打包成压缩包zip
     * @param picFile
     * @param zipFileName
     * @return
     * @author chenhg
     * 2016年8月9日 上午10:33:42
     */
    public static Map<String, String> filesToZip(List<UploadFile> picFile, String zipFileName){
    	Map<String, String> result = new HashMap<String, String>();
    	ZipOutputStream out = null;
    	try{
    		out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName))); 
    		byte[] buffer = new byte[1024];
    		
    		for(int i=0;i<picFile.size();i++) {   
    			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(picFile.get(i).getFile()));   
    			out.putNextEntry(new ZipEntry(picFile.get(i).getFileName()));   
    			int len;   
    			//打包到zip文件   
    			while((len = fis.read(buffer))>0) {   
    				out.write(buffer,0,len);    
    			}   
    			out.closeEntry();   
    			fis.close();   
    		}   
    		out.close(); 
    		deleteAllFile(picFile);
    	}catch (Exception e){
    		System.out.println("打包成压缩包发生异常");
    		e.printStackTrace();
    		result.put("status", "1");
    		result.put("msg", "打包成压缩包发生异常");
    		return result;
    	}finally{
    		if(out != null){
    			try {
					out.close();
				} catch (IOException e) {
					System.out.println("关闭流异常");
				}
    		}
    	}
    	result.put("status", "0");
		result.put("msg", "打包成功");
    	return result;
    }
    
    /**
     * 删除需要压缩的文件
     * @param picFile
     * @author chenhg
     * 2016年8月9日 下午1:38:40
     */
    public static void deleteAllFile(List<UploadFile> picFile){
    	
    	for(int i=0;i<picFile.size();i++) { 
    		picFile.get(i).getFile().delete();
    	}
    }
}
