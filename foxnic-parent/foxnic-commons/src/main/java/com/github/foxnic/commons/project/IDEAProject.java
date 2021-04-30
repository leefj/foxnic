package com.github.foxnic.commons.project;

 
import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
 

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

/**
 * @author fangjieli 
 * 目前局部支持Eclipase项目结构
 * */
public class IDEAProject extends Project {
 
	private File classPathFile=null;
 
	public IDEAProject() {
		Class clz=ReflectUtil.forName((new Throwable()).getStackTrace()[1].getClassName(), true);
		init(clz);
	}
	
	public IDEAProject(Class clz) {
		this.init(clz);
	}
	
	private void init(Class clz) {
		super.init(clz,".classpath");
		this.classPathFile=this.getIdentityFile();	
	}
 
}
