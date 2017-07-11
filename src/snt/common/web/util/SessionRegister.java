/**
 * 
 */
package snt.common.web.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 在会话激活的时候注册会话对象到会话数据清理任务
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 2006-9-17
 */
public class SessionRegister implements HttpSessionActivationListener,
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(SessionRegister.class);
	
	private static transient List<HttpSession> activatedSessionList = null;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionActivationListener#sessionDidActivate(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionDidActivate(HttpSessionEvent se) {
		try {
			log.info("会话激活，会话ID"+se.getSession().getId());
			init();
			activatedSessionList.add(se.getSession());
		} catch (Throwable th) {
			log.error("注册激活的会话有误", th);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionActivationListener#sessionWillPassivate(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionWillPassivate(HttpSessionEvent se) {		
	}
	
	static List<HttpSession> getActivatedSessionList(){
		return activatedSessionList;
	}
	
	private void init(){
		if (activatedSessionList == null) {
			activatedSessionList = new ArrayList<HttpSession>();
		}
	}
}
