package com.github.foxnic.commons.code;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

/**
 * @author LeeFangJie
 * 可用于代码构建
 */

public class CodeBuilder {
	
	private String tabstr=null;
	
	public CodeBuilder(String tabstr) {
		this.tabstr=tabstr;
	}
	
	public CodeBuilder() {
		this("\t");
	}
 
	private ArrayList<String> lns =new ArrayList<>();
	
	private static String tab(int i,String tabstr)
	{
		StringBuilder str=new StringBuilder();
		for (int j = 0; j < i; j++) {
			str.append(tabstr);
		}
		return str.toString();
	}
	
	/**
	 * 加入一行代码
	 * @param tabs 行开头的tab个数
	 * @param code 单行代码
	 * */
	public void ln(int tabs,String code)
	{
		lns.add(tab(tabs,this.tabstr)+code);
	}
	
	/**
	 * 加入一行代码
	 * @param code 单行代码
	 * */
	public void ln(String code)
	{
		lns.add(code);
	}
	
	
	
	@Override
	public String toString()
	{
		StringBuilder buf=new StringBuilder();
		for (String string : lns) {
			buf.append(string+"\n");
		}
		return buf.toString();
	}
	
	/**
	 * 写入到文件
	 * @param path 文件的绝对路径
	 * @return 写入是否成功
	 * */
	public boolean wirteToFile(String path)
	{
		return wirteToFile(path,null);
	}
	
	/**
	 * 写入到文件，并替换某些内容
	 * @param path 文件的绝对路径
	 * @param replace 占位符替换配置
	 * @return 写入是否成功
	 * */
	public boolean wirteToFile(String path,Map<String,String> replace)
	{
		return wirteToFile(new File(path),replace);
	}
	
	/**
	 * 写入到文件
	 * @param file 文件
	 * @return 写入是否成功
	 * */
	public boolean wirteToFile(File file)
	{
		return wirteToFile(file,null);
	}
	
	/**
	 * 写入到文件，并替换某些内容
	 * @param file 文件
	 * @param replace 占位符替换配置
	 * @return 写入是否成功
	 * */
	public boolean wirteToFile(File file,Map<String,String> replace)
	{
		file.getParentFile().mkdirs();
		try {
			String code=this.toString();
			if(replace!=null)
			{
				for (Entry<String,String> et: replace.entrySet()) {
					code=code.replaceAll(et.getKey(), replace.get(et.getKey()));
				}
			}
			FileUtils.write(file, code,"UTF-8");
//			FileUtil.writeString(file, code);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void clear() {
		lns.clear();
	}

	public void append(CodeBuilder code) {
		this.lns.addAll(code.lns);
	}
	
	
	
}
