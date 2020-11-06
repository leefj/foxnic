package com.github.foxnic.commons.encrypt;

import java.util.Base64;

public class Base64Util {
	
	public static byte[] encode(byte[] data) {
		return Base64.getEncoder().encode(data);
	}
	
	public static String encodeToString(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}
	
	public static String encodeToString(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}
	
	
	public static byte[] decode(byte[] data) {
		return Base64.getDecoder().decode(data);
	}
	
	public static String decode(String data) {
		return new String(decode(data.getBytes()));
	}
	
	 
}
