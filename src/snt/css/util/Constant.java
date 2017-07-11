/**
 * 
 */
package snt.css.util;

import snt.css.util.Constant;

/**
 * 一些常量定义
 * （这里并没有采用Interface来保存常量，是为了保持java动态语言的特性，
 * 所以在这里定义的常量必须用static final来修饰）
 * @author lizheng
 *
 */
public class Constant {

	/*帐户身份类型定义 -- 数据库中相关字段：t_accounts.identity*/
	public static final String IDENTITY_MANUFACTURER = "F"; // 厂商
	public static final String IDENTITY_LARGEAREAMANAGER = "L"; //大区经理
	public static final String IDENTITY_SMALLAREAMANAGER = "A"; //小区经理
	public static final String IDENTITY_SMALLAREAMANAGER_D = "M"; //小区域经理
	public static final String IDENTITY_DEALER = "S"; //经销商
	public static final String IDENTITY_RESERCHER = "R"; //研究人员
	public static final String IDENTITY_FILE_MANAGER = "C";  // 文件管理员
	
	/*各统计表中统计时间定义：*/
	/** 一月份*/
	public static final int MONTH_JANUARY=1;
	/** 二月份*/
	public static final int MONTH_FEBRUARY=2;
	/** 三月份*/
	public static final int MONTH_MARCH=3;
	/** 四月份*/
	public static final int MONTH_APRIL=4;
	/** 五月份*/
	public static final int MONTH_MAY=5;
	/** 六月份*/
	public static final int MONTH_JUNE=6;
	/** 七月份*/
	public static final int MONTH_JULY=7;
	/** 八月份*/
	public static final int MONTH_AUGUST=8;
	/** 九月份*/
	public static final int MONTH_SEPTEMBER=9;
	/** 十月份*/
	public static final int MONTH_OCTOBER=10;
	/** 十一月份*/
	public static final int MONTH_NOVERMBER=11;
	/** 十二月份*/
	public static final int MONTH_DECERMBER=12;
	/** 第一季度*/
	public static final int QUARTER_SPRINGTIME=21;
	/** 第二季度*/
	public static final int QUARTER_SUMMERTIME=22;
	/** 第三季度*/
	public static final int QUARTER_AUTUMN=23;
	/** 第四季度*/
	public static final int QUARTER_WINTER=24;
	/** 上半年*/
	public static final int YEAR_UPPER=31;
	/** 下半年*/
	public static final int YEAR_LOWER=32;
	/** 全年*/
	public static final int YEAR_WHOLE=0;
	
	/*图表类型定义 -- 数据库中相关字段： charttype       */ 
	/** 体系结构图*/
	public static final int SYSTEM_FRAME_CHART=1;
	/** 管理者摘要图*/
	public static final int SUMMARY_CHART=2;
	/** 折线图*/
	public static final int LINE_CHART = 3;
	/** 横向柱型图*/
    public static final int BAR_CHART = 4;
    /** 竖立柱型图*/
    public static final int VERTICAL_BAR_CHART=5;
    /** 竖立柱线型图*/
    public static final int VERTICAL_BAR_LINE_CHART=6;
    /** 四拼堆积柱子图*/
    public static final int STACKED_BAR_CHART=7;
    /** 四区图*/
    public static final int SCATTER_CHART=8;
    /**满意度联席会宗旨图*/
    public static final int TENET_CHART=9;
    /**满意度联席会想法---理论层面图*/
    public static final int THEORETICS_CHART=10;
    /**满意度联席会解决的问题图*/
    public static final int RESOLVENT_CHART=11;
    /**满意度联席会评价体系图*/
    public static final int SYSTEM_CHART=12;
    /**满意度联席会计算方法图*/
    public static final int CALCULATE_CHART=13;
    /**满意度联席会样本构成*/
    public static final int SWATCH_CHART=14;
    /**满意度联席会行业情况*/
    public static final int TRADE_CHART=15;
    /**多竖立柱型图*/
    public static final int MULTI_VERTICAL_BAR_CHART=16;
    /**条形堆积图与柱型图*/
    public static final int PIECE_AND_BAR_CHART = 17;
    /**深访问卷*/
    public static final int OPEN_EXCEL=18;
    /**满意度联席会城市地图*/
    public static final int CITY_CHART=19;
    
