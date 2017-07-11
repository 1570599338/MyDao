/**
 * 
 */
package snt.common.web.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import snt.common.string.chinese.ChineseConvertor;
import snt.common.encrypt.EncodeUtil;
import snt.common.web.util.Constants;
import snt.common.web.util.WebUtils;

/**
 * Http请求的一个包装器<br>
 * 此包装器负责的工作是：<br>
 * 对于加密的请求参数进行解密，解密后的参数放入请求的参数列表<br>
 * 对于请求参数中的繁体中文进行转码，得到对应的简体中文参数<br>
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class HttpServletRequestDecorator extends HttpServletRequestWrapper{
	private static Log log = LogFactory.getLog(HttpServletRequestDecorator.class);
	/**
	 * 把Iterator包装成Enumeration的一个包装类
	 */
	private static class IteratorWrapper implements Enumeration{
		private Iterator it;
		public IteratorWrapper(Iterator it){
			this.it = it;
		}
		public boolean hasMoreElements() {
			return it.hasNext();
		}

		public Object nextElement() {
			return it.next();
		}
		
	}
	/**解析加密参数的正则表达式*/
	private static Pattern queryStringRegex = Pattern.compile("([^=&]+)\\s*\\=\\s*([^=&]+)\\s*&?\\s*", Pattern.CANON_EQ);
	/**
	 * 转换对象的方法
	 * @param <T>
	 * @param obj
	 * @return 转换后的对象
	 */
	@SuppressWarnings("unchecked")
	private static <T> T convertObject(T obj){
		try {
			return (T)ChineseConvertor.getInstance().tc2sc((String)obj);
		} catch (Exception e) {
			return obj;
		}
	}
	/**
	 * 转换字符数组的方法
	 * @param strs
	 * @return 转换后的字符数组
	 */
	private static String[] convertStrs(String[] strs){
		if(strs != null){
			int index = 0;
			for (String string : strs) {
				strs[index++]=convertObject(string);
			}
		}
		return strs;
	}
	/**上传文件Map*/
	private Map<String, FileItem> fileItemMap = null;
	/**参数Map*/
	private Map<String, String[]> paramMap = new HashMap<String, String[]>();
	private MultipartRequestHandler multipartRequestHandler;


	/**
	 * @param arg0
	 */
	public HttpServletRequestDecorator(HttpServletRequest arg0) {
		super(arg0);
		handleParam();//TODO 暂欠考虑，可能需要把这句放到setRequest方法里。
		handleMultipart();
	}

	/**
	 * 解密加密了的请求参数
	 * @param paramValues
	 */
	private void decodeEncodedParam(String[] paramValues){
		paramMap.put(Constants.ENCODEDPARAMNAME, paramValues);
		if (paramValues != null && paramValues.length>0) {
			String pv = paramValues[0];
			try {
				if (pv.length()>0) {
					String queryString = EncodeUtil.decodeStr(pv);
					Matcher regexMatcher = queryStringRegex.matcher(queryString);
					while (regexMatcher.find()) {
						//未对同名参数进行处理
						String key = regexMatcher.group(1);
						String value= regexMatcher.group(2);
						String[] values = value.split(",");
						paramMap.put(key, values);
					}
				}
			} catch (Exception e) {
				log.error("解密参数出错", e);
			}
		}
	}

	public FileItem getFileItem(String name){
		return fileItemMap==null?null:fileItemMap.get(name);
	}
	
	/**
	 * 得到对应的请求参数，这里返回的参数值已经被转码
	 * @param arg0
	 * @return 对应的请求参数值
	 */
	public String getParameter(String arg0) {
		String[] paramValues = getParameterValues(arg0);
		if (paramValues != null && paramValues.length>0) {
			return paramValues[0];
		} else{
			return null;
		}
	}

	/**
	 * @return 请求参数Map
	 */
	public Map getParameterMap() {
		return paramMap;
	}

	/**
	 * @return 请求参数名的枚举，这里返回的枚举包括解密请求参数之后的参数名
	 */
	public Enumeration getParameterNames() {
		return new IteratorWrapper(paramMap.keySet().iterator());
	}
	
	/**
	 * @return 得到对应的请求参数值数组
	 */
	public String[] getParameterValues(String arg0) {
		return paramMap.get(arg0);
	}
	
	/**
	 * 处理请求中可能的multipart部分
	 *
	 */
	private void handleMultipart(){
		if (!"POST".equalsIgnoreCase(getMethod())) {
			return;
		}
		
		String contentType = getContentType();
		if ((contentType != null) &&
				contentType.startsWith("multipart/form-data")) {
			multipartRequestHandler = new MultipartRequestHandler();
			try {
				multipartRequestHandler.handleRequest(this);
			} catch (ServletException e) {
				log.error("处理Multi part的请求出错", e);
			}
		}
	}
	
	/**
	 * 处理请求中的参数
	 */
	private void handleParam(){
		boolean needConvert = WebUtils.isNeedConvertChinese();
		Map map = getRequest().getParameterMap();
		for (Object key : map.keySet()) {
			String paramName = (String)key;
			String[] paramValue = (String[])map.get(key);
			if (Constants.ENCODEDPARAMNAME.equals(paramName)) {
				decodeEncodedParam(paramValue);
			} else {
				paramMap.put(paramName, needConvert?convertStrs(paramValue):paramValue);
			}			
		}
	}
	
	void setFileItem(Map<String, FileItem> fileItemMap){
		this.fileItemMap = fileItemMap;
	}
	
	public void setParameters(Map<String, String[]> paramMap){
		if (this.paramMap == null) {
			this.paramMap = paramMap;
		} else {
			this.paramMap.putAll(paramMap);
		}		
	}
	
	@Override
	public String toString() {
		return "Bizteller Request";		
	}
}
