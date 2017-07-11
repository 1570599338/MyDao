/**
 * 
 */
package snt.common.web.filter;

import java.sql.Types;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import snt.common.dao.base.CommonDAO;
import snt.common.web.util.WebUtils;

/**
 * 记录每次请求的日志记录器<br/>
 * 表名可以配置，但是表结构是固定的，必须包含如下字段：<br/>
 * 主键字段		自增型<br/>
 * request_url	varchar		255<br/>
 * query_string	varchar		255<br/>
 * remote_addr	varchar		15<br/>
 * account		varchar		100<br/>
 * sessionid	varchar		100<br/>
 * visittime	datetime    默认值为当前时间<br/>
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class Logger {
	private static Log log = LogFactory.getLog(Logger.class);
	private CommonDAO commonDAO;
	
	private String logTableName;
	/**
	 * 登录用户在Session中存储的key值
	 */
	private String userKey;
	/**
	 * 
	 */
	public Logger() {
		super();
	}
	
	/**
	 * 记录请求的各种信息，如远程地址，当前账户，访问资源，请求参数和访问时间等信息
	 * @param httpServletRequest
	 */
	public void logRequest(HttpServletRequest httpServletRequest){
		try {
			String requestURL = httpServletRequest.getRequestURI();
			if (requestURL == null || !(
					requestURL.endsWith(".do") || 
					requestURL.endsWith(".jsp")||
					requestURL.endsWith(".html")||
					requestURL.endsWith(".htm")||
					requestURL.endsWith("Service"))) {
				return;
			}
			String queryStr = getQueryString(httpServletRequest);
			String remoteAddr = httpServletRequest.getRemoteAddr();
			String loginAccount = (String)WebUtils.getSessionData(getUserKey());
			String insertSql = "insert into "+getLogTableName()+" (request_url,query_string,remote_addr,account,sessionid) values(?, ?, ?, ?, ?)";
			
			getCommonDAO().update(insertSql, new Object[]{requestURL, queryStr, remoteAddr, loginAccount, WebUtils.getSessionID()},
					new int[]{Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR});
		} catch (DataAccessException e) {
			log.debug(e);
		}
	}
	
	private String getQueryString(HttpServletRequest httpServletRequest){
		if (!"POST".equalsIgnoreCase(httpServletRequest.getMethod())) {
			return httpServletRequest.getQueryString();
		}else{
			StringBuffer strBuf = new StringBuffer("POST:");
			Enumeration paramEnum = httpServletRequest.getParameterNames();
			boolean first = true;
			while (paramEnum.hasMoreElements()) {
				String paramName = (String) paramEnum.nextElement();
				String paramValue = httpServletRequest.getParameter(paramName);
				if (first) {
					first = false;
				} else {
					strBuf.append("&");					
				}
				strBuf.append(paramName).append("=").append(paramValue);
			}
			return strBuf.toString();
		}
	}
	
	public CommonDAO getCommonDAO() {
		return commonDAO;
	}

	public void setCommonDAO(CommonDAO commonDAO) {
		this.commonDAO = commonDAO;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getLogTableName() {
		return logTableName;
	}

	public void setLogTableName(String logTableName) {
		this.logTableName = logTableName;
	}
}
