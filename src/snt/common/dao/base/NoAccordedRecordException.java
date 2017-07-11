/**
 * 
 */
package snt.common.dao.base;

import snt.common.business.BaseBusinessException;

/**
 * 没有相应记录更新而抛出的异常，通常用于扣费处理等对并发性能和数据一致性要求较高的场合。
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class NoAccordedRecordException extends BaseBusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int updatedCount = -1;

	/**
	 * @param updatedCount
	 * @param message
	 */
	public NoAccordedRecordException(int updatedCount, String message) {
		super(message);
		this.updatedCount = updatedCount;
	}
	
	public int getUpdatedCount(){
		return updatedCount;
	}
}
