/**
 * 
 */
package snt.common.web.util;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 会话数据清理任务
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * Create Time:2006-7-5 
 */
public class SessionDataClearTask extends TimerTask {
	private static Log log = LogFactory.getLog(SessionDataClearTask.class);
	private ConcurrentMap<String, HttpSession> sessionMap;	
	
	public SessionDataClearTask() {
		sessionMap = new ConcurrentHashMap<String, HttpSession>();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (log.isDebugEnabled()) {
			log.debug("开始本次清理会话数据");
		}
		for (String sessionID : sessionMap.keySet()) {
			HttpSession httpSession = sessionMap.get(sessionID);
			try {
				WeakDataMap weakSessionDataMap = (WeakDataMap)httpSession.getAttribute(Constants.WEAKREFDATA);
				if (weakSessionDataMap != null) {
					weakSessionDataMap.collectGarbage();
				}					
			} catch (IllegalStateException e) {
				sessionMap.remove(sessionID);
			} catch (Throwable th){
				log.error("清理会话弱引用数据出错！", th);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("结束本次清理会话数据");
		}
	}

	void addSession(HttpSession session){
		sessionMap.put(session.getId(), session);
		if (log.isDebugEnabled()) {
			log.debug("注册会话"+session.getId());
		}
	}
	
	void removeSession(HttpSession session){
		sessionMap.remove(session.getId());
		if (log.isDebugEnabled()) {
			log.debug("取消注册会话"+session.getId());
		}
	}
}
