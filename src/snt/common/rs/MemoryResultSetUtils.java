package snt.common.rs;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import snt.common.rs.statistics.AsstSumValue;


/**
 * 内存结果集的工具类
 * @author <a href="mailto:yangxiong@ufida.com.cn">阳雄</a>
 * @since NC3.0
 */
public class MemoryResultSetUtils
    extends MrsToolBase {

    /**
     * Key值的比较
     * @author 阳雄
     */
    private static class CompKeysComparator
        implements Comparator {
        private boolean[] m_ascending = null;
        CompKeysComparator(boolean[] ascending) {
            m_ascending = ascending == null ? new boolean[0] : ascending;
        }

        /**
         * Compares its two arguments for order.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the first
         *   argument is less than, equal to, or greater than the second.
         */
        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof CombRowKey) ||
                !(o2 instanceof CombRowKey)) {
                return 0;
            }
            CombRowKey crk1 = (CombRowKey)o1;
            CombRowKey crk2 = (CombRowKey)o2;
            for (int i = 0; i < crk1.getRow().length; i++) {
                Object v1 = crk1.getRow()[i];
                Object v2 = crk2.getRow()[i];
                /** 空值比较 */
                if (v1 == v2) {
                    continue;
                }
                else if (v1 == null) {
                    return isAscending(i) ? -1 : 1;
                }
                else if (v2 == null) {
                    return isAscending(i) ? 1 : -1;
                }
                if (v1 instanceof Comparable && v2 instanceof Comparable) {
                    int c = ((Comparable)v1).compareTo(v2);
                    if (c == 0) {
                        continue;
                    }
                    else {
                        return isAscending(i) ? c : -c;
                    }
                }
                else {
                    return 0;
                }
            }
            return crk1.getId() - crk2.getId();
        }

        /**
         * Indicates whether some other object is &quot;equal to&quot; this
         * Comparator.
         *
         * @param obj the reference object with which to compare.
         * @return <code>true</code> only if the specified object is also a
         *   comparator and it imposes the same ordering as this comparator.
         */
        public boolean equals(Object obj) {
            return false;
        }

        private boolean isAscending(int i) {
            if (i < m_ascending.length) {
                return m_ascending[i];
            }
            return true;
        }
    }

    /**
     * 将父子关系的对象转换到级次编码
     * 返回级次编码到对象的映射表，extraInfo第一个元素为树的最大级次，第二个元素为每级位数
     * @param parent_Child List
     * @param extraInfo int[] [out]类型参数，可空(如果不需要获取额外信息)。
     * @return HashMap
     */
    private static HashMap convertPCObj2Code(List parent_Child,
                                             final int[] extraInfo) {
        //生成子-父映射并对所有键值排序
        HashMap hmChild2Parent = new HashMap();
        TreeSet sortSet = new TreeSet();
        for (int i = 0, kn = parent_Child.size(); i < kn; i++) {
            Object[] pair = (Object[])parent_Child.get(i);
            if (pair[0] != null) {
                sortSet.add(pair[0]);
            }
            sortSet.add(pair[1]);
            hmChild2Parent.put(pair[1], pair[0]);
        }

        //对每个键值生成树节点
        Iterator it = sortSet.iterator();
        HashMap hmObj2TreeNode = new HashMap();
        while (it.hasNext()) {
            Object obj = it.next();
            hmObj2TreeNode.put(obj, new DefaultMutableTreeNode(obj));
        }

        //构造树
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        it = sortSet.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode)hmObj2TreeNode.get(obj);
            Object parentObj = hmChild2Parent.get(obj);
            DefaultMutableTreeNode parent = null;
            if (parentObj == null) {
                parent = root;
            }
            else{
                parent = (DefaultMutableTreeNode) hmObj2TreeNode.get(parentObj);
                if (parent == null) {
                    parent = root;
                }
            }
            parent.add(child);
        }

        //给每个节点赋一个子编码
        int maxChildCount = 0;
        int maxDepth = root.getDepth();
        Enumeration nodeEnum = root.preorderEnumeration();
        HashMap hmObj2SubCode = new HashMap();
        while (nodeEnum.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeEnum.
                nextElement();
            if (node.isLeaf()) {
                continue;
            }
            int childCount = node.getChildCount();
            maxChildCount = Math.max(childCount, maxChildCount);
            for (int i = 0; i < childCount; i++) {
                hmObj2SubCode.put(
                    ((DefaultMutableTreeNode)node.getChildAt(i)).
                    getUserObject(),
                    new Integer(i));
            }
        }

        //计算每级编码需要的位数(根据可能的最大子节点数计算)
        int digit = (int)Math.round(Math.log(maxChildCount) / Math.log(10));
        int codeBase = (int)Math.pow(10, digit - 1);
        if (maxChildCount > 9 * codeBase) {
            codeBase *= 10;
            digit += 1;
        }

        //将子编码组织成完整编码
        HashMap hmCode2Obj = new HashMap();
        Iterator it2 = sortSet.iterator();
        while (it2.hasNext()) {
            Object obj = it2.next();
            String code = getCode(obj, hmObj2SubCode, hmChild2Parent, codeBase);
            hmCode2Obj.put(code, obj);
        }

        if (extraInfo != null) {
            if (extraInfo.length >= 2) {
                extraInfo[0] = maxDepth;
                extraInfo[1] = digit;
            }
            else if (extraInfo.length == 1) {
                extraInfo[0] = maxDepth;
            }
        }
        return hmCode2Obj;
    }
    
   
    public static List convertValueList(List objValue, int type){
    	for (int i = 0, in = objValue.size(); i < in; i++) {
			objValue.set(i, convertValue(objValue.get(i), type));
		}
    	return objValue;
    }
    
    /**
     * 将值转换到相应的类型
     *
     * @param objValue Object
     * @param type int @see Types
     * @return Object
     */
    public static Object convertValue(Object objValue, int type) {
        if (objValue == null) {
            return null;
        }
        //对于辅助项小计合计特殊处理一下
        if (objValue instanceof AsstSumValue) {
            return objValue;
        }
        Object newValue = null;
        switch (type) {
	        case Types.CHAR:
	        case Types.VARCHAR:{
	        	newValue = objValue == null?objValue:objValue.toString();
	        }
            case Types.DECIMAL:
            	if(objValue instanceof BigDecimal){
                    newValue = objValue;
                }
                else if (objValue instanceof Number) {
                    newValue = new BigDecimal(((Number)objValue).doubleValue());
                }
                else {
                    newValue = new BigDecimal(objValue.toString());
                }
                break;
            case Types.DOUBLE: {
                if(objValue instanceof Double){
                    newValue = objValue;
                }
                else if (objValue instanceof Number) {
                    newValue = new Double(((Number)objValue).doubleValue());
                }
                else {
                    newValue = new Double(objValue.toString());
                }
                break;
            }
            case Types.INTEGER: {
                if (objValue instanceof Number) {
                    newValue = new Integer(((Number)objValue).intValue());
                }
                else {
                    double value = Double.parseDouble(objValue.toString());
                    newValue = new Integer((int)Math.round(value));
                }
                break;
            }
            case Types.BIGINT: {
                if (objValue instanceof Number) {
                    newValue = new Long(((Number)objValue).longValue());
                }
                else {
                    double value = Double.parseDouble(objValue.toString());
                    newValue = new Long(Math.round(value));
                }
                break;
            }
            case Types.DATE: {
            	if (objValue instanceof java.sql.Date) {
					 newValue = objValue;
				} else if (objValue instanceof java.util.Date) {
					newValue = new java.sql.Date(((java.util.Date)objValue).getTime());
				} else {
					newValue = java.sql.Date.valueOf(String.valueOf(objValue));
				}
            }
            case Types.TIMESTAMP: {
            	if (objValue instanceof Timestamp) {
					newValue = objValue;
				} else if (objValue instanceof java.util.Date) {
					newValue = new Timestamp(((java.util.Date)objValue).getTime());
				} else {
					newValue = Timestamp.valueOf(String.valueOf(objValue));
				}
            }
            default: {
                newValue = objValue;
            }
        }
        return newValue;
    }

    /**
     * 将内存结果集中父子关系的对象转换到级次编码，放置到fakeCodeColName所指列中<br>
     * （如不存在该列，则添加该列）；返回级次编码到对象的映射表，extraInfo第一个<br>
     * 元素为树的最大级次，第二个元素为每级位数。
     *
     * @param mrs MemoryResultSet
     * @param pcCols String[] 第一个元素为父列，第二个元素为子列
     * @param fakeCodeColName String
     * @param extraInfo int[] [out]类型参数，可空(如果不需要获取额外信息)。
     * @return HashMap
     * @throws SQLException
     */
    public static HashMap createFakeCodeByPCObj(MemoryResultSet mrs,
                                                String[] pcCols,
                                                String fakeCodeColName,
                                                final int[] extraInfo) throws
        SQLException {
        //取出内存结果集中这两列对应的各行的值
        List keys = fetchColValues(mrs, pcCols);
        //将父子关系转换为编码级次
        HashMap hmCode2Obj = convertPCObj2Code(keys, extraInfo);
        //如果不存在伪编码列，在内存结果集总添加临时列
        try {
            mrs.findColumn(fakeCodeColName);
        }
        catch (SQLException ex) {
            mrs.appendColumnByDefaultValue(fakeCodeColName, Types.VARCHAR, "");
        }

        //给该临时列填上对应的值
        HashMap hmObj2Code = new HashMap();
        Iterator it = hmCode2Obj.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            hmObj2Code.put(hmCode2Obj.get(key), key);
        }
        fillColValue(mrs, hmObj2Code, pcCols[1],
                     fakeCodeColName);

        return hmCode2Obj;
    }

    /**
     * 取得内存结果集中第colIndex列从beginRow到endRow的数据。
     * 取得的结果按纵向排列
     * @param mrs MemoryResultSet
     * @param colIndex int 以0为基
     * @param beginRow int 包含
     * @param endRow int 不含
     * @return Object[]
     * @throws SQLException
     */
    public static Object[] fetchColValuesByColDir(MemoryResultSet mrs, int colIndex,
                                          int beginRow, int endRow) throws
        SQLException {
        List dataArray = mrs.getResultList();
        //如果设倒了则我们应该助人为乐
        if (beginRow > endRow) {
            int temp = beginRow;
            beginRow = endRow;
            endRow = temp;
        }
        if (beginRow >= dataArray.size() || endRow <= 0) {
            return new Object[0];
        }
        else if (beginRow < 0) {
            beginRow = 0;
        }
        else if (endRow > dataArray.size()) {
            endRow = dataArray.size();
        }

        //获取该列各行的值
        Object[] values = new Object[endRow - beginRow];
        for (int i = beginRow; i < endRow; i++) {
            List v = (List)dataArray.get(i);
            values[i - beginRow] = v.get(colIndex);
        }
        return values;
    }

    /**
     * 取得结果集中某几列的值。<br>
     * 返回值为一列表，列表中每个元素为一个数组，对应内存结果集中一行中各列的值。
     *
     * @param mrs MemoryResultSet
     * @param colIndices int[]  各列的序号，以0为基
     * @return List
     * @throws SQLException
     */
    public static List fetchColValues(MemoryResultSet mrs, int[] colIndices) throws
        SQLException {
        List dataArray = mrs.getResultList();
        List colData = new ArrayList(dataArray.size());
        for (int i = 0, rn = dataArray.size(); i < rn; i++) {
            List v = (List)dataArray.get(i);
            Object[] oa = fetchObject(v, colIndices);
            colData.add(oa);
        }
        return colData;
    }
    
    /**
     * 取得结果集中某几列的值。<br>
     * 返回值为一列表，列表中每个元素为一个列表，对应内存结果集中一列中各行的值。
     *
     * @param mrs MemoryResultSet
     * @param colIndices int[]  各列的序号，以0为基
     * @return List
     * @throws SQLException
     */
    public static List fetchColValuesByColDir(MemoryResultSet mrs, int[] colIndices) throws
        SQLException {
        List dataArray = mrs.getResultList();
        List colData = new ArrayList(colIndices.length);
        for (int i = 0; i < colIndices.length; i++) {
			colData.add(new ArrayList(dataArray.size()));			
		}
        for (int i = 0, rn = dataArray.size(); i < rn; i++) {
            List v = (List)dataArray.get(i);
            Object[] oa = fetchObject(v, colIndices);
            for (int j = 0; j < oa.length; j++) {
            	((List)colData.get(j)).add(oa[j]);				
			}            
        }
        return colData;
    }

    /**
     * 取得结果集中某几列的值。<br>
     * 返回值为一列表，列表中每个元素为一个数组，对应内存结果集中一行中各列的值。
     *
     * @param mrs MemoryResultSet
     * @param colNames String[]
     * @return List
     * @throws SQLException
     */
    public static List fetchColValues(MemoryResultSet mrs, String[] colNames) throws
        SQLException {
        int[] colIndices = fetchIndex(mrs.getMetaData0(), colNames);
        return fetchColValues(mrs, colIndices);
    }
    
    
    /**
     * 取得结果集中某几列的值。<br>
     * 返回值为一列表，列表中每个元素为一个列表，对应内存结果集中一列中各行的值。
     *
     * @param mrs MemoryResultSet
     * @param colNames String[]
     * @return List
     * @throws SQLException
     */
    public static List fetchColValuesByColDir(MemoryResultSet mrs, String[] colNames) throws
        SQLException {
    	int[] colIndices = fetchIndex(mrs.getMetaData0(), colNames);
        return fetchColValuesByColDir(mrs, colIndices);
    }

    /**
     * 在映射表中找到对应于sourceCol列中值的值填入destCol中。
     *
     * @param mrs MemoryResultSet
     * @param hmValueMap HashMap
     * @param sourceCol String
     * @param destCol String
     * @throws SQLException
     */
    public static void fillColValue(MemoryResultSet mrs, HashMap hmValueMap,
                                    String sourceCol, String destCol) throws
        SQLException {
        int sourceIndex = mrs.getMetaData0().getNameIndex(sourceCol);
        int destIndex = mrs.getMetaData0().getNameIndex(destCol);
        List dataArray = mrs.getResultList();
        for (int i = 0, rn = dataArray.size(); i < rn; i++) {
            List v = (List)dataArray.get(i);
            Object s = v.get(sourceIndex);
            if (s == null) {
                continue;
            }
            v.set(destIndex, hmValueMap.get(s));
        }
    }
    
    /**
     * 取出列表中的元素依次填充内存结果集中某列的各行。
     * @param mrs MemoryResultSet
     * @param colIndex int 以零为基的列序号
     * @param colValueList List 列表
     * @throws SQLException
     */
    public static void fillColValue(MemoryResultSet mrs, int colIndex, List colValueList) throws SQLException{
    	List dataArray = mrs.getResultList();
        for (int i = 0, rn = Math.min(dataArray.size(), colValueList.size()); i < rn; i++) {
            List v = (List)dataArray.get(i);
            v.set(colIndex, colValueList.get(i));
        }
    }

    /**
     * 得到Obj对象对应的编码
     *
     * @param obj java.lang.Object
     * @param hmObj2SubCode java.util.HashMap
     * @param hmChild2Parent java.util.HashMap
     * @param codeBase int
     * @return String
     */
    private static String getCode(
        Object obj,
        HashMap hmObj2SubCode,
        HashMap hmChild2Parent,
        int codeBase) {
        List list = new ArrayList();
        while (obj != null) {
            Integer iSubCode = (Integer)hmObj2SubCode.get(obj);
            list.add(new Integer(iSubCode.intValue() + codeBase));
            obj = hmChild2Parent.get(obj);
        }
        StringBuffer strBuf = new StringBuffer();
        for (int i = list.size() - 1; i >= 0; i--) {
            strBuf.append(list.get(i));
        }
        return strBuf.toString();
    }

    /**
     * 将值转换成double
     * @param objValue
     * @return double
     */
    public static double getValueAsDouble(Object objValue){
        double value = 0.0;//空值当成0来处理
        if (objValue != null) {
            if (objValue instanceof Number) {
                value = ((Number)objValue).doubleValue();
            }
            else {
                value = Double.parseDouble(objValue.toString());
            }
        }
        return value;
    }

    /**
     * 打印内存结果集(测试用)
     *
     * @param mrs MemoryResultSet
     */
    public static void printOutMrs(MemoryResultSet mrs, PrintStream out) {
        try {
            MemoryResultSetMetaData mrsMetaData = mrs.getMetaData0();
            int colCount = mrsMetaData.getColumnCount();
            out.println("-------------------MemoryResultSet-------------------");
            for (int i = 0; i < colCount; i++) {
                out.print(mrsMetaData.getColumnName(i + 1));
                out.print("\t");
            }
            out.println();
            mrs.beforeFirst();
            while(mrs.next()){
            	for (int j = 0; j < colCount; j++) {
            		out.print(mrs.getString(j+1));
            		out.print("\t");
            	}
            	out.println();
            }
            out.println("-------------------MemoryResultSet-------------------");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * 打印内存结果集(测试用)
     *
     * @param mrs MemoryResultSet
     */
    public static void printOutMrs(MemoryResultSet mrs, PrintWriter out) {
        try {
            MemoryResultSetMetaData mrsMetaData = mrs.getMetaData0();
            int colCount = mrsMetaData.getColumnCount();
            out.println("-------------------MemoryResultSet-------------------");
            for (int i = 0; i < colCount; i++) {
                out.print(mrsMetaData.getColumnName(i + 1));
                out.print("\t");
            }
            out.println();
            mrs.beforeFirst();
            while(mrs.next()){
            	for (int j = 0; j < colCount; j++) {
            		out.print(mrs.getString(j+1));
            		out.print("\t");
            	}
            	out.println();
            }
            out.println("-------------------MemoryResultSet-------------------");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 对内存结果集进行排序
     *
     * @param mrs MemoryResultSet
     * @param colNames int[]
     * @param ascending boolean[]
     * @throws SQLException
     */
    public static void sortMrs(MemoryResultSet mrs, String[] colNames,
                               boolean[] ascending) throws SQLException {
        int colCount = (colNames == null) ? 0 : colNames.length;
        if (colCount == 0) {
            return;
        }
        List compKeys = fetchComKey(mrs, colNames);
        Collections.sort(compKeys, new CompKeysComparator(ascending));
        List resultArray = mrs.getResultList();
        resultArray.clear();
        for (int i = 0, cn = compKeys.size(); i < cn; i++) {
            CombRowKey crk = (CombRowKey)compKeys.get(i);
            resultArray.add(crk.getUserObject());
        }
    }

    /**
     * 空实现，我们不用
     * @return MemoryResultSet
     * @throws SQLException
     */
    public MemoryResultSet execute() throws SQLException {
        throw new UnsupportedOperationException("未实现");
    }
}
