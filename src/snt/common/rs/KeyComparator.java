/*
 * 创建日期 2005-7-21
 *
 * 
 */
package snt.common.rs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 *
 */
public class KeyComparator implements Comparator {
	private HashMap valueMap = null;
	
	public KeyComparator(List valueOrder){
		if(valueOrder != null){
			valueMap = new HashMap();
			ListIterator listIt = valueOrder.listIterator();
			int index = 0;
			while (listIt.hasNext()) {
				Object value = (Object) listIt.next();
				valueMap.put(value, new Integer(index++));
			}
		}
	}
	
	/* （非 Javadoc）
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if (valueMap != null) {
			Integer index1 = (Integer)valueMap.get(o1);
			Integer index2 = (Integer)valueMap.get(o2);
			if (index1 == null && index2 == null) {
				return MemoryResultSetUtils.compare(o1, o2);
			}
			else{
				return MemoryResultSetUtils.compare(index1, index2);
			}
		}
		return MemoryResultSetUtils.compare(o1, o2);
	}

}
