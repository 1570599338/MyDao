/**
 * 
 */
package snt.common.dao.base;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 主键生成器<br>
 * 提供了长整型主键、整型主键、字符型主键和整型键值四种生成器<br/>
 * 其中长整型主键、整型主键和字符型主键在集群环境下用来作为数据库主键也是安全的（需要在环境中设置节点号）<br/>
 * 而整型键值生成器可以保证在集群环境下唯一，但不具备持久性的唯一，不能作为数据库主键。但其效率<br/>
 * 较高，在不需要考虑持久化的时候应该优先选用。<br/>
 * 使用字符型主键应保证对应字段长度在16以上
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class PrimaryKeyGenerator {
	private static AtomicLong atomicLong = null;
	private static AtomicInteger atomicInteger = null;
	/**考虑在集群环境下应该记录节点号*/
	private static int nodeID = 0;
	/**获取种子接口*/
	private static IPrimaryKeySeed primaryKeySeed;
	/**整型主键种子*/
	private static int intKey = 0;
	/**整型主键上限*/
	private static int intKeyBound = 0;
	/**每次取主键的个数*/
	private static int intKeyInterval = 200;

	static{
		long time = System.currentTimeMillis();
		int intTime = 0;//(int)(time&0x0FFFFFFF);//TODO 还是应该修改为更好的方式
		atomicInteger = new AtomicInteger(intTime);
		atomicLong = new AtomicLong(time);
	}

	/**
	 * 得到当前节点号
	 * @return 当前节点号
	 */
	public static int getNodeID(){
		return nodeID;
	}
	
	/**
	 * 设置节点号
	 * @param nodeID
	 */
	static void setNodeID(int nodeID) {
		assert nodeID<=0xFF:"怎么会有那么多节点？节点号必须小于16";
		PrimaryKeyGenerator.nodeID = nodeID;
		atomicLong.getAndAdd((long)nodeID<<60);
		atomicInteger.getAndAdd(nodeID<<28);
	}
	
	/**
	 * 获得长整型主键
	 * @return long 长整型主键
	 */
	public static long getLongKey(){
		return atomicLong.getAndIncrement();
	}
	
	/**
	 * 获得字符型主键（长）
	 * @return String 字符型主键
	 */
	public static String getStringKey(){
		return Long.toHexString(getLongKey());
	}
	
	public static String getShortStringKey(){
		return Integer.toHexString(getIntKey());
	}
	
	/**
	 * 获得整型主键
	 * @return int 整型主键
	 */
	public static synchronized int getIntKey(){
		if (intKey == intKeyBound) {//当前进程的主键已经用完，必须向数据库重新申请
			genIntKeyInterval();
		}
		return intKey++;
	}
	
	/**
	 * 获得整型键值(不具备持久性唯一，但是效率较高）
	 * @return 整型键值
	 */
	public static int getIntID(){
		return atomicInteger.getAndIncrement();
	}
	
	private static void genIntKeyInterval(){		
		intKey = getPrimaryKeySeed().genIntKeyInterval(getIntKeyInterval());
		intKeyBound = intKey+getIntKeyInterval();
	}

	private static int getIntKeyInterval() {
		return intKeyInterval;
	}

	static void setIntKeyInterval(int intKeyInterval) {
		PrimaryKeyGenerator.intKeyInterval = intKeyInterval;
	}

	public static IPrimaryKeySeed getPrimaryKeySeed() {
		return primaryKeySeed;
	}

	public static void setPrimaryKeySeed(IPrimaryKeySeed primaryKeySeed) {
		PrimaryKeyGenerator.primaryKeySeed = primaryKeySeed;
	}
}