    /*图表数据类型定义 -- 数据库中相关字段：datasettype        */ 
    /** 样本量统计*/
    public static final int SWATCH_DATASET=1;
    /** 总评分与各环节得分*/
    public static final int TOTAL_TACHE_DATASET=2;
    /** 环节得分-与往期对比*/
    public static final int TACHE_PREVIOUS_DATASET=3;
    /** 环节得分*/
    public static final int TACHE_DATASET=4;
    /** 指标得分*/
    public static final int INDEX_POINT_DATASET=5;
    /** 优先改进分析-指标*/
    public static final int PREFERENTIAL_AMELIORATION_DATASET=6;
    /** 满意度对忠诚度的影响*/
    public static final int SATISFACTION_FEALTY_DATASET=7;
    /** 推荐率*/
    public static final int RECOMMENDATION_RATE_DATASET=8;
    /** 用户保持率*/
    public static final int KEEP_RATE_DATASET=9;
    /** KSP分析*/
    public static final int KSP_DATASET=10;
    /** 用户特征分析*/
    public static final int USER_CHARACTER_DATASET=11;
    /** 满意度联席会行业情况*/
    public static final int TRADE_INFO_DATASET=12;
    /** 满意度联席会厂商表现*/
    public static final int FACTORY_DATASET=13;
    /** 满意度联席会总评分厂商表现*/
    public static final int FACTORY_TOTAL_SCORE_DATASET=14;
    /** 满意度联席会22重点城市总评分厂商表现*/
    public static final int MAIN_CITY_FACTORY_TOTAL_SCORE_DATASET=15;
    /** 满意度联席会环节得分厂商表现*/
    public static final int FACTORY_TACHE_SCORE_DATASET=16;
    /** 满意度联席会指标得分厂商表现*/
    public static final int FACTORY_TARGET_SCORE_DATASET=17;
    /** 满意度联席会22重点城市分析*/
    public static final int MAIN_CITY_ANALYSIS=18;
    /**满意度联席会样本量对比值 */
    public static final int SWATCH_COMPARE_SIZE=30;
    /**总评分与往期对比*/
    public static final int TOTAL_PREVIOUS_DATASET = 31;
      
    /*报告类型定义 -- 数据库中相关字段： reporttype    */
	public static final int MONTH_SALES	= 0;//纵向月度销售报告 
	public static final int MONTH_AFTERSALES = 1;//纵向月度售后报告
	public static final int QUARTER_SALES = 2;//纵向季度销售报告
	public static final int QUARTER_AFTERSALES = 3;//纵向季度售后报告
	public static final int HALF_YEAR_SALES=4;//纵向半年销售报告
	public static final int HALF_YEAR_AFTERSALES=5;//纵向半年售后报告
	public static final int YEAR_SALES=6;//纵向全年销售报告
	public static final int YEAR_AFTERSALES=7;//纵向全年售后报告
	
	public static final int TABLE_MONTH_SALES = 8;//销售月度数据报告
	public static final int TABLE_MONTH_AFTERSALES = 9;//售后月度数据报告
	public static final int TABLE_QUARTER_SALES=10;//销售季度数据报告
	public static final int TABLE_QUARTER_AFTERSALES=11;//售后季度数据报告
	public static final int TABLE_HALF_YEAR_SALES=12;//销售半年度数据报告
	public static final int TABLE_HALF_YEAR_AFTERSALES=13;//售后半年度数据报告
	public static final int TABLE_YEAR_SALES=14;//全年销售数据报告
	public static final int TABLE_YEAR_AFTERSALES=15;//全年售后数据报告
	
	public static final int MONTH_SALES_X=16;//横向月度销售报告
	public static final int MONTH_AFTERSALES_X=17;//横向月度售后报告
	public static final int QUARTER_SALES_X=18;//横向季度销售报告
	public static final int QUARTER_AFTERSALES_X=19;//横向季度售后报告
	public static final int HALF_YEAR_SALES_X=20;//横向半年度销售报告
	public static final int HALF_YEAR_AFTERSALES_X=21;//横向半年度售后报告
	public static final int YEAR_SALES_X=22;//横向全年销售报告
	public static final int YEAR_AFTERSALES_X=23;//横向全年售后报告
	
	public static final int TABLE_QUARTER_CONSULTANT = 24;//销售顾问季度数据报告
	public static final int QUARTER_CONSULTANT = 25;//销售顾问季度纵向分析报告
	public static final int TABLE_MONTH_CONSULTANT = 26; // 销售顾问月度数据报告
	public static final int MONTH_CONSULTANT = 27; // 销售顾问月度纵向分析报告
	
