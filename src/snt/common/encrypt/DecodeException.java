/**
 * 
 */
package snt.common.encrypt;

/**
 * 解密错误异常
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class DecodeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DecodeException() {
		super();
	}

	/**
	 * @param message
	 */
	public DecodeException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DecodeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DecodeException(Throwable cause) {
		super(cause);
	}

}
