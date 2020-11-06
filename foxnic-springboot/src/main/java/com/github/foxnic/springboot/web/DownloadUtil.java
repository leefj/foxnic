package com.github.foxnic.springboot.web;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.github.foxnic.commons.lang.StringUtil;

public class DownloadUtil {
	
	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name)  throws Exception {
		writeToOutput(response, bytes, name,null);
	}

	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name,String contentType)  throws Exception {
		
		response.reset();
		
		OutputStream toClient = response.getOutputStream();

		if(StringUtil.isBlank(contentType)) {
			contentType=MimeUtil.getFileMime(name);
		}
		response.setContentType(contentType);
		response.setContentLength(bytes.length);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));
		toClient.write(bytes);
		toClient.flush();
		toClient.close();
	}
	
	
//	public static void writeToOutput(HttpServletResponse response,Workbook workBook,String name,String contentType)  throws Exception {
//		
//		response.reset();
// 
//		if(StringUtil.isBlank(contentType)) {
//			contentType=MimeUtil.getFileMime(name);
//		}
//		response.setContentType(contentType);
// 
//		response.setHeader("Content-Disposition",
//				"attachment; filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));
//		
//		OutputStream toClient = response.getOutputStream();
//		workBook.write(toClient);
//		
//		toClient.flush();
//		toClient.close();
//	}
}
