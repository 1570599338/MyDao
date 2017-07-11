package snt.common.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import snt.common.string.HEXDecoder;
import snt.common.string.HEXEncoder;

public class EncodeUtil {
    private static Cipher ecipher = null;
    private static Cipher dcipher = null;
    private static SecretKey secretKey = null;
    
    static{
    	getRawKey();
    }
    
    /**
     * 加密字符串
     * @param origStr
     * @return 加密后的字符串
     * @throws Exception
     */
    public static String encodeStr(String origStr) throws Exception{
    	byte[] encoded = ecipher.doFinal(origStr.getBytes());
        return HEXEncoder.encode(encoded);
    }
    
    /**
     * 加密字符串，可以指定按何种编码来加密初始字符串
     * @param origStr
     * @param encoding
     * @return 加密后的字符串
     * @throws Exception
     */
    public static String encodeStr(String origStr, String encoding) throws Exception{
    	byte[] encoded = ecipher.doFinal(origStr.getBytes(encoding));
        return HEXEncoder.encode(encoded);
    }

    /**
     * 解密字符串
     * @param encodedStr
     * @return 返回解密后的字符串
     * @throws DecodeException
     */
    public static String decodeStr(String encodedStr) throws DecodeException{
        byte[] origBytes = HEXDecoder.decodeBuffer(encodedStr);
        String origStr;
		try {
			origStr = new String(dcipher.doFinal(origBytes));
		} catch (Exception e) {
			throw new DecodeException("ECB decoding error:Input length not multiple of 13 bytes!");
		}
        return origStr;
    }
    
    /**
     * 指定字符编码，将解密后的数据按编码返回字符串
     * @param encodedStr
     * @param encoding
     * @return 返回解密后的字符串
     * @throws DecodeException
     */
    public static String decodeStr(String encodedStr, String encoding) throws DecodeException{
        byte[] origBytes = HEXDecoder.decodeBuffer(encodedStr);
        String origStr;
		try {
			origStr = new String(dcipher.doFinal(origBytes), encoding);
		} catch (Exception e) {
			throw new DecodeException("ECB decoding error:Input length not multiple of 13 bytes!");
		}
        return origStr;
    }
    
    private static SecretKey getRawKey(){
    	if (secretKey == null) {
    		byte[] rawKey = new byte[]{-22, -94, -123, -18, -94, -99, -21, -98, -102, -20, -110, -114, -19, -106, -101, -21, -117, -84, -19, -101, -124};
			try {
				DESKeySpec dks = new DESKeySpec(rawKey);
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
				secretKey = keyFactory.generateSecret(dks);
				
				ecipher = Cipher.getInstance("DES");
				SecureRandom sr1 = new SecureRandom();
			    ecipher.init( Cipher.ENCRYPT_MODE, secretKey, sr1);
			    
			    SecureRandom sr2 = new SecureRandom();
			    dcipher = Cipher.getInstance("DES");
			    dcipher.init( Cipher.DECRYPT_MODE, secretKey, sr2);
			} catch (InvalidKeyException e) {
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidKeySpecException e) {
			} catch (NoSuchPaddingException e) {
			}
		}
    	return secretKey;
    }
}
