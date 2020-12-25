package com.github.foxnic.commons.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.commons.log.Logger;

/**
 * @author fangjieli
 */
public class StringUtil {

	/**
	 * 字符串有内容，且为非空白内容
	 * 
	 * @param str 字符串
	 * @return 是否有内容
	 */
	public static boolean hasContent(String str)
	{
		return !isEmpty(str) && !isBlank(str);
	}
	
	/**
	 * 字符串没有内容，空白字符内容也视为无内容
	 * 
	 * @param str 字符串
	 * @return 是否无内容
	 */
	public static boolean noContent(String str)
	{
		return isEmpty(str) || isBlank(str);
	}
	
	/**
	 * 是否为空白
	 * @param str 字符串
	 * @return 是否有内容
	 */
	public static boolean isBlank(String str) {
        final int strLen = str==null ? 0 : str.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
	}

	/**
	 * 值为 null 或长度为0
	 **/
	public static boolean isEmpty(String str) {
		return ((str == null) || (str.length() == 0));
	}

	/**
	 * 左补齐字符或者字符串
	 * 
	 * @param str    原字符串
	 * @param length 补齐后的长度
	 * @param rex    用来补齐的字符串
	 * @return 补齐后的字符串 1.如果str的长度大于length，str将左边多余的删除，留下length个字符
	 *         2.当rex大于一个字符，可能出现补齐后超过了length，将左边多余的删除，留下length个字符
	 */
	public static String leftPad(String str, int length, String rex) {
		if (str == null) {
			return str;
		}
		if (str.length() >= length) {
			return str.substring(str.length() - length);
		}

		return leftPad(rex + str, length, rex);
	}


	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否包含中文汉字和符号
	 * @param str 字符串
	 * @return 是否包含中文汉字和符号
	 */
	public static boolean hasChineseChar(String str) {
		char[] ch = str.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否为空,"null"/"nan"也将被视为空，返回 true
	 * 
	 * @param str 文本内容
	 * @return 是否为空
	 */
	public static boolean isBlank(Object str) {
		if (str == null) {
			return true;
		}
		if ("".equals(String.valueOf(str).trim()) || "null".equalsIgnoreCase(String.valueOf(str).trim())
				|| "nan".equalsIgnoreCase(String.valueOf(str).trim())) {
			return true;
		}
		return false;
	}

	/**
	 * 是否存在空项
	 * 
	 * @param strs 字符串清单
	 * @return 是否存在空项
	 */
	public static boolean isExistsBlank(Object... strs) {
		if (strs == null) {
			return true;
		}
		for (Object str : strs) {
			if (isBlank(str)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 把字符串平均分成n段
	 * @param content 文本内容
	 * @param n 分段数
	 * @return String[]
	 */
	public static String[] sliceAverage(String content, int n) {
		int step = (int) (content.length() / n);
		String[] seg = new String[n];
		int i = 0;
		for (; i < n - 1; i++) {
			seg[i] = content.substring(i * step, (i + 1) * step);
		}
		seg[i] = content.substring(i * step, content.length());
		return seg;
	}

	/**
	 * 默认字符集
	 * */
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	/**
	 * 将queryString转换成Map
	 * @param queryString queryString
	 * @return  Map
	 */
	public static Map queryStringToMap(String queryString) {
		Map map = new HashMap(5);
		map = queryStringToMap(queryString, map);
		return map;
	}

	/**
	 * 将queryString转换,并加入到Map
	 * @param queryString queryString
	 * @param map 传入的用于填充的map对象
	 * @return  Map
	 */
	public static Map queryStringToMap(String queryString, Map map) {
		return queryStringToMap(queryString, map, DEFAULT_CHARSET, true);
	}

	/**
	 * 将queryString转换,并加入到Map
	 * @param queryString queryString
	 * @param map 传入的用于填充的map对象
	 * @param charset 字符集 ，  Charset.forName("UTF-8");
	 * @param cs key是否大小写敏感，false 时 key全部大些
	 * @return  Map
	 */
	public static Map queryStringToMap(String queryString, Map map, Charset charset, boolean cs) {
		if (queryString == null || "".equals(queryString)) {
			return map;
		}
		String[] list = queryString.split("&");
		String[] keyValue = null;
		String key = null;
		for (String entry : list) {
			keyValue = entry.split("=");

			if (keyValue == null) {
				continue;
			}
			if (keyValue.length == 0) {
				continue;
			}
			if (keyValue.length == 1) {
				key = keyValue[0];
				if (!cs) {
					key = key.toUpperCase();
				}
				map.put(key, null);
			} else if (keyValue.length >= 2) {
				key = keyValue[0];
				if (!cs) {
					key = key.toUpperCase();
				}
				try {
					map.put(key, java.net.URLDecoder.decode(keyValue[1], charset.name()));
				} catch (UnsupportedEncodingException e) {
					map.put(key, keyValue[1]);
				}
			}
		}
		return map;
	}

	/**
	 * 移除字符串str中第一个匹配的字符串c
	 * @param str 源文本
	 * @param c	将要移除的内容
	 * @return 处理后的字符串
	 * */
	public static String removeFirst(String str, String c) {

		if (str == null || c == null) {
			return str;
		}
		while (str.startsWith(c)) {
			str = str.substring(c.length());
		}
		return str;
	}

	/**
	 * 移除字符串str中最后一个匹配的字符串c
	 * @param str 源文本
	 * @param c	将要移除的内容
	 * @return 处理后的字符串
	 * */
	public static String removeLast(String str, String c) {
		if (str == null || c == null) {
			return str;
		}
		while (str.endsWith(c)) {
			str = str.substring(0, str.length() - c.length());
		}
		return str;
	}
	
	/**
	 * 移除字符串str中前后一个匹配的字符串c
	 * @param str 源文本
	 * @param c	将要移除的内容
	 * @return 处理后的字符串
	 * */
	public static String trim(String str,String c)
	{
		return removeLast(removeFirst(str, c),c);
	}

	/**
	 * 把各个部分拼接成地址,连接符号按操作系统 斜杠或反斜杠
	 * @param parts 路径的各个部分
	 * @return 拼接后的路径
	 */
	public static String joinPath(String... parts) {
		return joinPathInternal(File.separator, parts);
	}

	/**
	 * 把各个部分拼接成地址 始终使用斜杠
	 * @param parts 路径的各个部分
	 * @return 拼接后的路径
	 */
	public static String joinUrl(String... parts) {
		String url = joinPathInternal("/", parts);
		return url.replace('\\', '/');
	}

	 
	/**
	 * 把数组拼接成字符串，默认用逗号隔开
	 * @param array 数组
	 * @return 拼接后的字符串
	 * */
	public static String join(Object[] array)
	{
		 return ArrayUtil.join(array);
	}
	
	/**
	 * 把数组拼接成字符串
	 * @param array 数组
	 * @param sep 分隔符
	 * @return 拼接后的字符串
	 * */
	public static String join(Object[] array,String sep)
	{
		 return ArrayUtil.join(array,sep);
	}
	
	
	/**
	 * 把数组拼接成字符串，默认用逗号隔开
	 * @param list 元素清单，toString()后再拼接
	 * @return 拼接后的字符串
	 * */
	@SuppressWarnings("rawtypes")
	public static String join(Collection list)
	{
		 return ArrayUtil.join(list.toArray());
	}
	
	/**
	 * 把数组拼接成字符串
	 * @param list 元素清单，toString()后再拼接
	 * @param sep 分隔符
	 * @return 拼接后的字符串
	 * */
	@SuppressWarnings("rawtypes")
	public static String join(Collection list,String sep)
	{
		 return ArrayUtil.join(list.toArray(),sep);
	}

	/**
	 * 把各个部分拼接成地址
	 */
	private static String joinPathInternal(String sep, String... part) {
		String p = null;
		StringBuilder pa = new StringBuilder();
		for (int i = 0; i < part.length; i++) {
			if(StringUtil.isBlank(part[i])) {
				continue;
				//throw new IllegalArgumentException("子路径不允许为空");
			}
			p = part[i] + "";
			if (i == 0) {
				p = p.trim();
				p = StringUtil.removeLast(p, "/");
				p = StringUtil.removeLast(p, "\\");
				pa.append(p);
			} else {
				p = p.trim();
				p = StringUtil.removeLast(p, "/");
				p = StringUtil.removeLast(p, "\\");
				p = StringUtil.removeFirst(p, "/");
				p = StringUtil.removeFirst(p, "\\");
				pa.append(sep + p);
			}
		}
		return pa.toString();
	}

	/**
	 * 异常转字符串
	 * @param e Throwable
	 * @return String
	 * */
	public static String toString(Throwable e) {
		if(e==null) return null;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String content = sw.toString();
		try {
			sw.close();
			pw.close();
		} catch (IOException e1) {
		}
		return content;
	}
	
	/**
	 * Clob 转字符串
	 * @param clob Clob
	 * @return String
	 * */
	public static String toString(Clob clob) {
		String reString = null;
		try {
			Reader is = clob.getCharacterStream();// 得到流
			BufferedReader br = new BufferedReader(is);
			String s = br.readLine();
			StringBuilder sb = new StringBuilder();
			while (s != null) {
				sb.append(s);
				s = br.readLine();
			}
			reString = sb.toString();
			if (br != null) {
				br.close();
			}
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
			 if(clob.getClass().getName().indexOf(".db2.")!=-1) {
				 Logger.error("DB2请确认数据库相关参数是否设置正确，连接字符串相关参数是否配置正确");
			 }
			 Logger.exception(e);
		}
		return reString;
	}
	
	public static String removeLineBreak(String str)
	{
		return removeLineBreak(str," ");
	}
	
	/**
	 * 移除换行符，用指定的字符串替换
	 * */
	public static String removeLineBreak(String str,String replace)
	{
		if(str==null) return str;
		str=str.replace("\n\r", replace);
		str=str.replace("\r\n", replace);
		str=str.replace("\r", replace);
		str=str.replace("\n", replace);
		return str;
	}
	
	/**
	 * 对内容进行URL编码，使用UTF8字符集
	 * */
	public static String encodeAsURL(String url) {
		return encodeAsURL(url,"UTF-8");
	}
	
	/**
	 * 对内容进行URL编码
	 * */
	public static String encodeAsURL(String url,String encoding) {
		try {
			url = URLEncoder.encode(url, encoding);
		} catch (UnsupportedEncodingException e) {
			url = null;
		}
		return url;
	}
	
	
	/**
	 * 对内容进行URL解码，使用UTF8字符集
	 * */
	public static String decodeFromURL(String url) {
		return decodeFromURL(url,"UTF-8");
	}
	
	/**
	 * 对内容进行URL解码
	 * */
	public static String decodeFromURL(String url,String encoding) {
		try {
			url = URLDecoder.decode(url, encoding);
		} catch (UnsupportedEncodingException e) {
			url = null;
		}
		return url;
	}
	
	/**
	 * 拆分内容，并获取指定序号的部分
	 * @param content 被拆分的内容
	 * @param spliter 分隔符
	 * @param index 序号，如果序号越界则返回null，初始部分的序号为 0
	 * @return 返回指定的部分
	 * */
	public static String getPart(String content,String spliter,int index) {
		if(StringUtil.isBlank(content)) return null;
		if(spliter==null) return null;
		//特殊处理
		if(spliter.equals(".")) spliter="\\.";
 		String[] tmp=content.split(spliter);
		if(index<0) return null;
		if(index>=tmp.length) return null;
		return tmp[index];
	}
	
	/**
	 * 拆分内容，并获拆分后的第一部分
	 * @param content 被拆分的内容
	 * @param spliter 分隔符
	 * @return 返回第一部分
	 * */
	public static String getFirstPart(String content,String spliter) {
		 return getPart(content,spliter,0);
	}
	
	
	/**
	 * 拆分内容，并获拆分后的最后部分
	 * @param content 被拆分的内容
	 * @param spliter 分隔符
	 * @param lastIndex 反向的需要，如果序号越界则返回null，最末部分的序号为 0
	 * @return 返回最后部分
	 * */
	public static String getLastPart(String content,String spliter,int lastIndex) {
		if(StringUtil.isBlank(content)) return null;
		if(spliter==null) return null;
		//特殊处理
		if(spliter.equals(".")) spliter="\\.";
		String[] tmp=content.split(spliter);
		int index=tmp.length-1-lastIndex;
		if(index<0) return null;
		if(index>=tmp.length) return null;
		return tmp[index];
	}
	
	/**
	 * 拆分内容，并获拆分后的最后部分
	 * @param content 被拆分的内容
	 * @param spliter 分隔符
	 * @return 返回最后部分
	 * */
	public static String getLastPart(String content,String spliter) {
		if(StringUtil.isBlank(content)) return null;
		if(spliter==null) return null;
		//特殊处理
		if(spliter.equals(".")) spliter="\\.";
		String[] tmp=content.split(spliter);
		return tmp[tmp.length-1];
	}
}
