/**
 * 
 */
package snt.css.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import snt.common.rs.MemoryResultSet;

/**
 * 缓存
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 2006-9-20
 */
public class Cache {
	/**日志*/
	private static Log log = LogFactory.getLog(Cache.class);
	/**缓存提供器*/
	private static CacheProvider cacheProvider;
	/**获取报告结构的同步对象*/
	private static Object reportStructureSyn = new Object();
	/**报告结构*/
	private static Map<String, MemoryResultSet> reportStructureMap = null;
	/**报告具体页的描述*/
	private static Map<Long, MemoryResultSet> reportPageMap = null;
	/**获取数据报告结构的同步对象*/
	private static Object reportStructureSyn2 = new Object();
	/**数据报告结构*/
	private static Map<String, MemoryResultSet> reportStructureMap2 = null;
	/**数据报告具体页的描述*/
	private static Map<Long, MemoryResultSet> reportPageMap2 = null;
	/**销售题库*/
	private static Map<String, Map> saleQuestionsMap = null;
	/**售后题库*/
	private static Map<String, Map> afterSaleQuestionsMap = null;
	/**大区*/
	private static MemoryResultSet largeAreaMrs=null;
	/**小区*/
	private static MemoryResultSet smallAreaMrs=null;
	/**小区域经理*/
	private static MemoryResultSet smallAreaManagerMrs=null;
	/**经销商*/
	private static MemoryResultSet dealerMrs=null;
	/**环节*/
	private static MemoryResultSet tacheMrs=null;
	/**指标*/
	private static MemoryResultSet targetMrs=null;
	
	
	//**反馈原始问题*//*
	private static Map<String,String> questionsMap=null;
	//**电访成功样本标识及配额满未使用样本标识*//*
	private static Map<String,Integer> swatchFlagMap=null;
	//**大样本及小样本限定范围*//*
	private static MemoryResultSet swatchDefineMrs=null;
	
	
	/**
	 * 取得报告结构
	 * @param reportType
	 * @return 表示报告结构的内存结果集
	 */
	public static MemoryResultSet getReportStructure(int year, int reportType){
		if(log.isDebugEnabled()){
			log.debug("取得报表结构");
		}
		if(reportStructureMap == null){
			synchronized (reportStructureSyn) {
				if (reportStructureMap == null) {
					reportPageMap = new HashMap<Long, MemoryResultSet>();
					reportStructureMap = cacheProvider.getReportStructure(reportPageMap);
				}
			}
		}
		MemoryResultSet mrs = reportStructureMap.get(year + "," + reportType);
		return mrs==null?null:new MemoryResultSet(mrs.getResultList(), mrs.getMetaData0());
	}
	
	/**
	 * 取得报表中某页的定义
	 * @param pageID
	 * @return 报表中某页的定义
	 */
	public static MemoryResultSet getReportPageDefinition(long pageID){
		if(log.isDebugEnabled()){
			log.debug("取得报表中某页的定义");
		}
		if(reportStructureMap == null){
			synchronized (reportStructureSyn) {
				if (reportStructureMap == null) {
					reportPageMap = new HashMap<Long, MemoryResultSet>();
					reportStructureMap = cacheProvider.getReportStructure(reportPageMap);
				}
			}
		}
		MemoryResultSet mrs = reportPageMap.get(pageID);
		return new MemoryResultSet(mrs.getResultList(), mrs.getMetaData0());
	}
	
	/**
	 * 取得数据报告结构
	 * @param reportType
	 * @return 表示报告结构的内存结果集
	 */
	public static MemoryResultSet getReportStructure2(int year, int reportType){
		if(log.isDebugEnabled()){
			log.debug("取得报表结构");
		}
		if(reportStructureMap2 == null){
			synchronized (reportStructureSyn2) {
				if (reportStructureMap2 == null) {
					reportPageMap2 = new HashMap<Long, MemoryResultSet>();
					reportStructureMap2 = cacheProvider.getReportStructure(reportPageMap2);
				}
			}
		}
		MemoryResultSet mrs = reportStructureMap2.get(year + "," + reportType);
		return mrs==null?null:new MemoryResultSet(mrs.getResultList(), mrs.getMetaData0());
	}
	
	/**
	 * 取得数据报表中某页的定义
	 * @param pageID
	 * @return 报表中某页的定义
	 */
	public static MemoryResultSet getReportPageDefinition2(long pageID){
		if(log.isDebugEnabled()){
			log.debug("取得报表中某页的定义");
		}
		if(reportStructureMap2 == null){
			synchronized (reportStructureSyn2) {
				if (reportStructureMap2 == null) {
					reportPageMap2 = new HashMap<Long, MemoryResultSet>();
					reportStructureMap2 = cacheProvider.getReportStructure(reportPageMap2);
				}
			}
		}
		MemoryResultSet mrs = reportPageMap2.get(pageID);
		return new MemoryResultSet(mrs.getResultList(), mrs.getMetaData0());
	}
	
