package com.github.foxnic.api.web;

import java.util.HashMap;

/**
 * 
 * @author lifangjie2
 * */
public class MimeUtil {
	
	private static final HashMap<String, String> MIME_MAP = new HashMap<String, String>();

	static {
		
		MIME_MAP.put("evy","application/envoy");
		MIME_MAP.put("fif","application/fractals");
		MIME_MAP.put("spl","application/futuresplash");
		MIME_MAP.put("hta","application/hta");
		MIME_MAP.put("acx","application/internet-property-stream");
		MIME_MAP.put("hqx","application/mac-binhex40");
		MIME_MAP.put("doc","application/msword");
		MIME_MAP.put("dot","application/msword");
		MIME_MAP.put("*","application/octet-stream");
		MIME_MAP.put("bin","application/octet-stream");
		MIME_MAP.put("class","application/octet-stream");
		MIME_MAP.put("dms","application/octet-stream");
		MIME_MAP.put("exe","application/octet-stream");
		MIME_MAP.put("lha","application/octet-stream");
		MIME_MAP.put("lzh","application/octet-stream");
		MIME_MAP.put("oda","application/oda");
		MIME_MAP.put("axs","application/olescript");
		MIME_MAP.put("pdf","application/pdf");
		MIME_MAP.put("prf","application/pics-rules");
		MIME_MAP.put("p10","application/pkcs10");
		MIME_MAP.put("crl","application/pkix-crl");
		MIME_MAP.put("ai","application/postscript");
		MIME_MAP.put("eps","application/postscript");
		MIME_MAP.put("ps","application/postscript");
		MIME_MAP.put("rtf","application/rtf");
		MIME_MAP.put("setpay","application/set-payment-initiation");
		MIME_MAP.put("setreg","application/set-registration-initiation");
		MIME_MAP.put("xla","application/vnd.ms-excel");
		MIME_MAP.put("xlc","application/vnd.ms-excel");
		MIME_MAP.put("xlm","application/vnd.ms-excel");
		MIME_MAP.put("xls","application/vnd.ms-excel");
		MIME_MAP.put("xlt","application/vnd.ms-excel");
		MIME_MAP.put("xlw","application/vnd.ms-excel");
		MIME_MAP.put("msg","application/vnd.ms-outlook");
		MIME_MAP.put("sst","application/vnd.ms-pkicertstore");
		MIME_MAP.put("cat","application/vnd.ms-pkiseccat");
		MIME_MAP.put("stl","application/vnd.ms-pkistl");
		MIME_MAP.put("pot","application/vnd.ms-powerpoint");
		MIME_MAP.put("pps","application/vnd.ms-powerpoint");
		MIME_MAP.put("ppt","application/vnd.ms-powerpoint");
		MIME_MAP.put("mpp","application/vnd.ms-project");
		MIME_MAP.put("wcm","application/vnd.ms-works");
		MIME_MAP.put("wdb","application/vnd.ms-works");
		MIME_MAP.put("wks","application/vnd.ms-works");
		MIME_MAP.put("wps","application/vnd.ms-works");
		MIME_MAP.put("hlp","application/winhlp");
		MIME_MAP.put("bcpio","application/x-bcpio");
		MIME_MAP.put("cdf","application/x-cdf");
		MIME_MAP.put("z","application/x-compress");
		MIME_MAP.put("tgz","application/x-compressed");
		MIME_MAP.put("cpio","application/x-cpio");
		MIME_MAP.put("csh","application/x-csh");
		MIME_MAP.put("dcr","application/x-director");
		MIME_MAP.put("dir","application/x-director");
		MIME_MAP.put("dxr","application/x-director");
		MIME_MAP.put("dvi","application/x-dvi");
		MIME_MAP.put("gtar","application/x-gtar");
		MIME_MAP.put("gz","application/x-gzip");
		MIME_MAP.put("hdf","application/x-hdf");
		MIME_MAP.put("ins","application/x-internet-signup");
		MIME_MAP.put("isp","application/x-internet-signup");
		MIME_MAP.put("iii","application/x-iphone");
		MIME_MAP.put("js","application/x-javascript");
		MIME_MAP.put("latex","application/x-latex");
		MIME_MAP.put("mdb","application/x-msaccess");
		MIME_MAP.put("crd","application/x-mscardfile");
		MIME_MAP.put("clp","application/x-msclip");
		MIME_MAP.put("dll","application/x-msdownload");
		MIME_MAP.put("m13","application/x-msmediaview");
		MIME_MAP.put("m14","application/x-msmediaview");
		MIME_MAP.put("mvb","application/x-msmediaview");
		MIME_MAP.put("wmf","application/x-msmetafile");
		MIME_MAP.put("mny","application/x-msmoney");
		MIME_MAP.put("pub","application/x-mspublisher");
		MIME_MAP.put("scd","application/x-msschedule");
		MIME_MAP.put("trm","application/x-msterminal");
		MIME_MAP.put("wri","application/x-mswrite");
		MIME_MAP.put("cdf","application/x-netcdf");
		MIME_MAP.put("nc","application/x-netcdf");
		MIME_MAP.put("pma","application/x-perfmon");
		MIME_MAP.put("pmc","application/x-perfmon");
		MIME_MAP.put("pml","application/x-perfmon");
		MIME_MAP.put("pmr","application/x-perfmon");
		MIME_MAP.put("pmw","application/x-perfmon");
		MIME_MAP.put("p12","application/x-pkcs12");
		MIME_MAP.put("pfx","application/x-pkcs12");
		MIME_MAP.put("p7b","application/x-pkcs7-certificates");
		MIME_MAP.put("spc","application/x-pkcs7-certificates");
		MIME_MAP.put("p7r","application/x-pkcs7-certreqresp");
		MIME_MAP.put("p7c","application/x-pkcs7-mime");
		MIME_MAP.put("p7m","application/x-pkcs7-mime");
		MIME_MAP.put("p7s","application/x-pkcs7-signature");
		MIME_MAP.put("sh","application/x-sh");
		MIME_MAP.put("shar","application/x-shar");
		MIME_MAP.put("swf","application/x-shockwave-flash");
		MIME_MAP.put("sit","application/x-stuffit");
		MIME_MAP.put("sv4cpio","application/x-sv4cpio");
		MIME_MAP.put("sv4crc","application/x-sv4crc");
		MIME_MAP.put("tar","application/x-tar");
		MIME_MAP.put("tcl","application/x-tcl");
		MIME_MAP.put("tex","application/x-tex");
		MIME_MAP.put("texi","application/x-texinfo");
		MIME_MAP.put("texinfo","application/x-texinfo");
		MIME_MAP.put("roff","application/x-troff");
		MIME_MAP.put("t","application/x-troff");
		MIME_MAP.put("tr","application/x-troff");
		MIME_MAP.put("man","application/x-troff-man");
		MIME_MAP.put("me","application/x-troff-me");
		MIME_MAP.put("ms","application/x-troff-ms");
		MIME_MAP.put("ustar","application/x-ustar");
		MIME_MAP.put("src","application/x-wais-source");
		MIME_MAP.put("cer","application/x-x509-ca-cert");
		MIME_MAP.put("crt","application/x-x509-ca-cert");
		MIME_MAP.put("der","application/x-x509-ca-cert");
		MIME_MAP.put("pko","application/ynd.ms-pkipko");
		MIME_MAP.put("zip","application/zip");
		MIME_MAP.put("au","audio/basic");
		MIME_MAP.put("snd","audio/basic");
		MIME_MAP.put("mid","audio/mid");
		MIME_MAP.put("rmi","audio/mid");
		MIME_MAP.put("mp3","audio/mpeg");
		MIME_MAP.put("aif","audio/x-aiff");
		MIME_MAP.put("aifc","audio/x-aiff");
		MIME_MAP.put("aiff","audio/x-aiff");
		MIME_MAP.put("m3u","audio/x-mpegurl");
		MIME_MAP.put("ra","audio/x-pn-realaudio");
		MIME_MAP.put("ram","audio/x-pn-realaudio");
		MIME_MAP.put("wav","audio/x-wav");
		MIME_MAP.put("bmp","image/bmp");
		MIME_MAP.put("cod","image/cis-cod");
		MIME_MAP.put("ief","image/ief");
		MIME_MAP.put("jpe","image/jpeg");
		MIME_MAP.put("jfif","image/pipeg");
		MIME_MAP.put("svg","image/svg+xml");
		MIME_MAP.put("tif","image/tiff");
		MIME_MAP.put("tiff","image/tiff");
		MIME_MAP.put("ras","image/x-cmu-raster");
		MIME_MAP.put("cmx","image/x-cmx");
		MIME_MAP.put("ico","image/x-icon");
		MIME_MAP.put("pnm","image/x-portable-anymap");
		MIME_MAP.put("pbm","image/x-portable-bitmap");
		MIME_MAP.put("pgm","image/x-portable-graymap");
		MIME_MAP.put("ppm","image/x-portable-pixmap");
		MIME_MAP.put("rgb","image/x-rgb");
		MIME_MAP.put("xbm","image/x-xbitmap");
		MIME_MAP.put("xpm","image/x-xpixmap");
		MIME_MAP.put("xwd","image/x-xwindowdump");
		MIME_MAP.put("mht","message/rfc822");
		MIME_MAP.put("mhtml","message/rfc822");
		MIME_MAP.put("nws","message/rfc822");
		MIME_MAP.put("css","text/css");
		MIME_MAP.put("323","text/h323");
		MIME_MAP.put("stm","text/html");
		MIME_MAP.put("uls","text/iuls");
		MIME_MAP.put("bas","text/plain");
		MIME_MAP.put("c","text/plain");
		MIME_MAP.put("h","text/plain");
		MIME_MAP.put("txt","text/plain");
		MIME_MAP.put("rtx","text/richtext");
		MIME_MAP.put("sct","text/scriptlet");
		MIME_MAP.put("tsv","text/tab-separated-values");
		MIME_MAP.put("htt","text/webviewhtml");
		MIME_MAP.put("htc","text/x-component");
		MIME_MAP.put("etx","text/x-setext");
		MIME_MAP.put("vcf","text/x-vcard");
		MIME_MAP.put("mp2","video/mpeg");
		MIME_MAP.put("mpa","video/mpeg");
		MIME_MAP.put("mpe","video/mpeg");
		MIME_MAP.put("mpeg","video/mpeg");
		MIME_MAP.put("mpg","video/mpeg");
		MIME_MAP.put("mpv2","video/mpeg");
		MIME_MAP.put("mov","video/quicktime");
		MIME_MAP.put("qt","video/quicktime");
		MIME_MAP.put("lsf","video/x-la-asf");
		MIME_MAP.put("lsx","video/x-la-asf");
		MIME_MAP.put("asf","video/x-ms-asf");
		MIME_MAP.put("asr","video/x-ms-asf");
		MIME_MAP.put("asx","video/x-ms-asf");
		MIME_MAP.put("avi","video/x-msvideo");
		MIME_MAP.put("movie","video/x-sgi-movie");
		MIME_MAP.put("flr","x-world/x-vrml");
		MIME_MAP.put("vrml","x-world/x-vrml");
		MIME_MAP.put("wrl","x-world/x-vrml");
		MIME_MAP.put("wrz","x-world/x-vrml");
		MIME_MAP.put("xaf","x-world/x-vrml");
		MIME_MAP.put("xof","x-world/x-vrml");

		
		//
		MIME_MAP.put("doc", "application/msword");
		MIME_MAP.put("xls", "application/vnd.ms-excel");
		MIME_MAP.put("docx", "application/msword");
		MIME_MAP.put("xlsx", "application/vnd.ms-excel");
		MIME_MAP.put("pdf", "application/pdf");
		MIME_MAP.put("ppt", "application/ppt");
		MIME_MAP.put("txt", "text/plain");
		MIME_MAP.put("jpg", "image/jpg");
		MIME_MAP.put("jpeg", "image/jpeg");
		MIME_MAP.put("gif", "image/gif");
		MIME_MAP.put("png", "image/png");
		MIME_MAP.put("html", "text/html");
		MIME_MAP.put("htm", "text/html");

	}

	/**
	 * 获取文件类型对应的MIME类型。
	 * 
	 * @param filename
	 * @return
	 */
	public static String getFileMime(String filename) {
		return MIME_MAP.get(getFileType(filename));
	}

	/**
	 * 获取文件类型对应的MIME类型。
	 *
	 * @param filename
	 * @return
	 */
	public static boolean getFileInline(String filename) {
		String mine=MIME_MAP.get(getFileType(filename));
		if(mine==null) return false;
		String[] ts=mine.split("/");
		return "image".equalsIgnoreCase(ts[0]) || "text".equalsIgnoreCase(ts[0]);
	}


	/**
	 * 获取小写的文件后缀名。
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileType(String path) {
		if(path==null) return "";
		path=path.trim();
		if(path.length()==0) return "";

		int i = path.lastIndexOf(".");
		// 不存在“.”，或“.”在最前或最后，作为无文件后缀名处理。
		if (0 >= i || (path.length() - 1 == i)) {
			return "";
		}

		return path.substring(i + 1, path.length()).toLowerCase();
	}
	
}
