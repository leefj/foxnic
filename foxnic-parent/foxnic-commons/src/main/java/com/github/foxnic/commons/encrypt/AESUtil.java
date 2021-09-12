package com.github.foxnic.commons.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;
 
  
/** 
 * AES加解密
 * Created by 李方捷  202-09-10
 */  
public class AESUtil {  
  
    /** 
     * 密钥算法 
     */  
    private static final String ALGORITHM = "AES";  
    /** 
     * 加解密算法/工作模式/填充方式 
     */  
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";  
  
    /** 
     * SecretKeySpec类是KeySpec接口的实现类,用于构建秘密密钥规范 
     */  
    private SecretKeySpec key;

    private String fit16(String hexKey) {
        hexKey=hexKey.trim();
        if(hexKey.length()>16) {
            hexKey=hexKey.substring(0,16);
        } else if(hexKey.length()<16){
            int i=16-hexKey.length();
            for (int j = 0; j < i; j++) {
                hexKey+="0";
            }
        }
        return hexKey;
    }
  
    public AESUtil(String hexKey) {
        //凑16位
        hexKey=fit16(hexKey);
        key = new SecretKeySpec(hexKey.getBytes(), ALGORITHM);  
    }  
  
    /** 
     * AES加密 
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public String encryptData(String data)  {  
        try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_STR); // 创建密码器  
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化  
			return Base64Util.encodeToString(cipher.doFinal(data.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }  
  
    /** 
     * AES解密 
     * @param base64Data 
     * @return 
     * @throws Exception 
     */  
    public String decryptData(String base64Data) {  
        try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_STR);  
			cipher.init(Cipher.DECRYPT_MODE, key);  
			return new String(cipher.doFinal(Base64Util.decodeToBtyes(base64Data)));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }  
  
    /** 
     * hex字符串 转 byte数组 
     * @param s 
     * @return 
     */  
    private static byte[] hex2byte(String s) {  
        if (s.length() % 2 == 0) {  
            return hex2byte (s.getBytes(), 0, s.length() >> 1);  
        } else {  
            return hex2byte("0"+s);  
        }  
    }  
  
    private static byte[] hex2byte(byte[] b, int offset, int len) {  
        byte[] d = new byte[len];  
        for (int i=0; i<len*2; i++) {  
            int shift = i%2 == 1 ? 0 : 4;  
            d[i>>1] |= Character.digit((char) b[offset+i], 16) << shift;  
        }  
        return d;  
    }
    
    /**
	 * 把字节数组转换成16进制字符串
	 * 
	 * @param bArray
	 * @return
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
 
    private static final String SEED="abcdefghijklmnoABCDEFHpqrstHIJKLMNuvwOPQRSTxyz01234UVWXYZ56789";
    
    public static String getRandomString(int len) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < len; ++i) {
            int number = random.nextInt(SEED.length());
            sb.append(SEED.charAt(number));
        }

        return sb.toString();
    }
  
    public static void main(String[] args) throws Exception {  
    	AESUtil util = new AESUtil("abcdefghijklmnop"); // 密钥  
        System.out.println("cardNo:"+util.encryptData("1234")); // 加密  
        System.out.println("exp:"+util.decryptData("34+Jzs4KkwaCQWVyyAgwLA==")); // 解密  
    }  
} 