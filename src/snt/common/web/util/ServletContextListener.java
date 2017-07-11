package snt.common.web.util;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.Log4jWebConfigurer;

/**
 * 监听Servlet Context的初始化和销毁，在其中初始化（/销毁）Spring业务上下文以及日志记录器和集群节点号<br/>
 * 监听会话对象的创建和销毁 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class ServletContextListener extends ContextLoaderListener implements HttpSessionListener {
	private static Log log = LogFactory.getLog(ServletContextListener.class);
	
	private SessionDataClearTask sessionDataClearTask = null;
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent sce) {
		Log4jWebConfigurer.initLogging(sce.getServletContext());
		super.contextInitialized(sce);
		try {
			WebUtils.servletContext = sce.getServletContext();
			
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
			if(wac.containsBean("sessionDataClearTask")){
				sessionDataClearTask = (SessionDataClearTask)wac.getBean("sessionDataClearTask", SessionDataClearTask.class);
				List<HttpSession> activatedSessionList = SessionRegister.getActivatedSessionList();
				if(activatedSessionList != null){
					for (HttpSession session : activatedSessionList) {
						sessionDataClearTask.addSession(session);
					}
				}
			}
		} catch (Exception e) {
			log.error("竟然出错了——这绝对是人品问题！", e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("停止会话数据清理任务");
			}
			if(sessionDataClearTask!=null){
				sessionDataClearTask.cancel();
			}
		} catch (Exception e) {
			log.error("竟然出错了——这绝对是人品问题！", e);
		}
		WebUtils.servletContext = null;
		super.contextDestroyed(sce);
		Log4jWebConfigurer.shutdownLogging(sce.getServletContext());
	}

	public void sessionCreated(HttpSessionEvent se) {
		HttpSession httpSession = se.getSession();
		if (log.isInfoEnabled()) {
			log.info("新建会话"+httpSession.getId());
		}
		if(sessionDataClearTask != null){
			sessionDataClearTask.addSession(httpSession);
		}
		httpSession.setAttribute("SessionRegister", new SessionRegister());
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession httpSession = se.getSession();
		if (log.isInfoEnabled()) {
			log.info("销毁会话"+httpSession.getId());
		}
		if(sessionDataClearTask != null){
			sessionDataClearTask.removeSession(httpSession);
		}
	}
}

