package com.github.foxnic.commons.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

public class FileUtil {
	
	/**
	 * 获得文件
	 * */
	public static File resolve(String basicPath,String... part) {
		part=ArrayUtil.unshift(part, basicPath);
		File file=new File(StringUtil.joinPath(part));
		if(file.getParentFile().exists()) {
			file.getParentFile().mkdirs(); 
		}
		return file;
	}
	
	public static void writeText(File f,CharSequence text,String encoding) {
		try {
			FileUtils.write(f, text, encoding);
		} catch (IOException e) {
			Logger.error("write file error, file exists="+f.exists()+" , "+f.getAbsolutePath());
		}
	}
	
	public static void writeText(File f,CharSequence text) {
		writeText(f, text, "UTF-8");
	}
	
	public static String readText(File f,String encoding) {
		try {
			return FileUtils.readFileToString(f,encoding);
		} catch (IOException e) {
			Logger.error("read file error, file exists="+f.exists()+" , "+f.getAbsolutePath());
			return null;
		}
	}
	
	public static String readText(File f) {
		return readText(f,"UTF-8");
	}
	
	public static JSONObject readJSONobject(File f) {
		return JSONObject.parseObject(readText(f));
	}
	
	public static JSONObject readJSONobject(File f,String encoding) {
		return JSONObject.parseObject(readText(f,encoding));
	}
}
