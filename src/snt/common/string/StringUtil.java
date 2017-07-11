/*
 * 创建日期 2005-7-20
 *
 * 
 */
package snt.common.string;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 字符串工具
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 *
 */
public class StringUtil {
	/**
	 * 之所以提供这个方法，是为了和String.split区分开。
	 * 这里没有采用正则表达式，而是直接基于设定字符的分割
	 * @param sourceStr
	 * @param delim
	 * @return String[]
	 */
	public static String[] split(String sourceStr, String delim) {
		StringTokenizer st = new StringTokenizer(sourceStr, delim);
		List subStrList = new ArrayList();
		while (st.hasMoreTokens()) {
			String element = st.nextToken();
			subStrList.add(element);
		}
		return (String[]) subStrList.toArray(new String[subStrList.size()]);
	}

	/** 
	 * 转换字节数组为16进制字串 
	 * @param b 字节数组 
	 * @return 16进制字串 
	 */

	public static String byteArrayToHexString(byte[] b) {
		return HEXEncoder.encode(b);
	}
	
	public static String join(String seperator, String[] strings) {
		int length = strings.length;
		if ( length == 0 ) return "";
		StringBuffer buf = new StringBuffer( length * strings[0].length() )
				.append( strings[0] );
		for ( int i = 1; i < length; i++ ) {
			buf.append( seperator ).append( strings[i] );
		}
		return buf.toString();
	}
	
	public static String replaceOnce(String template, String placeholder, String replacement) {
		int loc = template.indexOf( placeholder );
		if ( loc < 0 ) {
			return template;
		}
		else {
			return new StringBuffer( template.substring( 0, loc ) )
					.append( replacement )
					.append( template.substring( loc + placeholder.length() ) )
					.toString();
		}
	}

	public static String MD5Encode(String origin) {
		String resultString = null;

		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString
					.getBytes()));
		} catch (Exception ex) {
		}
		return resultString;
	}
}