	/**
	 * 清除缓存中的报告结构
	 */
	public static void clearReportStructureCache(){
		synchronized (reportStructureSyn) {
			reportStructureMap = null;
			reportPageMap = null;
		}
	}
	
	/**
	 * 清除缓存中的销售题目信息
	 */
	public static void clearSaleQuestionInfoCache(){
		synchronized ("SaleQuestion".intern()) {
			saleQuestionsMap = null;
		}
	}
	
	/**
	 * 清除缓存中的售后题目信息
	 */
	public static void clearAfterSaleQuestionInfoCache(){
		synchronized ("AfterSaleQuestion".intern()) {
			afterSaleQuestionsMap = null;
		}
	}
	
	/**
	 * 清除缓存中的区域和经销商信息
	 */
	public static void clearAreaAndDealerInfoCache(){
		synchronized ("myQueryRight".intern()) {
			/**大区*/
			largeAreaMrs=null;
			/**小区*/
			smallAreaMrs=null;
			/**小区域经理*/
			smallAreaManagerMrs = null;
			/**经销商*/
			dealerMrs=null;
			/**环节*/
			tacheMrs=null;
			/**指标*/
			targetMrs=null;
		}
	}
	
	/**
	 * 取得销售题库中对应题目的信息
	 * @param questionID 题目对应的编号
	 * @return 题目信息
	 */
	public static Map getSaleQuestionInfo(String questionID){
		synchronized ("SaleQuestion".intern()) {
			if(saleQuestionsMap == null){
				saleQuestionsMap = cacheProvider.getSaleQuestionsMap();
			}
		}
		return saleQuestionsMap.get(questionID.toUpperCase());
	}
	
	/**
	 * 取得售后题库中对应题目的信息
	 * @param questionID 题目对应的编号
	 * @return 题目信息
	 */
	public static Map getAfterSaleQuestionInfo(String questionID){
		synchronized ("AfterSaleQuestion".intern()) {
			if(afterSaleQuestionsMap == null){
				afterSaleQuestionsMap = cacheProvider.getAfterSaleQuestionsMap();
			}
		}
		return afterSaleQuestionsMap.get(questionID.toUpperCase());
	}
	
	/** hqx add
	 * @description 取得反馈的原始问题
	 * @param flag 销售与售后的标识，与Constant.FEEDBACK_SALE、Constant.FEEDBACK_AFTER_SALE的值一致
	 * @return
	 */
	public static String getFeedbackQuestion(String flag){
		synchronized ("feedback".intern()) {
			if(questionsMap==null){
				questionsMap=cacheProvider.getFeedbackQuestion();
			}
		}
		return questionsMap.get(flag);
	}
	
	/**
	 * hqx add 
	 * 电访成功样本标识
	 * @return
	 */
	public static Integer getPhoneSuccessFlag(){
		synchronized ("phoneSuccess".intern()) {
			if(swatchFlagMap==null){
				swatchFlagMap=cacheProvider.getSwatchFlag();
			}
		}
		return swatchFlagMap.get("phone");
	}
	
	/**
	 * hqx add
	 * 配额满未使用样本标识
	 * @return
	 */
	public static Integer getUnused(){
		synchronized ("phoneSuccess".intern()) {
			if(swatchFlagMap==null){
				swatchFlagMap=cacheProvider.getSwatchFlag();
			}
		}
		return swatchFlagMap.get("unused");
	}
	
	/**
	 * hqx add
	 * 大样本定义值
	 * @param year
	 * @param month
	 * @return
	 */
	public static Integer getBigSwatchDefine(Integer year,Integer month){
		synchronized ("swatchDefine".intern()) {
			if(swatchDefineMrs==null){
				swatchDefineMrs=cacheProvider.getSwatchDefine();
			}
		}
		if(year==null || month==null)return null;
		Integer bigDefine=null;
		try{
			Integer[] months=null;
			if(month>20){
				months=new Integer[3];
				for(int i=0;i<3;i++){
					months[i]=3*(month-20)-i;
				}
			}else{
				months=new Integer[1];
				months[0]=month;
			}
			swatchDefineMrs.beforeFirst();
			while(swatchDefineMrs.next()){
				int stat_year=swatchDefineMrs.getInt("stat_year");
				int stat_month=swatchDefineMrs.getInt("stat_month");
				if(year.intValue()<=stat_year){
					for(int i=0;i<months.length;i++){
						if(months[i].intValue()<=stat_month){
							bigDefine=new Integer(swatchDefineMrs.getInt("big"));
							break;
						}
					}
				}
			}
		}catch(Exception e){
			log.error("获取大样本量定义值出错",e);
		}
		return bigDefine;
	}
	
