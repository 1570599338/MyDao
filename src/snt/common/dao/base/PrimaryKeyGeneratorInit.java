/**
 * 
 */
package snt.common.dao.base;

/**
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 * 2006-8-31
 */
public class PrimaryKeyGeneratorInit {
	private IPrimaryKeySeed primaryKeySeed;

	/**
	 * 一次获取的主键数
	 */
	private int keyInterval = 200;

	public IPrimaryKeySeed getPrimaryKeySeed() {
		return primaryKeySeed;
	}

	public void setPrimaryKeySeed(IPrimaryKeySeed primaryKeySeed) {
		this.primaryKeySeed = primaryKeySeed;
		PrimaryKeyGenerator.setPrimaryKeySeed(primaryKeySeed);
	}	

	public int getKeyInterval() {
		return keyInterval;
	}

	public void setKeyInterval(int keyInterval) {
		this.keyInterval = keyInterval;
		PrimaryKeyGenerator.setIntKeyInterval(keyInterval);
	}
}
