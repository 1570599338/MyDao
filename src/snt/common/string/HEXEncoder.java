/**
 * 
 */
package snt.common.string;

/**
 * 将字节数组转换为十六进制表示的字符串
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class HEXEncoder {
	private final static char[] hexDigits = { '0', '1', '2', '3', '4', '5',
		'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	public static String encode(byte[] bytes){
		char[] charValues = new char[bytes.length<<1];
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			charValues[i<<1]=hexDigits[(b>>4)&0xF];
			charValues[(i<<1)+1]=hexDigits[b&0xF];
		}
		return new String(charValues);
	}
}
