/**
 * 
 */
package snt.css.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import snt.common.dao.base.ICommonDAOService;
import snt.common.rs.MemoryResultSet;
import snt.common.rs.MemoryResultSetMetaData;
import snt.css.util.Constant;

/**
 * 缓存提供者
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 2006-9-20
 */
public class CacheProvider implements InitializingBean{
	private static Log log = LogFactory.getLog(CacheProvider.class);
	private ICommonDAOService commonDAOService;
	
	/**
	 * 加载报表结构
	 * @param reportPageMap
	 * @return 报告类型到报表结构的映射表
	 */
	@SuppressWarnings("unchecked")
	Map<String, MemoryResultSet> getReportStructure(Map<Long, MemoryResultSet> reportPageMap){
		try {
			String sql = "select * from t_report_struct order by stat_year,reporttype, node_sn";
			MemoryResultSet mrs = getCommonDAOService().queryForResultSet(sql);
			//这个元数据描述是可以共用的
			MemoryResultSetMetaData mrsMetaData = mrs.getMetaData0();
			
			Map<String, MemoryResultSet> reportStructureMap = new HashMap<String, MemoryResultSet>();
			mrs.beforeFirst();
			int reportType = -1;
			List report = null;
			int year = -1;
			while (mrs.next()) {
				long id = mrs.getLong("id");
				int newReportType = mrs.getInt("reporttype");
				int newYear = mrs.getInt("stat_year");
				if(year != newYear || reportType != newReportType){
					if (report != null) {
						reportStructureMap.put(year + "," + reportType, new MemoryResultSet(report, mrsMetaData));
					}
					report = new ArrayList();
					reportType = newReportType;
					year = newYear;
				}
				List row = new ArrayList(1);
				row.add(mrs.getRowList());
				report.add(mrs.getRowList());
				reportPageMap.put(id, new MemoryResultSet(row, mrsMetaData));
				
			}
			if (report != null) {
				reportStructureMap.put(year + "," + reportType, new MemoryResultSet(report, mrsMetaData));
			}
			return reportStructureMap;
		} catch (Exception e) {
			log.error("缓存报表结构出错", e);
			return null;
		}
	}
	
	/**hqx add
	 * 加载数据报告结构
	 * @param reportPageMap
	 * @return 报告类型到报表结构的映射表
	 */
	@SuppressWarnings("unchecked")
	Map<Integer, MemoryResultSet> getReportStructure2(Map<Long, MemoryResultSet> reportPageMap){
		try {
			String sql = "select * from t_report_struct order by reporttype,sort";
			MemoryResultSet mrs = getCommonDAOService().queryForResultSet(sql);
			//这个元数据描述是可以共用的
			MemoryResultSetMetaData mrsMetaData = mrs.getMetaData0();
			
			Map<Integer, MemoryResultSet> reportStructureMap = new HashMap<Integer, MemoryResultSet>();
			mrs.beforeFirst();
			int reportType = -1;
			List report = null;
			while (mrs.next()) {
				long id = mrs.getLong("pk_id");
				int newReportType = mrs.getInt("reporttype");
				if(reportType != newReportType){
					if (report != null) {
						reportStructureMap.put(reportType, new MemoryResultSet(report, mrsMetaData));
					}
					report = new ArrayList();
					reportType = newReportType;
				}
				List row = new ArrayList(1);
				row.add(mrs.getRowList());
				report.add(mrs.getRowList());
				reportPageMap.put(id, new MemoryResultSet(row, mrsMetaData));
				
			}
			if (report != null) {
				reportStructureMap.put(reportType, new MemoryResultSet(report, mrsMetaData));
			}
			return reportStructureMap;
		} catch (Exception e) {
			log.error("缓存报表结构出错", e);
			return null;
		}
	}
	
	Map<String, Map> getSaleQuestionsMap(){
		try {
			String sql = "select * from t_sale_question";
			List<Map<String, Object>> questionsList = getCommonDAOService().queryForMapList(sql);
			
			Map<String, Map> questionsMap = new HashMap<String, Map>(questionsList.size());
			for (Map map : questionsList) {
				String questionID = (String)map.get("qid");
				questionsMap.put(questionID.toUpperCase(), map);
			}
			return questionsMap;
		} catch (Exception e) {
			log.error("缓存销售题库出错", e);
			return null;
		}
	}
	
	Map<String, Map> getAfterSaleQuestionsMap(){
		try {
			String sql = "select * from t_after_sale_question";
			List<Map<String, Object>> questionsList = getCommonDAOService().queryForMapList(sql);
			
			Map<String, Map> questionsMap = new HashMap<String, Map>(questionsList.size());
			for (Map map : questionsList) {
				String questionID = (String)map.get("qid");
				questionsMap.put(questionID.toUpperCase(), map);
			}
			return questionsMap;
		} catch (Exception e) {
			log.error("缓存售后题库出错", e);
			return null;
		}
	}
	/**
	 * hqx add
	 * 查询所有大区
	 * @return
	 */
	MemoryResultSet getLargeAreaMrs(int year){
		try {
			if (year == 0){
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
			}
			String sql="select * From t_large_areas where stat_year = " + year + " order by pk_id";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		} catch (Exception e) {
			log.error("查询所有大区出错", e);
			return null;
		}
	}
	
