/**
 * 
 */
package snt.common.web.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.struts.upload.MultipartRequestWrapper;

import snt.common.web.filter.HttpServletRequestDecorator;

/**
 * 上传文件的处理类
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * Create Time:2006-6-15 
 */
public class FileUtil {
	private static boolean inited;
	private static Log log = LogFactory.getLog(FileUtil.class);
	private static long maxFileSize;
	private static String repositoryFilePath;
	private static int thresholdFileSize;
	
	private static void init(){
		if (!inited) {
			maxFileSize = convertSizeToBytes(WebUtils.getModuleProperty("upload.maxFileSize"), 20*1024*1024);
			thresholdFileSize = (int)convertSizeToBytes(WebUtils.getModuleProperty("upload.thresholdFileSize"), 250*1024);
			repositoryFilePath = WebUtils.getModuleProperty("upload.repository");
			inited = true;
		}
	}
	
	public static String saveFile(FileItem item, String path) throws Exception{
		if (!item.isFormField()) {
			String name = item.getName();
			long size = item.getSize();
			if((name==null||name.equals("")) && size==0)
				return null;
			//保存上传的文件到指定的目录
			File srcfile = new File(name);
			//防止浏览器强制用UTF-8发送请求，所以将中文转成拼音
			//临时！！！ TODO
//			String srcFileName = ChsUtil.getPinYin(srcfile.getName(), true);
			File destFile = new File(path+"/"+srcfile.getName());
			if (!destFile.getParentFile().canWrite()) {
				destFile.getParentFile().mkdirs();
			}
			item.write(destFile);
			return srcfile.getName();
		}
		return null;
	}

	/**
	 * HTTP协议上传操作
	 * @param request
	 * @param path
	 * @return 上传的文件名数组
	 * @throws Exception
	 */
	/*public static String[] upload(HttpServletRequest request, String path) throws Exception{
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new String[0];
        }
		init();
		
		//It's so ugly!!!!! But I have no good idea!
		if(request instanceof MultipartRequestWrapper){
			request = ((MultipartRequestWrapper)request).getRequest();
		}
		
		if(request instanceof HttpServletRequestDecorator){
			return upload((HttpServletRequestDecorator)request, path);
		}
		
		DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		//设置缓存目录
		diskFileItemFactory.setRepository(new File(repositoryFilePath));
		//设置允许在内存中存储的数据，单位：字节
		diskFileItemFactory.setSizeThreshold(thresholdFileSize);
		//初始化FILEUPLOAD组件的ServletFileUpload类
		ServletFileUpload diskFileUpload = new ServletFileUpload(diskFileItemFactory);
		
		diskFileUpload.setHeaderEncoding(request.getCharacterEncoding());
		//设置允许用户上传的文件大小，单位：字节
		diskFileUpload.setSizeMax(maxFileSize);

		//开始读取上传信息
		List<String> fileNameList = new ArrayList<String>();
		try{
			//解析REQUEST，得到文件域列表
			List fileItems = diskFileUpload.parseRequest(request);
			//依次处理每个上传的文件
			Iterator iter = fileItems.iterator();
			while (iter.hasNext()) {
			  FileItem item = (FileItem) iter.next();
			  //忽略其他不是文件域的所有表单信息
			  String fileName = saveFile(item, path);
			  if(fileName != null){
				  fileNameList.add(fileName);
			  }
			}
		}catch(Exception e){
			log.error("保存上传文件失败!", e);
			throw e;
		}
		return fileNameList.toArray(new String[fileNameList.size()]);
	}*/
	
	private static String[] upload(HttpServletRequestDecorator request, String path) throws Exception{
		List<String> fileNameList = new ArrayList<String>();
		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = (String) enumeration.nextElement();
			FileItem fileItem = request.getFileItem(paramName);
			if (fileItem != null) {
				String fileName = saveFile(fileItem, path);
				if(fileName != null){
					fileNameList.add(fileName);
				}
			}
		}
		return fileNameList.toArray(new String[fileNameList.size()]);
	}
	
	public static void download(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String filePath) throws Exception{
		File downloadFile = new File(filePath);
        if (downloadFile.canRead()) {
        	String fileName = new String(downloadFile.getName().getBytes("GBK"),"ISO-8859-1");
        	httpResponse.setHeader("Expires", "0");
        	httpResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        	httpResponse.setHeader("Pragma", "public");
        	httpResponse.setHeader("Content-disposition","attachment; filename="+fileName);
        	
        	httpResponse.setContentType(WebUtils.getMimeType(downloadFile.getName()));
			//the contentlength is needed for MSIE!!!
        	httpResponse.setContentLength((int)downloadFile.length());
        	ReadableByteChannel ic = null;
        	WritableByteChannel oc = null;
        	try {
				ic = Channels.newChannel(new BufferedInputStream(new FileInputStream(downloadFile)));        	
				oc = Channels.newChannel(new BufferedOutputStream(httpResponse.getOutputStream()));
				ByteBuffer buf = ByteBuffer.allocate(4096);
				while (ic.read(buf) != -1) {
					buf.flip();
					oc.write(buf);
					buf.clear();
				}
			} finally {
				if (ic != null) {
					ic.close();
				}
				if (oc != null) {
					oc.close();
				}
			}        	
		}
	}
	
	/**
     * Converts a size value from a string representation to its numeric value.
     * The string must be of the form nnnm, where nnn is an arbitrary decimal
     * value, and m is a multiplier. The multiplier must be one of 'K', 'M' and
     * 'G', representing kilobytes, megabytes and gigabytes respectively.
     *
     * If the size value cannot be converted, for example due to invalid syntax,
     * the supplied default is returned instead.
     *
     * @param sizeString  The string representation of the size to be converted.
     * @param defaultSize The value to be returned if the string is invalid.
     *
     * @return The actual size in bytes.
     */
	public static long convertSizeToBytes(String sizeString, long defaultSize) {
		if(sizeString == null){
			return defaultSize;
		}
		
		int multiplier = 1;

		if (sizeString.endsWith("K")) {
			multiplier = 1024;
		} else if (sizeString.endsWith("M")) {
			multiplier = 1024 * 1024;
		} else if (sizeString.endsWith("G")) {
			multiplier = 1024 * 1024 * 1024;
		}
		if (multiplier != 1) {
			sizeString = sizeString.substring(0, sizeString.length() - 1);
		}

		long size = 0;
		try {
			size = Long.parseLong(sizeString);
		} catch (NumberFormatException nfe) {
			log.warn("Invalid format for file size ('" + sizeString
					+ "'). Using default.");
			size = defaultSize;
			multiplier = 1;
		}

		return (size * multiplier);
	}
}
