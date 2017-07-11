package snt.common.rs;

/**
 * 此处插入类型描述。
 * 创建日期：(2001-10-20 10:53:34)
 * @author ：阳雄
 */
public interface IResultSetConst {
	//////////// COMBINE
	int SORT_ASCEND = 0;
	int SORT_DEASCEND = 1;
	int SIMPLE_COMBINE = 2;
	///////////// JOIN
	int LEFT_OUT_JOIN = 0;
	int RIGHT_OUT_JOIN = 1;
	int INNER_JOIN = 2;
	int JOIN_ALL = 3;
	///////////// SUM
	int COMBINE_MODE = 1;
	int JOIN_MODE = 0;
	//////////// INTER LINE LINE    计算
	int APPEND_LINE = 2;
	int LOOP_LINE = 1;
	int INIT_LINE = 0;
	/** SUM AVG COUNT 只对LOOP LINE 有效*/
	int VT_NOMAL = 0;
	String STR_UPLINE_PREFIX = "UPLINE_";
	int VT_UPLINE = 1;
	String STR_SUMLINE_PREFIX = "SUMLINE_";
	int VT_SUMLINE = 2;
	String STR_AVGLINE_PREFIX = "AVGLINE_";
	int VT_AVGLINE = 3;
	String STR_COUNTLINE_PREFIX = "COUNTLINE_";
	int VT_COUNTLINE = 4;
	String STR_CUR_PREFIX = "CUR_";
	int VT_CUR = 5;
	String STR_UPLINEFORCE_PREFIX = "UPLINEFORCE_";
	int VT_UPLINEFORCE = 6;
	String STR_YESUMLINE_PREFIX = "YESUM_";
	int VT_YESUMLINE = 7;
	String STR_ASSISTSUMLINE_PREFIX = "ASSISTSUM_";
	int VT_ASSISTSUMLINE = 8;
}
