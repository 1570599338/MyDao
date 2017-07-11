/**
 * 
 */
package snt.common.business;

/**
 * 业务异常基类
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public abstract class BaseBusinessException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public BaseBusinessException() {
		super();
	}

	/**
	 * @param message
	 */
	public BaseBusinessException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BaseBusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public BaseBusinessException(Throwable cause) {
		super(cause);
	}
}
