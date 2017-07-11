/**
 * 
 */
package snt.common.string;

/**
 * 将十六进制表示的字符串转换为字节数组
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class HEXDecoder {
	public static byte[] decodeBuffer(String encodedStr){
		byte[] bytes = new byte[(encodedStr.length()+1)>>1];
		for (int i = 0; i < bytes.length; i++) {
			char c1 = encodedStr.charAt(i<<1);
			char c2 = encodedStr.charAt((i<<1)+1);
			bytes[i]=(byte)(getNum(c1)<<4|getNum(c2));				
		}
		return bytes;
	}
	
	private static int getNum(char c){
		if (c>='0' && c<='9') {
			return c&0xF;
		} else {
			return (c+9)&0xF;
		}
	}
}
