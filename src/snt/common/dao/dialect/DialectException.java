/**
 * 
 */
package snt.common.dao.dialect;

/**
 * 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class DialectException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DialectException() {
		super();
	}

	/**
	 * @param message
	 */
	public DialectException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DialectException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DialectException(Throwable cause) {
		super(cause);
	}
}
