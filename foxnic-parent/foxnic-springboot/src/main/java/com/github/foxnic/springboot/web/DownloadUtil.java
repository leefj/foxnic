package com.github.foxnic.springboot.web;

import com.github.foxnic.api.web.MimeUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.springboot.mvc.RequestParameter;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

public class DownloadUtil {
	
	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name)  throws Exception {
		writeToOutput(response, bytes, name,null);
	}

	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name,String contentType)  throws Exception {
		writeToOutput(response,bytes,name,contentType,false);
	}

	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name,String contentType,Boolean inline)  throws Exception {
		if(inline==null) inline= MimeUtil.getFileInline(name);
		response.reset();
		
		OutputStream toClient = response.getOutputStream();

		if(StringUtil.isBlank(contentType)) {
			contentType= MimeUtil.getFileMime(name);
		}
		String desc="attachment";
		if(inline) {
			desc="inline";
		}

		response.setContentType(contentType);
		response.setContentLength(bytes.length);
		response.setHeader("Content-Disposition",
				desc+"; filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));
		toClient.write(bytes);
		toClient.flush();
		toClient.close();
	}
	
	
	public static void writeToOutput(HttpServletResponse response,Workbook workBook,String name)  throws Exception {
		writeToOutput(response, workBook, name, null);
	}
	
	public static void writeToOutput(HttpServletResponse response,Workbook workBook,String name,String contentType)  throws Exception {
		
		response.reset();


 
		if(StringUtil.isBlank(contentType)) {
			contentType=MimeUtil.getFileMime(name);
		}
		response.setContentType(contentType);
 
		response.setHeader("Content-Disposition",
				"attachment; filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));

		OutputStream toClient = response.getOutputStream();

		workBook.write(toClient);
//		try {
//			workBook.close();
//		} catch (Exception e) {}

		RequestParameter requestParameter=RequestParameter.get();
		String tag=requestParameter.getString("downloadTag");
		Cookie cookie=new Cookie("downloadTag",tag);
		response.addCookie(cookie);

		toClient.flush();
		toClient.close();
	}
}
