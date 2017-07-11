/**
 * 
 */
package snt.common.web.util;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 因为JDK提供的WeakHashMap回收垃圾的速度之快出乎我的意料，所以，没有办法<br/>
 * 我只好勉为其难写一个自己的可以回收垃圾的数据结构
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * Create Time:2006-7-5 
 */
public class WeakDataMap<K, V> implements Serializable{
	public static class Entity<V> implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**最后一次访问时间*/
		private long lastAccessTime = 0L;
		/**值*/
		private V value;
		
		public Entity(V value) {
			this.value = value;
			lastAccessTime = System.currentTimeMillis();
		}
		
		public long getLastAccessTime(){
			return lastAccessTime;
		}
		
		public V getValue(){
			lastAccessTime = System.currentTimeMillis();
			return value;
		}
	}
	
	public final static long DEFAULT_EXPIRE_PERIOD = 300000L;

    private static Log log = LogFactory.getLog(WeakDataMap.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	/**
     * Value representing null keys inside tables.
     */
    static final Object NULL_KEY = new Object();

	/**
     * Returns internal representation for key. Use NULL_KEY if key is null.
     */
    @SuppressWarnings("unchecked")
	static <T> T maskNull(T key) {
        return key == null ? (T)NULL_KEY : key;
    }
	/**
     * Returns key represented by specified internal representation.
     */
    static <T> T unmaskNull(T key) {
        return (key == NULL_KEY ? null : key);
    }
    
	/**真正存放数据的Map*/
	private ConcurrentMap<K, Entity<V>> concurrentMap = new ConcurrentHashMap<K, Entity<V>>();
	/**数据超时时间*/
	private long expirePeriod = DEFAULT_EXPIRE_PERIOD;
	/**会话ID*/
	private String sessionID = null;
	
	public WeakDataMap(String sessionID) {
		this(sessionID, DEFAULT_EXPIRE_PERIOD);
	}
	
	public WeakDataMap(String sessionID, long expirePeriod) {
		this.sessionID = sessionID;
		this.expirePeriod = expirePeriod;
	}
	
	public void collectGarbage(){
		long curTime = System.currentTimeMillis();
		for (K key : concurrentMap.keySet()) {
			Entity<V> entity = concurrentMap.get(key);
			if (entity == null ||
				entity.getLastAccessTime() <= curTime-expirePeriod) {
				concurrentMap.remove(key);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("会话弱引用数据自清理完成！会话ID："+sessionID);
		}
	}
	
	public boolean containsKey(Object key) {
		key = maskNull(key);
		return concurrentMap.containsKey(key);
	}
	
	public V get(Object key){
		key = maskNull(key);
		Entity<V> entity = concurrentMap.get(key);
		return entity == null?null:entity.getValue();
	}

	public boolean isEmpty() {
		return concurrentMap.isEmpty();
	}

	public Set<K> keySet() {
		return concurrentMap.keySet();
	}

	public V put(K key, V value){
		key = maskNull(key);
		Entity<V> oldEntity = concurrentMap.put(key, new Entity<V>(value));
		return oldEntity == null?null:oldEntity.getValue();
	}

	public V remove(Object key) {
		key = maskNull(key);
		Entity<V> oldEntity = concurrentMap.remove(key);
		return oldEntity == null?null:oldEntity.getValue();
	}

	public int size() {
		return concurrentMap.size();
	}
}
