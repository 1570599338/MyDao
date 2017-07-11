package snt.common.web.util;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import snt.common.i18n.IMessageResources;
import snt.common.web.filter.HttpServletRequestDecorator;


/**
 * Web应用工具类
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class WebUtils{
	// 日志
	private static Log log = LogFactory.getLog(WebUtils.class);
	
	/**
	 * 语种信息 
	 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
	 */
	public static class LocalInfo{
		// 获取当前的环境的语言信息
		private Locale  curLocale = Locale.getDefault();
		private boolean needConvertChinese = false;

		/**
		 * @return 当前Locale
		 */
		public Locale getCurLocale() {
			return curLocale;
		}
		/**
		 * @return 是否需要转换中文字符
		 */
		public boolean isNeedConvertChs(){
			return needConvertChinese;
		}		
		/**
		 * 设置当前Locale
		 * @param curLocale
		 */
		public void setCurLocale(Locale curLocale) {
			this.curLocale = curLocale;
		}
		/**
		 * 设置是否需要转换中文字符
		 * @param needConvertChinese
		 */
		public void setNeedConvertChs(boolean needConvertChinese){
			this.needConvertChinese = needConvertChinese;
		}		
	}
	
	/**
	 * web应用配置的属性
	 */
	static Properties moduleProperties;
	static ServletContext servletContext;
	public static IMessageResources messageResources;
	
	/**和线程绑定的HttpServletRequest记录器*/
	private static ThreadLocal<HttpServletRequest> curRequest = new ThreadLocal<HttpServletRequest>();
	/**和线程绑定的HttpSession记录器*/
	private static ThreadLocal<HttpSession> curSession = new ThreadLocal<HttpSession>();
	
	/**和当前线程绑定的语种信息*/
	private static ThreadLocal<LocalInfo> localInfo = new ThreadLocal<LocalInfo>(){
		@Override
		protected LocalInfo initialValue() {
			return new LocalInfo();
		}
	};
	
	 private static IMessageResources getMessageResources()
	  {
	    if (messageResources == null) {
	      String clsName = System.getProperty("MessageResources");
	      try {
	        messageResources = (IMessageResources)Class.forName(clsName).getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
	      } catch (Exception e) {
	        throw new IllegalStateException("语法错误：messageResources", e);
	      }
	    }
	    return messageResources;
	  }
	 
		private static Object[] handleMsgParams(Object[] args){
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (args[i] != null && !(args[i] instanceof String)) {
						args[i] = String.valueOf(args[i]);
					}
				}
			}
			return args;
		}
		
	 /**
	 * 取得web应用配置的属性值
	 * @param key
	 * @return web应用配置的属性值
	 */
	  public static String getModuleProperty(String key)
	  {
	    return moduleProperties == null ? null : moduleProperties.getProperty(key);
	  }
	 
		  public static String getModuleProperty(String key, Object[] args) {
			    args = handleMsgParams(args);
			    String formatString = getModuleProperty(key);
			    if ((formatString == null) || (args == null) || (args.length == 0)) {
			      return formatString;
			    }
			    MessageFormat format = new MessageFormat(formatString);
			    return format.format(args);
			  }
		  
		  
		 /**
		 * 得到当前的会话对象<br>
		 * 适合在需要确保有会话对象的情况下使用，如果当前没有相关会话对象，则创建一个，并注册到当前线程。<br>
		 * 不出意外，此方法必定返回一个会话对象。<br>
		 * 在调用本方法后，当前线程之后需要获取会话对象的地方可以直接调用getSession()来获取。<br>
		 * @return 当前会话对象
		 */
		  private static HttpSession getOrCreateSession()
		  {
		    HttpSession session = (HttpSession)curSession.get();
		    if (session == null) {
		      session = ((HttpServletRequest)curRequest.get()).getSession();
		      curSession.set(session);
		    }
		    return session;
		  }
		  
		  /**
			 * 从当前会话中得到对象，如果当前没有建立会话对象或者会话中不存在该key，则返回null
			 * @param key
			 * @return 会话中存储的数据
			 */
			public static Object getSessionData(String key){
				HttpSession session = getSession();
				try {
					return session==null?null:session.getAttribute(key);
				} catch (IllegalStateException e) {
					log.error("获取会话数据出错！", e);
					return null;
				}
			}
		  
		  /**
		 * 得到会话ID
		 * @return 会话ID
		 */
		public static String getSessionID(){
			HttpSession session = getSession();
			try {
				return session==null?null:session.getId();
			} catch (IllegalStateException e) {
				log.error("获取会话数据出错！", e);
				return null;
			}
		}
			
		/**
		 * 从请求中取得参数值<br>
		 * 先在请求的Parameter里找，如果找不到，则在Attributes里找
		 * @param request
		 * @param name
		 * @return 请求中对应的参数或属性
		 */
		  public static String getParameterOrAttribute(HttpServletRequest request, String name)
		  {
		    String value = request.getParameter(name);
		    if (value == null) {
		      Object obj = request.getAttribute(name);
		      value = obj == null ? null : obj.toString();
		    }
		    return value;
		  }
		  
		/**
		 * 获取当前的请求的Url
		 * @return
		 */
		  public static String getRequestURI() {
			    HttpServletRequest request = (HttpServletRequest)curRequest.get();
			    if (request != null) {
			      return request.getRequestURI();
			    }
			    return null;
			  }
		  /*
		   * 获取路由信息
		   */
		  public static String getRemoteAddr()
		  {
		    HttpServletRequest request = (HttpServletRequest)curRequest.get();
		    if (request != null) {
		      return request.getRemoteAddr();
		    }
		    return null;
		  }

		  /**
		 * 得到当前的会话对象<br>
		 * 适合在不能直接取得HttpServletRequest对象的场合下使用<br>
		 * 如果当前没有会话对象，则返回null<br>
		 * @return 当前会话对象
		 */
		  private static HttpSession getSession()
		  {
		    return (HttpSession)curSession.get();
		  }
		  
		 
		  
		/**
		 * 从会话中得到弱引用保持的数据<br>
		 * 注意，这种数据是可以被JVM自动回收的，有可能存入值之后经过一段时间就被回收了，再次<br>
		 * 取的话返回为null，所以，调用这个方法一定要判断返回值是否为null。<br>
		 * @param key
		 * @return 会话中弱引用保持的数据
		 */
		@SuppressWarnings("unchecked")
		public static Object getWeakSessionData(Object key){
			HttpSession session = getSession();
			if (session == null) {
				return null;
			}
			try {
				WeakDataMap<Object, Serializable> weakMap = (WeakDataMap<Object, Serializable>)session.getAttribute(Constants.WEAKREFDATA);
				return weakMap==null?null:weakMap.get(key);
			} catch (IllegalStateException e) {
				log.error("获取会话数据出错！", e);
				return null;
			}
		}
		
		/**
		 * 得到Web应用的根目录（绝对路径）
		 * @return Web应用的根目录
		 */
		public static String getWebRootPath(){
			return servletContext.getRealPath("/");
		}
		
		/**
		 * 强制当前会话失效
		 */
		public static void invalidateSession(){
			HttpSession session = getSession();
			if(session != null){
				session.invalidate();
				curSession.set(null);
			}
		}
		
		/**
		 * 判断当前线程是否需要转换中文（即繁体语种的用户）<br>
		 * 只有过滤器需要用到，其它模块不应该处理转换相关的事宜
		 * @return 当前线程是否需要转换中文
		 */
		public static boolean isNeedConvertChinese() {
			return localInfo.get().isNeedConvertChs();
		}

		/**
		 * 输出异常到jsp页面（开发期间使用）
		 * @param throwable
		 * @param out
		 * @throws IOException
		 */
		public static void printOutThrowable(Throwable throwable, JspWriter out) throws IOException{
			out.print(throwable);
			out.println("<br>");
	        StackTraceElement[] trace = throwable.getStackTrace();
	        for (int i=0; i < trace.length; i++){
	        	out.print("\tat " + trace[i]);
	        	out.println("<br>");
	        }

	        Throwable ourCause = throwable.getCause();
	        if (ourCause != null)
	            printStackTraceAsCause(out, ourCause, trace);
		}
		
		/**
		 * 输出根异常到jsp页面（开发期间使用）
		 * @param out
		 * @param throwable
		 * @param causedTrace
		 * @throws IOException
		 */
		private static void printStackTraceAsCause(JspWriter out, Throwable throwable,
				StackTraceElement[] causedTrace) throws IOException{
			//Compute number of frames in common between this and caused
			StackTraceElement[] trace = throwable.getStackTrace();
			int m = trace.length-1, n = causedTrace.length-1;
			while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
				m--; n--;
			}
			int framesInCommon = trace.length - 1 - m;
			
			out.print("Caused by: " + throwable);
			out.println("<br>");
			for (int i=0; i <= m; i++){
				out.print("\tat " + trace[i]);
				out.println("<br>");
			}
			if (framesInCommon != 0){
				out.print("\t... " + framesInCommon + " more");
				out.println("<br>");
			}
			
			//Recurse if we have a cause
			Throwable ourCause = throwable.getCause();
			if (ourCause != null)
				printStackTraceAsCause(out, ourCause, trace);
		}
		
		/**
		 * 注册当前语种信息到当前线程<br>
		 * 只有过滤器需要调用，其它模块不应该调用该方法。
		 * @param locale
		 */
		public static void registerLocale2CurThread(Locale locale){
			boolean cht = Locale.TRADITIONAL_CHINESE.equals(locale)||Locale.TAIWAN.equals(locale);
			LocalInfo lInfo = localInfo.get();
			lInfo.setCurLocale(locale);
			lInfo.setNeedConvertChs(cht);//如果是繁体，必须要转换字符
		}
		
		/**
		 * 注册请求和会话对象到当前线程
		 * @param httpRequest
		 */
		public static void registerRequest(HttpServletRequest httpRequest){
			curRequest.set(httpRequest);
			HttpSession session = httpRequest.getSession(false);
			curSession.set(session);
		}
		
		/**
		 * 从会话中删除key对应的对象
		 * @param key
		 */
		public static void removeSessionData(String key){
			HttpSession session = getSession();
			if(session != null){
				session.removeAttribute(key);
			}
		}
		
		/**
		 * 在会话中删除弱引用保持的数据
		 * @param key
		 */
		public static void removeWeakSessionData(Object key){
			HttpSession session = getSession();
			if (session != null) {
				Map weakMap = (Map)session.getAttribute(Constants.WEAKREFDATA);
				if (weakMap != null) {
					weakMap.remove(key);
				}
			}		
		}
		
		/**
		 * 继续原来挂起的请求处理
		 * @param request
		 * @return boolean 如果复原原来的请求成功，则返回true，否则，返回false。
		 * @throws IOException 
		 * @throws Exception 
		 */
		@SuppressWarnings("unchecked")
		public static boolean restoreHttpServletRequest(HttpServletRequest request, HttpServletResponse response) throws Exception{
			String requestURL = (String)getSessionData(SessionKeyConstants.REQUESTURL_BEFORELOGIN);
			if (requestURL != null) {
				if (request instanceof HttpServletRequestDecorator) {
					Map<String, String[]> paramMap = (Map<String, String[]>)getSessionData(SessionKeyConstants.REQUESTPARAMS_BEFORELOGIN);
					if (paramMap != null) {
						((HttpServletRequestDecorator)request).setParameters(paramMap);
					}
				}
				int index = 0;
				//第一个/处即是请求资源开始处（刨去前面的网站地址）
				Pattern p = Pattern.compile("[^/]/[^/]");
				Matcher m = p.matcher(requestURL);
				if(m.find()){
					index = m.start()+1; 
				}
				if (index > 0) {
					requestURL = requestURL.substring(index+request.getContextPath().length());
				}
				request.getRequestDispatcher(requestURL).forward(request, response);
				removeSessionData(SessionKeyConstants.REQUESTURL_BEFORELOGIN);
				removeSessionData(SessionKeyConstants.REQUESTPARAMS_BEFORELOGIN);
				return true;
			} else {
				return false;
			}		
		}
		
		/**
		 * 保存当前的请求资源和参数，挂起当前请求的处理，等待之后的复原
		 * @param request
		 */
		@SuppressWarnings("unchecked")
		public static void saveHttpServletRequest(HttpServletRequest request){
			String requestURL = request.getRequestURL().toString();
			int index = requestURL.indexOf("?");
			if (index > 0) {
				requestURL = requestURL.substring(0, index);
			}
			setSessionData(SessionKeyConstants.REQUESTURL_BEFORELOGIN, requestURL);
			Map<String, String[]> paramMap = null;
			if (request instanceof HttpServletRequestDecorator) {
				paramMap = request.getParameterMap();
			} else {
				Map map = request.getParameterMap();
				if (map != null) {
					for (Object key : map.keySet()) {
						if (key instanceof String) {
							Object value = map.get(key);
							if (value instanceof String) {
								paramMap.put((String)key, new String[]{(String)value});
							}else if(value instanceof String[]){
								paramMap.put((String)key, (String[])value);
							}
						}
					}
				}
			}
			
			setSessionData(SessionKeyConstants.REQUESTPARAMS_BEFORELOGIN, (Serializable)paramMap);
		}
		
		/**
		 * 设置当前线程是否需要转换中文<br>
		 * 只有过滤器需要用到，其它模块不应该处理转换相关的事宜
		 * @param needConvert
		 */
		public static void setNeedConvertChinese(boolean needConvert) {
			localInfo.get().setNeedConvertChs(needConvert);
		}
		
		/**
		 * 保存对象到当前会话中，如果当前还没有建立会话对象，则建立一个
		 * @param key
		 * @param value
		 */
		public static void setSessionData(String key, Serializable value){
			HttpSession session = getOrCreateSession();
			session.setAttribute(key, value);
		}
		
		/**
		 * 设置当前会话的超时时间<br>
		 * 如果为负数，则表明永不超时
		 * @param interval 以秒为单位
		 */
		public static void setSessionMaxInactiveInterval(int interval){
			HttpSession session = getSession();
			if(session != null){
				session.setMaxInactiveInterval(interval);
			}
		}
		
		/**
		 * 通过弱引用的方式保存数据到会话中<br>
		 * 这种保存是不需要手动清空的，因为会话持有的是数据的弱引用，能被自动回收<br>
		 * @param key
		 * @param value
		 */
		@SuppressWarnings("unchecked")
		public static void setWeakSessionData(Object key, Serializable value){
			HttpSession session = getOrCreateSession();
			WeakDataMap<Object, Serializable> weakMap = null;
			synchronized (session) {//之所以这样，是因为HttpSession本身不是线程安全的
				weakMap = (WeakDataMap<Object, Serializable>)session.getAttribute(Constants.WEAKREFDATA);
				if (weakMap == null) {
					weakMap = new WeakDataMap<Object, Serializable>(session.getId());
					session.setAttribute(Constants.WEAKREFDATA, weakMap);
				}			
			}
			weakMap.put(key, value);
		}
		
		/**
		 * 取消注册到当前线程的会话对象
		 */
		public static void unregisterRequest(){
			curRequest.set(null);
			curSession.set(null);
		}
		
		/**
		 * 得到文件后缀对应的MIME类型
		 * @param file
		 * @return String
		 */
		public static String getMimeType(String file){
			return servletContext.getMimeType(file);
		}
		
		  
	
	/**
	 * 得到当前线程相关的Locale<br>
	 * 如果当前线程和请求绑定，则返回请求的Locale，否则，返回JVM的默认Locale
	 * @return Locale
	 */
	public static Locale getLocale(){
		LocalInfo linfo = localInfo.get();
		return linfo==null?Locale.getDefault():linfo.getCurLocale();
	}
	
	/**
	 * 根据key值和相应的参数从默认的资源包取对应的资源并替换参数，资源的Locale和当前线程的Locale一致。<br>
	 * 如果当前线程是处理Http请求的线程，那么Locale和用户设置的语言一致（包括通过浏览器设置的或者是用户<br>
	 * 手动在页面上选择的语言）；如果当前线程不是处理Http请求的线程（比如后台任务调度线程，那么Locale为<br>
	 * 服务器系统Locale，对于我们的应用来说，就是Locale.CHINESE，这个可以通过方法{@link #registerLocale2CurThread(Locale)}<br>
	 * 来设置
	 * @param key
	 * @param args
	 * @return String
	 */
	public static String getMessage(String key, Object[] args){
		args = handleMsgParams(args);
		return getMessageResources().getMessage(getLocale(), key, args);
	}
 }
