package snt.common.web.util;

/**
 * 存放入Session的信息Key常量定义接口
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public interface SessionKeyConstants {
	/**登录的账户名*/
	String LOGINACCOUNT = "LOGINACCOUNT";
	/**登录账户主键*/
	String LOGIN_ACCOUNTPK = "LOGIN_ACCOUNTPK";
	
	/**登录前的来源页面*/
	String FROMURL_BEFORELOGIN = "FROMURL_BEFORELOGIN";
	/**当前窗口是否是弹出窗口*/
	String IF_CURWIN_POPUP = "IF_CURWIN_POPUP";
	/**登录前挂起的请求URL*/
	String REQUESTURL_BEFORELOGIN = "REQUESTURI_BEFORELOGIN";
	/**登录前挂起的请求参数Map*/
	String REQUESTPARAMS_BEFORELOGIN = "REQUESTPARAMS_BEFORELOGIN";
}

