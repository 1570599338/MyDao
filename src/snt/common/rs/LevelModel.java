package snt.common.rs;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存结果集行模式
 */
public class LevelModel {
	/** 数据的列名称 */
	public String strValueClumn;
	/** 数据列的位置 */
	public int nValueLoc;
	/** 目标列名称 */
	public String strTargetColumn;
	/** 目标列位置 */
	public int nTargetColumn;
	/** 列名称 */
	public String strColumn;
	/** 列等级  */
	public int nrLevel[];
	/** 列等级  */
	public int thisLevelLength[];
	/** 列位置 */
	public int colIndex=0;
/**
 * 输入的内容
 * LEVEL(VALUE.COL.l1.l2.l3.l4)
 * 否则为CLUMON
 */
public LevelModel(String levelStr) throws Exception {
	levelStr = levelStr.toUpperCase();
	if (levelStr.indexOf("LEVELSUM(") == -1) {
		strColumn = levelStr;
		nrLevel = null;
		return;
	}
	int loc = levelStr.indexOf("->");
	if (loc > 0)
		strTargetColumn = levelStr.substring(0, loc);
	int loc1 = levelStr.indexOf("(");
	int loc2 = levelStr.indexOf(")");
	String strForm = "The correct form is LEVEL(VALUE.COL.l1.l2.l3.l4)";
	if (loc1 == -1 || loc2 == -1)
		throw new Exception("Error in expression: (" + levelStr + ")" + strForm);
	String strInfor = levelStr.substring(loc1 + 1, loc2);
	java.util.StringTokenizer st = new java.util.StringTokenizer(strInfor, ".");
	try {
		strValueClumn = st.nextToken();
		strColumn = st.nextToken();
	} catch (Exception e) {
		throw new Exception("Error in expression: (" + levelStr + ")" + strForm + " no column " + e);
	}
	List v = new ArrayList();
	while (st.hasMoreElements()) {
		v.add(st.nextElement());
	}
	nrLevel = new int[v.size()];
	thisLevelLength = new int[v.size()];
	int nL = 0;
	for (int i = 0; i < nrLevel.length; i++) {
		try {
			String str = (String) v.get(i);
			nrLevel[i] = Integer.parseInt(str);
			nL += nrLevel[i];
			thisLevelLength[i] = nL;
		} catch (Exception e) {
			throw new Exception(
				"Error in expression: (" + levelStr + ")" + strForm + " No." + (i + 1) + " is not Number type");
		}
	}
}
public int compare(String str1, String str2) {
	if (nrLevel == null)
		return compareDirect(str1, str2);
	/** 进行分级处理 */
	return compareDirect(str1, str2);
}
private int compareDirect(String str1, String str2) {
	for (int j = 0; j < str1.length() && j < str2.length(); j++) {
		int c = str1.charAt(j) - str2.charAt(j);
		if (c != 0)
			return c;
	}
	if (str1.length() != str2.length()) {
		return str1.length() - str2.length();
	}
	return 0;
}
public static LevelModel[] getLevelModel(String str[]) throws Exception{
	LevelModel[] lm=new LevelModel[str.length];
	for(int i=0;i<lm.length;i++)
	{
		lm[i]=new LevelModel(str[i]);
	}
	return lm;
}
/**
 * 根据分级方案得到上一级的数据
 */
public String getUppLevelValue(String str) {
	/** 可以在这个地方实现分级方案 */
	if (nrLevel == null) {
		if (str == null)
			return null;
		return str.trim();
	}
	str = str.trim();
	int n = str.length();
	int nUpperLength = 0;
	for (int i = 0; i < nrLevel.length; i++) {
		if (thisLevelLength[i] >= n) {
			break;
		}
		nUpperLength = thisLevelLength[i];
	}
	if (nUpperLength == 0)
		return null;
	return str.substring(0, nUpperLength);
}
}
