package com.github.foxnic.commons.lang;

/**
 * @author LeeFangJie
 */
public enum YNBoolean {
	/**
	 * True
	 */
	TRUE(true),
	/**
	 * False
	 */
	FALSE(false),
	/**
	 * null
	 */
	NULL(null);

	Boolean value;

	YNBoolean(Boolean b) {
		this.value = b;
	}
	
	private static final String Y1="Y";
	private static final String Y2="YES";
	
	private static final String N1="N";
	private static final String N2="NO";

	public static YNBoolean parse(String s) {
		if (s == null) {
			return YNBoolean.NULL;
		}
		s=s.trim();
		if (s.equalsIgnoreCase(Y1) || s.equalsIgnoreCase(Y2)) {
			return YNBoolean.TRUE;
		} else if (s.equalsIgnoreCase(N1) || s.equalsIgnoreCase(N2)) {
			return YNBoolean.FALSE;
		} else {
			return YNBoolean.NULL;
		}

	}

	public Boolean getValue() {
		return value;
	}

	public static String toText(boolean b) {
		return b ? "Y" : "N";
	}
}
