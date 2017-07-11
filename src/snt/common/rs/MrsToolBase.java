package snt.common.rs;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import snt.common.string.StringUtil;

/**
 * 处理记录处理中公共的内容 创建日期：(2001-10-18 16:02:35)
 * 
 * @author ：阳雄
 */
public abstract class MrsToolBase {
	/**
	 * MrsToolBase 构造子注解。
	 */
	public MrsToolBase() {
		super();
	}
	
	public abstract MemoryResultSet execute() throws java.sql.SQLException;

	/**
     * 判断两个对象是否相等
     *
     * @param obj1 Object
     * @param obj2 Object
     * @return boolean
     */
    public static boolean equals(Object obj1, Object obj2) {
        return (obj1 == null)?(obj2 == null):obj1.equals(obj2);
    }
    
    public static int compare(Object obj1, Object obj2){
    	if (obj1 == null || obj2 == null) {
			return (obj1==null)?-1:1;
		}
    	if (obj1.equals(obj2)) {
			return 0;
		}
    	if (obj1 instanceof Comparable){
    		return ((Comparable)obj1).compareTo(obj2);
    	}
    	else if(obj2 instanceof Comparable){
    		return -((Comparable)obj2).compareTo(obj1);
    	}
    	else{
			String str1 = String.valueOf(obj1);
			String str2 = String.valueOf(obj2);
			return str1.compareTo(str2);
    	}
    }

    /**
     * 判断两个对象是否相等
     *
     * @param objArray1 Object[]
     * @param objArray2 Object[]
     * @return boolean
     */
    public static boolean equals(Object[] objArray1, Object[] objArray2) {
        return Arrays.equals(objArray1, objArray2);
    }
    
	public static int compare(List al1, List al2, int index[]) {
		for (int i = 0; i < index.length; i++) {
			int loc = index[i];
			Object v1 = al1.get(loc);
			Object v2 = al2.get(loc);
			
			int c = compare(v1, v2);
			if (c == 0) {
				continue;
			} else {
				return c;
			}
		}
		return 0;
	}

	public static List copyRow(List source, int indexs[]) {
		List newLine = new ArrayList();
		for (int j = 0; j < source.size(); j++)
			newLine.add(null);
		if (indexs != null){
			for (int j = 0; j < indexs.length; j++) {
				newLine.set(indexs[j], source.get(indexs[j]));
			}
		}
		return newLine;
	}

	public static int[] fetchIndex(MemoryResultSetMetaData mrsmd,
			String[] names) throws java.sql.SQLException {
		int keys[] = new int[names.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = mrsmd.getNameIndex(names[i]);
		}
		return keys;
	}
	
	public static int[] fetchIndex0(MemoryResultSetMetaData mrsmd,
			String[] names) throws java.sql.SQLException {
		int keys[] = new int[names.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = mrsmd.getNameIndex0(names[i]);
		}
		return keys;
	}

	public static Object[] fetchObject(List vc, int[] keys) {
		Object[] oneKey = new Object[keys.length];
		for (int j = 0; j < oneKey.length; j++) {
			oneKey[j] = vc.get(keys[j]);
		}
		return oneKey;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-18 16:29:27)
	 * 
	 * @return java.util.List
	 * @param vResultSet
	 *            java.util.List
	 * @param keys
	 *            int[]
	 */
	public static List fetchComKey(List vResultSet, int[] keys) {
		List vRowKey = new ArrayList();
		for (int i = 0; i < vResultSet.size(); i++) {
			List v = (List) vResultSet.get(i);
			Object[] oa = fetchObject(v, keys);
			CombRowKey crk = new CombRowKey(oa);
			crk.setUserObject(v);
			vRowKey.add(crk);
		}
		return vRowKey;
	}

	public static List fetchComKey(MemoryResultSet mrs, String[] fields)
			throws java.sql.SQLException {
		int keys[] = fetchIndex((MemoryResultSetMetaData) mrs.getMetaData(),
				fields);
		List vRowKey = new ArrayList();
		List al = mrs.getResultList();
		for (int i = 0; i < al.size(); i++) {
			List v = (List) al.get(i);
			Object[] oa = fetchObject(v, keys);
			CombRowKey crk = new CombRowKey(oa);
			crk.setMemoryResultSet(mrs);
			crk.setId(i);
			crk.setUserObject(v);
			vRowKey.add(crk);
		}
		return vRowKey;
	}

	public static List getNullLine(int length) {
		List newLine = new ArrayList();
		for (int j = 0; j < length; j++)
			newLine.add(null);
		return newLine;
	}

	public static String[] getToken(String row, String tk) {
		if (row == null)
			return new String[0];		
		return StringUtil.split(row, tk);
	}

