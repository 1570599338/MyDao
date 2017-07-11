package snt.common.rs;

import java.util.Comparator;

/**
 * 大部分数值相同不能使用这个方法。 创建日期：(2001-10-17 22:18:47)
 * 
 * @author 阳雄
 */
public class RowKeyComparator implements Comparator {
	private boolean beAscend = true;
	
	private KeyComparator[] comparators = null;

	/**
	 * Compare 构造子注解。
	 */
	public RowKeyComparator() {
		super();
	}

	/**
	 * 
	 */
	public int compare(Object arg1, Object arg2) {
		if (!(arg1 instanceof RowKey)) {
			return isAscend()?-1:1;
		}
		if (!(arg2 instanceof RowKey)) {
			return isAscend()?1:-1;
		}
		RowKey crk1 = (RowKey) arg1;
		RowKey crk2 = (RowKey) arg2;
		
		int nR = CombRowKey.compareRowKey(crk1, crk2, comparators);
		if (isAscend())
			return nR;
		else
			return -nR;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-18 16:33:25)
	 * 
	 * @return boolean
	 */
	public boolean isAscend() {
		return beAscend;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-18 16:33:25)
	 * 
	 * @param newAscend
	 *            boolean
	 */
	public void setAscend(boolean newAscend) {
		beAscend = newAscend;
	}
	
	
	/**
	 * @return 返回 comparators。
	 */
	public KeyComparator[] getComparators() {
		return comparators;
	}
	/**
	 * @param comparators 要设置的 comparators。
	 */
	public void setComparators(KeyComparator[] comparators) {
		this.comparators = comparators;
	}
}