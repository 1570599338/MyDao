/**
 * 
 */
package snt.common.dao.base;

import snt.common.business.BaseBusinessException;

/**
 * 自动装配异常
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class AutoAssembleException extends BaseBusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public AutoAssembleException() {
		super();
	}

	/**
	 * @param message
	 */
	public AutoAssembleException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AutoAssembleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public AutoAssembleException(Throwable cause) {
		super(cause);
	}

}
