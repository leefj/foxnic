package com.github.foxnic.springboot.web;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.api.web.MimeUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.EnumUtil;
import com.github.foxnic.springboot.mvc.RequestParameter;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public  class DownloadUtil {


	public static enum DownloadMode implements CodeTextEnum {
		ATTACHMENT("附件"),
		INLINE("预览");

		private String text;

		private DownloadMode(String text) {
			this.text=text;
		}

		public String code() {
			return this.name().toLowerCase();
		}

		public String text() {
			return this.text;
		}
		public static DownloadMode parseByCode(String code) {
			return (DownloadMode) EnumUtil.parseByCode(DownloadMode.values(),code);
		}
	}

	public static final String DOWNLOAD_TAG_PARAM_NAME = "$download_tag";

	public static void writeToOutput(HttpServletResponse response, byte[] bytes, String name)  throws Exception {
		writeToOutput(response, bytes, name,null);
	}

	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name,String contentType)  throws Exception {
		writeToOutput(response,bytes,name,contentType,DownloadMode.ATTACHMENT);
	}

	public static void writeToOutput(HttpServletResponse response,byte[] bytes,String name,String contentType,DownloadMode mode)  throws Exception {


		response.reset();

		OutputStream toClient = response.getOutputStream();

		if(StringUtil.isBlank(contentType)) {
			contentType= MimeUtil.getFileMime(name);
		}


		response.setContentType(contentType);
		response.setContentLength(bytes.length);
		writeContentDisposition(response,name,mode);
		writeDownloadSuccess(response);
		toClient.write(bytes);
		toClient.flush();
		toClient.close();
	}


	public static void writeContentDisposition(String fileName) throws UnsupportedEncodingException {
		writeContentDisposition(RequestParameter.getResponse(),fileName,DownloadMode.ATTACHMENT);
	}

	public static void writeContentDisposition(HttpServletResponse response,String fileName) throws UnsupportedEncodingException {
		writeContentDisposition(response,fileName,DownloadMode.ATTACHMENT);
	}


	public static void writeContentDisposition(HttpServletResponse response,String fileName,DownloadMode mode) throws UnsupportedEncodingException {
		if (mode == null) {
			if (MimeUtil.getFileInline(fileName)) {
				mode = DownloadMode.INLINE;
			} else {
				mode=DownloadMode.ATTACHMENT;
			}
		}
		response.setHeader("Content-Disposition",
				mode.code()+"; filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
	}


	public static void writeToOutput(HttpServletResponse response,Workbook workBook,String name) throws IOException {
		writeToOutput(response, workBook, name, null);
	}

	public static void writeDownloadResult(Result result) throws IOException {
		writeDownloadResult(RequestParameter.getResponse(),result);
	}

	public static void writeDownloadResult(HttpServletResponse response, Result result) throws IOException {
		response.reset();
		RequestParameter requestParameter=RequestParameter.get();
		String tag=requestParameter.getString("downloadTag");
		response.setHeader("content-type", "text/html;charset=UTF-8");
		response.getWriter().println("<script>top."+tag+"("+ JSONObject.toJSONString(result)+");</script>");
		response.flushBuffer();
	}

	public static void writeDownloadError(Exception e) throws IOException {
		writeDownloadError(RequestParameter.getResponse(),e);
	}

	public static void writeDownloadError(HttpServletResponse response,Exception e) throws IOException {
		Result result= ErrorDesc.exception(e);
		result.message(e.getMessage());
		writeDownloadResult(response,result);
	}

	public static void writeToOutput(HttpServletResponse response,Workbook workBook,String name,String contentType) throws IOException {

		response.reset();

		if(StringUtil.isBlank(contentType)) {
			contentType=MimeUtil.getFileMime(name);
		}
		response.setContentType(contentType);

		response.setHeader("Content-Disposition",
				"attachment; filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));

		writeDownloadSuccess(response);

		OutputStream toClient = response.getOutputStream();
		workBook.write(toClient);
		toClient.flush();
		toClient.close();
	}

	/**
	 * 标记导出成功，需要在输出流写入开始前调用
	 * */
	public static void writeDownloadSuccess() {
		 writeDownloadSuccess(RequestParameter.getResponse());
	}

	/**
	 * 标记导出成功，需要在输出流写入开始前调用
	 * */
	public static void writeDownloadSuccess(HttpServletResponse response) {
		String tag=RequestParameter.getRequest().getParameter(DOWNLOAD_TAG_PARAM_NAME);
		//
		if(StringUtil.hasContent(tag)) {
			response.addHeader("Set-Cookie", tag+"=1; path=/");
		}
	}

}
