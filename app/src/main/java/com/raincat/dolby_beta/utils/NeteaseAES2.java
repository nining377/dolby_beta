package com.raincat.dolby_beta.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2020/03/24
 *     desc   : EAPI加解密
 *     version: 1.0
 * </pre>
 */
public class NeteaseAES2 {
    private final static byte[] aesKey = "e82ckenh8dichen8".getBytes();

    /**
     * AES 加密算法为：AES-128-ECB，输出格式为：Hex化字符串
     *
     * @param sSrc 参数值
     */
    public static String Encrypt(String sSrc) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// "算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes());
            return byteToHex(encrypted);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @param sSrc 参数值
     * @return
     */
    // 解密
    public static String Decrypt(String sSrc) {
        try {
            byte[] encrypted = hexToByte(sSrc);
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original);
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    /**
     * hex转byte数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexToByte(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte) intVal);
        }
        return ret;
    }

    /**
     * byte数组转hex
     *
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim().toUpperCase();
    }
}