	public static final int TABLE_MONTH_AFTER_CONSULTANT = 28;
    public static final int MONTH_AFTER_CONSULTANT = 29;
    
	/*报告类别*/
    /**没有选择报告类型*/
    public static final int NUMERAL_NULL=0;//数据报告
	/**数据报告 */
	public static final int NUMERAL_REPORT=1;//数据报告
	/**横向分析报告 */
	public static final int ANALYSIS_REPORT_X=2;//横向分析报告
	/**纵向分析报告 */
	public static final int ANALYSIS_REPORT_Y=3;//纵向分析报告
	/**销售顾问报告 */
	public static final int CONSULTANT_REPORT=4;//销售顾问报告
	
	/*BM类型定义 */
	public static final int NO_BENCH = 0;//没有选择BM
	public static final int BM_COUNTRY = 1;//BM选择为全国
	public static final int BM_BIGARAE = 2;//BM选择为所在大区
	public static final int BM_SMALLAREA = 3;//BM选择为所在小区
	
	/*措施状态, 包括四种状态：*/
	public static final String STEP_WAIT = "W"; //待完成
	public static final String STEP_REMIT = "R"; //推迟
	public static final String STEP_FINISH = "F"; //已完成(标记为已完成，但须在页面上显示)
	public static final String STEP_DELETE = "D"; //删除(标记为删除，不需要在页面上显示，后台根据此标识定期删除措施数据)
	
	/*优先改进分析四区代码*/
	public static final int TOP_LEFT_CORCER = 1;//左上角区
	public static final int TOP_RIGHT_CORCER = 2;//右上角区
	public static final int BOTTOM_LEFT_CORCER=3;//左下角区
	public static final int BOTTOM_RIGHT_CORCER=4;//右下角区
	
	/*坐标配置*/
	public static final int ABSCISSA = 1;//X轴（横坐标）
	public static final int ORDINATE = 2;//Y轴（纵坐标）
	public static final int ABSCISSA_OF_INTERSECTION = 3;//焦点的横坐标
	public static final int ORDINATE_OF_INTERSECTION = 4;//焦点的纵坐标
	public static final int ARCHIVES_TRUE_RATE=5;//档案正确率
	
	/*pdf报告类型*/
	public static final int PRINT_ACTIONS_ALL = 0; //措施 打印全部
	public static final int PRINT_ACTIONS_FINISH = 1; //措施 打印已完成
	public static final int PRINT_REPORT_QUARTER = 2; //报告 季度
	public static final int PRINT_REPORT_MONTH = 2; //报告 月度
	
	/*后台帐户角色权限*/
	public static final String BACKEND_ADMIN = "A"; //超级管理员
	public static final String BACKEND_MANAGER = "M"; //普通管理员
	public static final String BACKEND_OTHER = "O"; //其他后台用户
	
	/*反馈意见标志位*/
	public static final String FEEDBACK_SALE = "1"; //属于销售反馈意见
	public static final String FEEDBACK_AFTER_SALE = "2"; //属于售后反馈意见

	/*导出经销商列表文件名称 */
	public static final String SHEET_NAME_DEALER = "dealerList";	
	
	/** 验证导入的 数据*/
	public static final int DATA_IMPORT_ERROR = -1;
	public static final int DATA_IMPORT_SUCCESS = 1;
	public static final int DATA_CHECK_ERROR = 2;
	
	/** 投诉文件的状态 */
	public static final String FILE_UPLOAD = "已上传";
	public static final String FILE_DOWNLOAD = "已下载";
	public static final String FILE_READ = "已读";
	public static final String FILE_DELETE = "已删除";
	
	/** 投诉文件是查看还是下载的标识*/
	public static final int OPERATE_FILE_READ = 1;
	public static final int OPERATE_FILE_DOWNLOAD = 2;
	
	/** 信息跟进：经销商处理状态*/
	public static final String DEALER_UNREAD = "未读";
	public static final String DEALER_READ = "已读";
	public static final String DEALER_HANDLE = "已处理";
	public static final String DEALER_HANDLE_INTIME = "正常处理";
	public static final String DEALER_HANDLE_OUTOFTIME = "过期处理";
	
	/** 信息跟进：当前 或 存档*/
	public static final String INFOTRACE_CURR = "1";     // 当前
	public static final String INFOTRACE_ARCHIVE = "2";  // 存档
	