	/**
	 * hqx add 
	 * 小样本量定义值
	 * @param year
	 * @param month
	 * @return
	 */
	public static Integer getSmallSwatchDefine(Integer year,Integer month){
		synchronized ("swatchDefine".intern()) {
			if(swatchDefineMrs==null){
				swatchDefineMrs=cacheProvider.getSwatchDefine();
			}
		}
		if(year==null || month==null)return null;
		Integer smallDefine=null;
		try{
			Integer[] months=null;
			if(month>20){
				months=new Integer[3];
				for(int i=0;i<3;i++){
					months[i]=3*(month-20)-i;
				}
			}else{
				months=new Integer[1];
				months[0]=month;
			}
			swatchDefineMrs.beforeFirst();
			while(swatchDefineMrs.next()){
				int stat_year=swatchDefineMrs.getInt("stat_year");
				int stat_month=swatchDefineMrs.getInt("stat_month");
				if(year.intValue()<=stat_year){
					for(int i=0;i<months.length;i++){
						if(months[i].intValue()<=stat_month){
							smallDefine=new Integer(swatchDefineMrs.getInt("small"));
							break;
						}
					}
				}
			}
		}catch(Exception e){
			log.error("获取大样本量定义值出错",e);
		}
		return smallDefine;
	}
	
