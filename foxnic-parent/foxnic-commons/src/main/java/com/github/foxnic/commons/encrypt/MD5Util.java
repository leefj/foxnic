package com.github.foxnic.commons.encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author LeeFangJie
 */
public class MD5Util {

	private MD5Util() {}

	/**
	 * 文本MD5，32
	 * @param input  输入文本
	 * @return 32位MD5字符串
	 * */
	public static String encrypt32(String input) {
//		MessageDigest md5;
//		try {
//			md5 = MessageDigest.getInstance("MD5");
//			byte[] md5Bytes = md5.digest(input.getBytes());
//			StringBuilder hexValue = new StringBuilder();
//			for (int i = 0; i < md5Bytes.length; i++) {
//				int val = ((int) md5Bytes[i]) & 0xff;
//				if (val < 16) {
//					hexValue.append("0");
//				}
//				hexValue.append(Integer.toHexString(val));
//			}
//			input = hexValue.toString();
//			input = input.toUpperCase();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return input;
		
		return DigestUtils.md5Hex(input.getBytes()).toUpperCase();
	}

	/**
	 * 文本MD5，32
	 * @param input  输入文本
	 * @return 32位MD5字符串
	 * */
	public static String encrypt16(String input) {
		return encrypt32(input).substring(8, 24);
	}

	/**
	 * 文本MD5，32
	 * @param stream  输入数据流
	 * @return 32位MD5字符串
	 * */
	public static String encrypt32(InputStream stream) {
		String md5 = null;
		try {
			md5 = DigestUtils.md5Hex(stream).toUpperCase();
		} catch (IOException e) {
			return null;
		}
		return md5;
	}

	/**
	 * 文本MD5，32
	 * @param stream  输入数据流
	 * @return 32位MD5字符串
	 * */
	public static String encrypt16(InputStream stream) {
		String str = encrypt32(stream);
		if (str == null) {
			return null;
		}
		return str.substring(8, 24);
	}

}