	/** 信息跟进：客户联系状态*/
	public static final String CONTACT_SUCCESS = "1";   //联系客户成功
	public static final String CUSTOMER_REJECT = "2";   //客户拒访
	public static final String CONTACT_FAIL = "3";      //未联系到客户
	
	/** 信息跟进：区域经理审阅 */
	public static final String AREA_REPLY = "已审阅";
	public static final String AREA_NOT_REPLY = "未审阅";
	
	
	/** 信息跟进：下载表头的固定部分 */
	public static final String[] excelTitle = {"年份", "月份", "客户编号", "客户名称", "联系电话", "车型", "底盘号", "stime", "etime", "顾问编号",
	                                           "新华信编号", "经销商名称", "经销商代码", "经销公司", "大区代码", "经销公司代码","小区代码","Q24", "2Q2", "2Q3", "2Q4", "上传时间", "批次号",
	                                           "有效时间", "经销商处理状态","经销商处理时间", "联系客户状态", "区域审阅状态", "客户不满意原因", "客户跟进情况及结果", "经销商层面的改进措施", "满意度得分"
	};
	
	/** 信息跟进：下载表头的固定部分对应的数据库字段 */
	public static final String[] excelField = {"buy_year", "buy_month", "client_pk", "customer_name", "customer_telephone", "type", "chassis", "stime", "etime", "consultant_pk",
		                                       "JXSDM", "dealer_name", "dealer_code", "sales_company", "region", "sub_region","sarea_m_pk","Q24", "Q2Q2", "Q2Q3", "Q2Q4", "upload_time", "batchnum",
		                                       "invalid_time", "handle_status","handle_time","contact_customer_status", "area_reply", "unsatisfy_reason", "status_result", "step", "ssi_score"
		
	};
	
	/**密码修改记录，下载表头*/
	public static final String[] recordTitle={"登录名","用户名","修改人","修改时间"};
	/**密码修改记录，下载表头对应数据库字段*/
	public static final String[] recordFiled={"login_name","user_name","edit_user","edit_time"};
	
	
	
	/*
	 * 季度的数字与罗马数字符号之间的转换
	 * 采用数组进行存储，下标从 1 开始
	 * 例如：想
	 */
	private static final String[] QUARTER = new String[] {"0","Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ"};
	
	public static String transformToRome(int i){
		if (i < 1 || i > 4) {
			return "0";
		}else {
			return Constant.QUARTER[i];
		}
	}
	
	/**
	 * 将题号转化为显示用题号
	 * 如：题号为：Q006A1 则，转化后为 6A.1:<p>
	 * 转化规则：<p>
	 * 首字母为 Q 则：首先取qid的前4个字符，去除首字母Q，去除Q后面的0，直到第一个不是0的字符(希望不会出现 Q000的这种题号)。
	 * 然后判断是否有第五个字符，如果没有，则加 ":"返回，如果有，则判断第五个字符是否是数字，如果是数字，加"."加第五个字符(包含第五个字符)后面的字符，加":"返回
	 * 如果不是数字，则加第五个字符，判断是否存在第六个字符，有，则加"."加第六个字符(包含第六个字符)后面的字符，加":"返回，没有，则加":"返回。<p>
	 * 首字母非 Q 则：去掉首字母，加":"返回
	 * @param qid
	 * @return
	 */
	public static String parseQuestionNum(String qid){
		char[] temp = qid.toCharArray();
		if (temp[0] == 'Q' || temp[0] == 'q') {
			int index = 0;
			for (int i = 1; i < temp.length; i++) {
				if (temp[i] == '0') {
					index = i;
				}else if (index >= 0) {
					index = i;
					break;
				}
				
			}
			String displayQid = "";
			while (index <= 3) {
				displayQid += temp[index];
				index++;
			}
			
			if (index >= temp.length) {
				return displayQid + ": ";
			} else {
				for (int i = 0; i < 10; i++) {
					if (Integer.valueOf(i).toString().equalsIgnoreCase(Character.valueOf(temp[index]).toString())) {
						return displayQid + "." + temp[index] + " : ";
					}
				}
				displayQid += temp[index];
				index++;
				if (index >= temp.length) {
					return displayQid + ": ";
				} else {
					displayQid += ".";
					for (int i = index; i < temp.length; i++) {
						displayQid += temp[i];
					}
					return displayQid + ": ";
				}
			}
		} else {
			return qid.substring(1) + ": ";
		}
	}
}