	/**
	 * hqx add
	 * 用户登录权限下的区域查询条件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,MemoryResultSet> getMyQueryRight(String dealer_id,Integer area_id,String role, int year,int month){
		synchronized("myQueryRight".intern()){
			log.error("=======================================");
			log.error("dealer_id:"+dealer_id);
			log.error("area_id:"+area_id);
			log.error("role:"+role);
			log.error("=======================================");
//		 MemoryResultSet largeAreaMrs = null;
//		 MemoryResultSet smallAreaMrs = null;
//		 MemoryResultSet smallAreaManagerMrs = null;
//		 MemoryResultSet dealerMrs = null;
		 
			if(largeAreaMrs==null){
				largeAreaMrs=cacheProvider.getLargeAreaMrs(year);
			}
			if(smallAreaMrs==null){
				smallAreaMrs=cacheProvider.getSmallAreaMrs(year);
			}
//			if(smallAreaManagerMrs == null){//小区域经理
//				smallAreaManagerMrs = cacheProvider.getSmallAreaManagerMrs(year,type);
//			}
			if(dealerMrs==null){
				dealerMrs=cacheProvider.getDealerMrs(year);
			}
			//得到环节
			if(tacheMrs==null){
				tacheMrs=cacheProvider.getTacheProvider(year,month);
			}
			//得到指标
			if(targetMrs==null){
				targetMrs=cacheProvider.getTargetProvider(year,month);
			}
//		}
		Map<String,MemoryResultSet> map=new HashMap<String,MemoryResultSet>();
		if(tacheMrs.getResultList().size()>0){
			map.put("tache",tacheMrs);
		}
		if(targetMrs.getResultList().size()>0){
			map.put("target",targetMrs);
		}
		
		try{
			if(role!=null && role.equals("F")){//厂商
				if(largeAreaMrs.getResultList().size()>0)
				map.put("area",largeAreaMrs);
/*				if(smallAreaMrs.getResultList().size()>0)
				map.put("district",smallAreaMrs);*/
				if(dealerMrs.getResultList().size()>0)
				map.put("dealer",dealerMrs);
			}else if(role!=null && role.equals("L")){//大区经理
				if(largeAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					largeAreaMrs.beforeFirst();
					while(largeAreaMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(largeAreaMrs.getInt("pk_id")))){
							list.add(largeAreaMrs.getRowList());
							map.put("area",new MemoryResultSet(list,largeAreaMrs.getMetaData0()));
						}
					}
				}
				/*if(smallAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					smallAreaMrs.beforeFirst();
					while(smallAreaMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(smallAreaMrs.getInt("l_area_pk")))){
							list.add(smallAreaMrs.getRowList());
						}
					}
					map.put("district",new MemoryResultSet(list,smallAreaMrs.getMetaData0()));
				}*/
				
				if(dealerMrs.getResultList().size()>0){
					List list=new ArrayList();
					dealerMrs.beforeFirst();
					while(dealerMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(dealerMrs.getInt("l_area_pk")))){
							list.add(dealerMrs.getRowList());
						}
					}
					map.put("dealer",new MemoryResultSet(list,dealerMrs.getMetaData0()));
				}
			}/*else if(role!=null && role.equals("A")){//小区经理
				Integer largeArea_id=null;
				if(smallAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					smallAreaMrs.beforeFirst();
					while(smallAreaMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(smallAreaMrs.getInt("pk_id")))){
							largeArea_id=new Integer(smallAreaMrs.getInt("l_area_pk"));
							list.add(smallAreaMrs.getRowList());
							map.put("district",new MemoryResultSet(list,smallAreaMrs.getMetaData0()));
						}
					}
				}
				
				if(largeAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					largeAreaMrs.beforeFirst();
					while(largeAreaMrs.next()){
						if(largeArea_id!=null && largeArea_id.equals(new Integer(largeAreaMrs.getInt("pk_id")))){
							list.add(largeAreaMrs.getRowList());
							map.put("area",new MemoryResultSet(list,largeAreaMrs.getMetaData0()));
						}
					}
				}
				if(dealerMrs.getResultList().size()>0){
					List list=new ArrayList();
					dealerMrs.beforeFirst();
					while(dealerMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(dealerMrs.getInt("s_area_pk")))){
							list.add(dealerMrs.getRowList());
						}
					}
					map.put("dealer",new MemoryResultSet(list,dealerMrs.getMetaData0()));
				}
			}*/else if(role!=null && role.equals("M")){//区域经理
				Integer largeArea_id=null;
				/*if(smallAreaManagerMrs.getResultList().size() > 0){
					List list=new ArrayList();
					smallAreaManagerMrs.beforeFirst();
					while(smallAreaManagerMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(smallAreaManagerMrs.getInt("pk_id")))){
							largeArea_id=new Integer(smallAreaManagerMrs.getInt("l_area_pk"));
							list.add(smallAreaManagerMrs.getRowList());
							map.put("district",new MemoryResultSet(list,smallAreaManagerMrs.getMetaData0()));
						}
					}
				}*/
				if(largeAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					largeAreaMrs.beforeFirst();
					while(largeAreaMrs.next()){
						if(largeArea_id!=null && largeArea_id.equals(new Integer(largeAreaMrs.getInt("pk_id")))){
							list.add(largeAreaMrs.getRowList());
							map.put("area",new MemoryResultSet(list,largeAreaMrs.getMetaData0()));
						}
					}
				}
				if(dealerMrs.getResultList().size()>0){
					List list=new ArrayList();
					dealerMrs.beforeFirst();
					while(dealerMrs.next()){
						if(area_id!=null && area_id.equals(new Integer(dealerMrs.getInt("sarea_m_pk")))){
							list.add(dealerMrs.getRowList());
						}
					}
					map.put("dealer",new MemoryResultSet(list,dealerMrs.getMetaData0()));
				}
			}else if(role!=null && role.equals("S")){//经销商
				Integer largeArea_id=null;
				Integer smallArea_id=null;
				if(dealerMrs.getResultList().size()>0){
					List list=new ArrayList();
					dealerMrs.beforeFirst();
					while(dealerMrs.next()){
						if(dealer_id!=null && dealer_id.equals(dealerMrs.getString("pk_id"))){
							largeArea_id=new Integer(dealerMrs.getInt("l_area_pk"));
							//smallArea_id=new Integer(dealerMrs.getInt("s_area_pk"));
							list.add(dealerMrs.getRowList());
							map.put("dealer",new MemoryResultSet(list,dealerMrs.getMetaData0()));
						}
					}
				}
				if(largeAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					largeAreaMrs.beforeFirst();
					while(largeAreaMrs.next()){
						if(largeArea_id!=null && largeArea_id.equals(new Integer(largeAreaMrs.getInt("pk_id")))){
							list.add(largeAreaMrs.getRowList());
							map.put("area",new MemoryResultSet(list,largeAreaMrs.getMetaData0()));
						}
					}
				}
				/*
				if(smallAreaMrs.getResultList().size()>0){
					List list=new ArrayList();
					smallAreaMrs.beforeFirst();
					while(smallAreaMrs.next()){
						if(smallArea_id!=null && smallArea_id.equals(new Integer(smallAreaMrs.getInt("pk_id")))){
							list.add(smallAreaMrs.getRowList());
							map.put("district",new MemoryResultSet(list,smallAreaMrs.getMetaData0()));
						}
					}
				}*/
			}else{
				map.put("area",null);
				map.put("district",null);
				map.put("dealer",null);
			}
		}catch(Exception e){
			log.error("用户登录权限下的区域查询条件",e);
		}
		return map;
		}
	}
	
	static void setCacheProvider(CacheProvider cacheProvider) {
		Cache.cacheProvider = cacheProvider;
	}
}