	protected static int compare(List al1, List al2, LevelModel lma[]) {
		for (int i = 0; i < lma.length; i++) {
			/** 空值比较 */
			int loc = lma[i].colIndex;
			Object v1 = al1.get(loc);
			Object v2 = al2.get(loc);
			if (v1 == v2)
				continue;
			if (v1 == null)
				return -1;
			if (v2 == null)
				return 1;
			String str1 = null;
			if (v1 instanceof String)
				str1 = (String) v1;
			else
				str1 = "" + v1;
			String str2 = null;
			if (v2 instanceof String)
				str2 = (String) v2;
			else
				str2 = "" + v2;
			int n = lma[i].compare(str1, str2);
			if (n != 0)
				return n;
		}
		return 0;
	}

	protected static int[] fetchIndex(MemoryResultSetMetaData mrsmd,
			LevelModel[] lm) throws java.sql.SQLException {
		int keys[] = new int[lm.length];
		for (int i = 0; i < keys.length; i++) {
			lm[i].colIndex = keys[i] = mrsmd.getNameIndex(lm[i].strColumn);
			if (lm[i].strTargetColumn != null)
				lm[i].nTargetColumn = mrsmd.getNameIndex(lm[i].strTargetColumn);
			else
				lm[i].nTargetColumn = mrsmd.getNameIndex(lm[i].strColumn);
			if (lm[i].strValueClumn != null)
				lm[i].nValueLoc = mrsmd.getNameIndex(lm[i].strValueClumn);
			else
				lm[i].nValueLoc = -1;
		}
		return keys;
	}
	
	/**
     * 判断某类型是否属于数值类型
     * 创建日期：(02-3-25 9:02:19)
     * @return boolean
     */
    public static boolean isNumberType(int iDataType) {
        boolean b = false;
        switch (iDataType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.TINYINT:
            case Types.NUMERIC: {
                b = true;
            }
        }
        return b;
    }
    
    /**
	 * 此处插入方法说明。 创建日期：(2002-12-2 10:35:40)
	 * 
	 * @return int
	 * @param clas
	 *            java.lang.Class
	 */
	public static int class2SqlType(Class clas) {
		int sqlType = Types.VARCHAR;
		if (clas == Integer.class)
			sqlType = Types.INTEGER;
		else if (clas == Double.class)
			sqlType = Types.DOUBLE;
		else if (clas == BigDecimal.class)
			sqlType = Types.DECIMAL;
		else if (clas == Long.class)
			sqlType = Types.BIGINT;
		else if (clas == Short.class)
			sqlType = Types.SMALLINT;
		else if (clas == Byte.class)
			sqlType = Types.TINYINT;
		return sqlType;
	}
	
	/**
	 * 将对象转换成对应的类型
	 * @param result
	 * @param type
	 * @param scale
	 * @return Object
	 */
	public static Object changeObject2CorrectType(Object result, int type, int scale) {
        Object obj = null;
        if (result != null) {
            switch (type) {
                case Types.SMALLINT: {
                	if (result instanceof Short) {
                        obj = result;
                    }
                	else if (result instanceof Number) {
                        double v = ((Number) result).doubleValue();
                        obj = new Short(new Double(v).shortValue());
                    }
                    else {
                        String str = result.toString();
                        int index = str.indexOf(".");
                        str = str
                                .substring(0, index > 0 ? index : str.length());
                        obj = Short.valueOf(str);
                    }
                    break;
                }
                case Types.INTEGER: {
                    if (result instanceof Integer) {
                        obj = result;
                    }
                    else if (result instanceof Number) {
                        double v = ((Number) result).doubleValue();
                        obj = new Integer(new Double(v).intValue());
                    }
                    else {
                        String str = result.toString();
                        int index = str.indexOf(".");
                        str = str
                                .substring(0, index > 0 ? index : str.length());
                        obj = Integer.valueOf(str);
                    }
                    break;
                }
                case Types.DOUBLE: {
                	if (result instanceof Double) {
                        obj = result;
                    }
                	else if (result instanceof Number) {
                        double v = ((Number) result).doubleValue();
                        obj = new Double(v);
                    }
                    else {
                        obj = Double.valueOf(result.toString());
                    }
                    break;
                }
                case Types.DECIMAL: {
                    if (result instanceof BigDecimal) {
                        obj = result;
                    }
                    else if (result instanceof Number) {
                        double v = ((Number) result).doubleValue();
                        obj = new BigDecimal(v);
                    }
                    else {
                        obj = new BigDecimal(result.toString());
                    }
                    obj = ((BigDecimal)obj).setScale(scale, BigDecimal.ROUND_HALF_UP);
                    break;
                }
                case Types.BOOLEAN: {
                    if (result instanceof Boolean) {
                        obj = result;
                    }
                    else {
                        obj = new Boolean(
                                Double.parseDouble(result.toString()) > 0);
                    }
                    break;
                }
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:{
                    obj = result.toString();
                    break;
                }
                default:
                    obj = result;
                    break;
            }
        }
        return obj;
    }
}