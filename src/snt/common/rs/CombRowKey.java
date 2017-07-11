package snt.common.rs;


/**
 * 合并比较对象 创建日期：(2001-10-18 15:50:04)
 * 
 * @author ：阳雄
 */
public class CombRowKey extends RowKey {
	/** 用户的数值 ，在进行合并时随着KEY值 一起调整在队列中的位置 */
	private java.lang.Object m_UserObject;

	/**
	 * CompareRowKey 构造子注解。
	 * 
	 * @param oa
	 *            java.lang.Object[]
	 */
	public CombRowKey(java.lang.Object[] oa) {
		super(oa);
	}
	
	public java.lang.Object getUserObject() {
		return m_UserObject;
	}

	public void setUserObject(java.lang.Object newUserObject) {
		m_UserObject = newUserObject;
	}	
}