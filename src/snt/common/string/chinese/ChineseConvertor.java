package snt.common.string.chinese;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 用来处理简繁字符互相转换的类.
 * 需要简繁映射文件:/snt/common/string/chinese/tc2sc.table和
 * 				 /snt/common/string/chinese/sc2tc.table
 * 映射文件可以根据具体情况补充映射不正确的码. 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class ChineseConvertor {
	private static ChineseConvertor pInstance = null;	
	private char[] c_tc2sc = null;
	private char[] c_sc2tc = null;
	static{
		//为了提高并发性
		pInstance = getInstance("/snt/common/string/chinese/tc2sc.table", "/snt/common/string/chinese/sc2tc.table");
	}

	/**
	 * 指定简繁映射文件来进行初始化
	 * @param s_tc2scTable
	 * @param s_sc2tcTable
	 * @throws NullPointerException
	 */
	private ChineseConvertor(String s_tc2scTable, String s_sc2tcTable)
			throws NullPointerException {
		if (null == c_tc2sc) {
			c_tc2sc = getCharsFromFile(s_tc2scTable);
		}
		if (null == c_tc2sc) {
			throw new NullPointerException("No traditional chinese to simplified chinese map table can be loaded!");
		}
		if (null == c_sc2tc) {
			c_sc2tc = getCharsFromFile(s_sc2tcTable);
		}
		if (null == c_sc2tc) {
			throw new NullPointerException("No traditional chinese to simplified chinese map table can be loaded!");
		}
	}

	/**
	 * 取得简繁转换器单例（按默认码表）
	 * @return ChineseConvertor
	 */
	public static ChineseConvertor getInstance() {
		return pInstance;
	}

	/**
	 * 按指定码表取得简繁转换器
	 * @param s_tc2scTable
	 * @param s_sc2tcTable
	 * @return ChineseConvertor
	 */
	public static ChineseConvertor getInstance(String s_tc2scTable, String s_sc2tcTable) {
		try {
			return new ChineseConvertor(s_tc2scTable, s_sc2tcTable);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 校正繁体到简体的映射
	 * 一般当发现字符映射不正确的时候可以通过这个方法来校正. 
	 * @param src
	 * @param map
	 * @throws Exception
	 */
	public synchronized void resetTc2ScMap(String src, String map)
			throws Exception {
		if (src == null || map == null) {
			return;
		}
		else if(src.length() != map.length()){
			throw new IllegalArgumentException("The two string's length are not equal.");
		}
		char[] cSrc = src.toCharArray();
		char[] cMap = map.toCharArray();
		for (int i = 0; i < cSrc.length; i++) {
			if(cSrc[i] >= 0x4e00 && cSrc[i] <= 0x9fa5){
				c_tc2sc[cSrc[i]-0x4e00] = cMap[i];
			}
		}

		BufferedWriter pWriter = new BufferedWriter(
				new FileWriter("tc2sc.table"));
		pWriter.write(c_tc2sc, 0, c_tc2sc.length);
		pWriter.close();
	}
	
	/**
	 * 校正简体到繁体的映射
	 * 一般当发现字符映射不正确的时候可以通过这个方法来校正.
	 * @param src
	 * @param map
	 * @throws Exception
	 */
	public synchronized void resetSc2TcMap(String src, String map)
			throws Exception {
		if (src == null || map == null) {
			return;
		}
		else if(src.length() != map.length()){
			throw new IllegalArgumentException("The two string's length are not equal.");
		}
		char[] cSrc = src.toCharArray();
		char[] cMap = map.toCharArray();
		for (int i = 0; i < cSrc.length; i++) {
			if(cSrc[i] >= 0x4e00 && cSrc[i] <= 0x9fa5){
				c_sc2tc[cSrc[i]-0x4e00] = cMap[i];
			}
		}

		BufferedWriter pWriter = new BufferedWriter(
				new FileWriter("sc2tc.table"));
		pWriter.write(c_sc2tc, 0, c_sc2tc.length);
		pWriter.close();
	}
	
	private String convert(String inStr, char[] cTable){
		if (null == inStr || inStr.length() == 0) {
			return inStr;
		}
		
		char[] cs = inStr.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			char c = cs[i];
			if(c >= 0x4e00 && c <= 0x9fa5){
				char c1 = cTable[c-0x4e00];
				if(c1 > 0)
					cs[i] = c1;
			}
		}

		return new String(cs);
	}

	/**
	 * 把简体字符转化成繁体字符
	 * @param c
	 * @return char
	 */
	public char sc2tc(char c){
		if(c >= 0x4e00 && c <= 0x9fa5){
			char c1 = c_sc2tc[c-0x4e00];
			if(c1 > 0)
				return c1;
		}
		return c;
	}
	
	/**
	 * 把简体字符串转化成繁体字符串
	 * @param inStr
	 * @return String
	 */
	public String sc2tc(String inStr){
		return convert(inStr, c_sc2tc);
	}
	
	/**
	 * 把简体字符串转化成繁体字符串
	 * @param inStr
	 * @param off
	 * @param len
	 * @return String
	 */
	public String sc2tc(String inStr, int off, int len){
		return convert(inStr.substring(off, off+len), c_sc2tc);
	}
	
	/**
	 * 把简体字符串转化成繁体字符串
	 * @param cs
	 */
	public void sc2tc(char[] cs){
		sc2tc(cs, 0, cs.length);
	}
	
	/**
	 * 把简体字符串转化成繁体字符串
	 * @param cs
	 * @param off
	 * @param len
	 */
	public void sc2tc(char[] cs, int off, int len){
		int end = off+len;
		for (int i = off; i < end; i++) {
			char c = cs[i];
			if(c >= 0x4e00 && c <= 0x9fa5){
				char c1 = c_sc2tc[c-0x4e00];
				if(c1 > 0)
					cs[i] = c1;
			}
		}
	}

	/**
	 * 把繁体字符转化成简体字符
	 * @param c
	 * @return char
	 */
	public char tc2sc(char c){
		if(c >= 0x4e00 && c <= 0x9fa5){
			char c1 = c_tc2sc[c-0x4e00];
			if(c1 > 0)
				return c1;
		}
		return c;
	}
	
	/**
	 * 把繁体字符串转化成简体字符串
	 * @param inStr
	 * @return String
	 */
	public String tc2sc(String inStr){
		return convert(inStr, c_tc2sc);
	}
	
	/**
	 * 把繁体字符串转化成简体字符串
	 * @param inStr
	 * @param off
	 * @param len
	 * @return String
	 */
	public String tc2sc(String inStr, int off, int len){
		return convert(inStr.substring(off, off+len), c_tc2sc);
	}
	
	/**
	 * 把繁体字符串转化成简体字符串
	 * @param cs
	 */
	public void tc2sc(char[] cs){
		tc2sc(cs, 0, cs.length);
	}
	
	/**
	 * 把繁体字符串转化成简体字符串
	 * @param cs
	 * @param off
	 * @param len
	 */
	public void tc2sc(char[] cs, int off, int len){
		int end = off+len;
		for (int i = off; i < end; i++) {
			char c = cs[i];
			if(c >= 0x4e00 && c <= 0x9fa5){
				char c1 = c_tc2sc[c-0x4e00];
				if(c1 > 0)
					cs[i] = c1;
			}
		}
	}

	private static char[] getCharsFromFile(String inFileName) {
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(ChineseConvertor.class.getResourceAsStream(inFileName)));			
			char[] cContent = new char[0x5a16];//4e00-9fa5 汉字的区间
			reader.read(cContent, 0, cContent.length);
			return cContent;

		} catch (Exception e) {
			return null;
		} finally{
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out
					.println("Usage: snt.common.string.chinese.ChineseConvertor [-gb | -big5] inputstring");
			System.exit(1);
			return;
		}

		boolean bIsGB = true;
		String inStr = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-gb")) {
				bIsGB = true;
			} else if (args[i].equalsIgnoreCase("-big5")) {
				bIsGB = false;
			} else {
				inStr = args[i];
			}
		}

		ChineseConvertor pTmp = ChineseConvertor.getInstance();

		String outStr = "";
		if (bIsGB) {
			outStr = pTmp.tc2sc(inStr);
		} else {
			outStr = pTmp.sc2tc(inStr);
		}

		System.out.println("String [" + inStr + "] converted into:\n[" + outStr
				+ "]");
	}
}
