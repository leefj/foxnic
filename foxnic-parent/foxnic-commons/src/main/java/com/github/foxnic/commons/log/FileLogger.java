package com.github.foxnic.commons.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;

/**
 * 把某些特定的日志单独输出到文件
 * */
public class FileLogger {
	
	private static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String FORMAT_TIME = "HH:mm:ss.SSS";
	private static final String LEVEL_INFO = "INFO";
	private static final String LEVEL_ERROR = "ERROR";
	private File file = null;
	private BufferedWriter write=null;
	
	private boolean isDisplayTimeOnly=true;
	
	public boolean isDisplayTimeOnly() {
		return isDisplayTimeOnly;
	}

	public void setDisplayTimeOnly(boolean isDisplayTimeOnly) {
		this.isDisplayTimeOnly = isDisplayTimeOnly;
	}

	private boolean isPrintOnScreen=true;
	
	public boolean isPrintOnScreen() {
		return isPrintOnScreen;
	}

	public void setPrintOnScreen(boolean isPrintOnScreen) {
		this.isPrintOnScreen = isPrintOnScreen;
	}
	
	private boolean isInfoEnabled=true;
	
	private boolean isErrorEnabled=true;
	
	public boolean isInfoEnabled() {
		return isInfoEnabled;
	}

	public void setInfoEnabled(boolean isInfoEnabled) {
		this.isInfoEnabled = isInfoEnabled;
	}

	public boolean isErrorEnabled() {
		return isErrorEnabled;
	}

	public void setErrorEnabled(boolean isErrorEnabled) {
		this.isErrorEnabled = isErrorEnabled;
	}
 
	public FileLogger(String file) {
		this(new File(file));
	}
	
	public FileLogger(File file) {
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();          
        }
        try {
			file.createNewFile();
			write = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        //
        this.file=file;
	}
	
	public void info(String location,String message)
	{
		if(!this.isInfoEnabled) return;
		append(LEVEL_INFO,location,message);
	}
	
	public void info(String message)
	{
		if(!this.isInfoEnabled) return;
		append(LEVEL_INFO,null, message);
	}
	
	public void error(String message)
	{
		if(!this.isErrorEnabled) return;
		append(LEVEL_ERROR, null,message);
	}
	
	public void error(String location,String message)
	{
		if(!this.isErrorEnabled) return;
		append(LEVEL_ERROR, location,message);
	}
	
	public void error(String message,Throwable e)
	{
		if(!this.isErrorEnabled) return;
		append(LEVEL_ERROR, null,message+"\n"+StringUtil.toString(e));
	}
	
	public void error(String location,String message,Throwable e)
	{
		if(!this.isErrorEnabled) return;
		append(LEVEL_ERROR, location,message+"\n"+StringUtil.toString(e));
	}
	
	private void append(String level,String location,String message)
	{
		StringBuilder line=new StringBuilder("["+level+"]["+DateUtil.format(new Date(),isDisplayTimeOnly?FORMAT_TIME:FORMAT_FULL)+"]");
		if(!StringUtil.isBlank(location)) {
			 line.append("[@"+location+"]");
		}
	    line.append(" "+message);
		try {
			write.write(line.toString());
			write.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(isPrintOnScreen) {
			System.out.println(line);
		}
	}
	
	
	
}
