package com.github.foxnic.commons.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

public class Command {

	
	private boolean printResultLine=true;
	
	private ResultLineHandler resultLineHandler=null;
	
	private String encoding=null;
	
	
	/**
	 * 执行本机命令行
	 * 
	 * @param command 命令
	 * @return 命令输出
	 */
	public String[] exec(String command) {
		return exec(command,null);
	}
	
	 

	/**
	 * 执行本机命令行
	 * 
	 * @param command 命令
	 * @return 命令输出
	 */
	public String[] exec(String command,File workDir) {

		if(StringUtil.isBlank(encoding)) {
			if(OSType.isWindows()) {
				encoding="GBK";
			} else {
				encoding="UTF-8";
			}
		}
		
		BufferedReader br = null;
		Process p=null;
		try {
			p = Runtime.getRuntime().exec(command, null, workDir);
			br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName(encoding)));
			ArrayList<String> result=new ArrayList<String>();
			String line = null;
			while ((line = br.readLine()) != null) {
				if(this.printResultLine) System.out.println(line);
				result.add(line+"\n");
				if(resultLineHandler!=null) {
					resultLineHandler.onResultLine(line);
				}
			}
			return result.toArray(new String[] {});
		} catch (Exception e) {
			Logger.error("command error : "+command,e);
			return null;
		} finally {
			if(p!=null) {
				p.destroy();
			}
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public boolean isPrintResultLine() {
		return printResultLine;
	}

	/**
	 * 是否打印命令输出
	 * */
	public void setPrintResultLine(boolean printLine) {
		this.printResultLine = printLine;
	}
	
	public static interface ResultLineHandler {
		void onResultLine(String line);
	}

	public ResultLineHandler getResultLineHandler() {
		return resultLineHandler;
	}

	public void setResultLineHandler(ResultLineHandler resultLineHandler) {
		this.resultLineHandler = resultLineHandler;
	}
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}