	/**
	 * hqx add
	 * 查询所有小区
	 * @return
	 */
	MemoryResultSet getSmallAreaMrs(int year){
		try{
			if (year == 0){
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
			}
			String sql="select * From t_small_areas where stat_year = "+ year + " order by l_area_pk,pk_id";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		}catch(Exception e){
			log.error("查询所有小区出错", e);
			return null;
		}
	}
	
	/**
	 * hqx add
	 * 查询所有小区域经理
	 * @return
	 */
	MemoryResultSet getSmallAreaManagerMrs(int year,String type){
		try{
			if (year == 0){
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
			}
			String sql="select * From t_SAreaManager where stat_year = "+ year + " and dealer_type="+type+" order by l_area_pk,pk_id";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		}catch(Exception e){
			log.error("查询所有小区域经理出错", e);
			return null;
		}
	}
	
	/**
	 * hqx add
	 * 查询所有经销商
	 * @return
	 */
	MemoryResultSet getDealerMrs(int year){
		try{
			if (year == 0){
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
			}
			String sql="select pk_id, dealer_name,l_area_pk,sinotrust_id From t_dealers where stat_year = " + year + " order by l_area_pk,pk_id";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		}catch(Exception e){
			log.error("查询所有经销商出错", e);
			return null;
		}
	}
	
	/**
	 * hqx add
	 * 取得反馈原始问题
	 * @return
	 */
	Map<String,String> getFeedbackQuestion(){
		try{
			StringBuffer sbuf=new StringBuffer();
			sbuf.append("select content,'").append(Constant.FEEDBACK_AFTER_SALE);
			sbuf.append("' as flag from t_after_sale_question where is_need=1 and is_feedback=1 union ");
			sbuf.append("select content,'").append(Constant.FEEDBACK_SALE);
			sbuf.append("' as flag from t_sale_question where is_need=1 and is_feedback=1");
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sbuf.toString());
			Map<String,String> questions=new HashMap<String,String>();
			while(mrs.next()){
				questions.put(mrs.getString("flag"),mrs.getString("content"));
			}
			return questions;
		}catch(Exception e){
			log.error("查询反馈原始问题出错", e);
			return null;
		}
	}
	
	/**
	 * hqx add 
	 * 取得电访成功(phone)、配额满未使用样本标识(unused)
	 * @return
	 */
	Map<String,Integer> getSwatchFlag(){
		try{
			String sql="select phone_code,unused_code from t_sale_other_filter";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			Map<String,Integer> map=new HashMap<String,Integer>();
			while(mrs.next()){
				Integer phone=mrs.getString("phone_code")!=null?new Integer(mrs.getInt("phone_code")):null;
				Integer unused=mrs.getString("unused_code")!=null?new Integer(mrs.getInt("unused_code")):null;
				map.put("phone",phone);
				map.put("unused",unused);
			}
			return map;
		}catch(Exception e){
			log.error("查询电访成功、配额满未使用样本标识出错", e);
			return null;
		}
	}
	
	/**
	 * hqx add
	 * 查询大样本及小样本定义值
	 * @return
	 */
	MemoryResultSet getSwatchDefine(){
		try{
			String sql="select big,small,stat_year,stat_month From t_big_swatch order by stat_year desc,stat_month desc";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		}catch(Exception e){
			log.error("查询大样本及小样本定义值出错", e);
			return null;
		}
	}
	
	public ICommonDAOService getCommonDAOService() {
		return commonDAOService;
	}
	
	public void setCommonDAOService(ICommonDAOService commonDAOService) {
		this.commonDAOService = commonDAOService;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Cache.setCacheProvider(this);
	}
	/**
	 * 获得环节
	 * @param year
	 * @param month
	 * @return
	 */
	public MemoryResultSet getTacheProvider(int year, int month) {

		try {
			if (year == 0){
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
			}
			String sql="select * From t_tache_target where stat_year = " + year + " and stat_month= " + month +"  and tache_id is null order by pk_id";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		} catch (Exception e) {
			log.error("查询所有大区出错", e);
			return null;
		}
	}
	/**
	 * 获得指标
	 * @param year
	 * @param month
	 * @return
	 */
	public MemoryResultSet getTargetProvider(int year, int month) {

		try {
			if (year == 0){
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
			}
			String sql="select * From t_tache_target where stat_year = " + year + " and stat_month= " + month +"  and tache_id is not null order by pk_id";
			MemoryResultSet mrs=getCommonDAOService().queryForResultSet(sql);
			return mrs;
		} catch (Exception e) {
			log.error("查询所有大区出错", e);
			return null;
		}
	}
}
