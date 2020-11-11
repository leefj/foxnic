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
public class EclipseProject extends Project {
 
	private File classPathFile=null;
 
	public EclipseProject() {
		Class clz=ReflectUtil.forName((new Throwable()).getStackTrace()[1].getClassName(), true);
		init(clz);
	}
	
	public EclipseProject(Class clz) {
		this.init(clz);
	}
	
	private void init(Class clz) {
		super.init(clz,".classpath");
		this.classPathFile=this.getIdentityFile();	
	}
	
 
	/**
	 * 读取 .classpath
	 * */
	private void readEclipseClassPathFile(File file) {
		if(file==null) {
			return;
		}
		SAXReader reader = new SAXReader(); 
		Document doc = null;
		try {
			doc = reader.read(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Element root = doc.getRootElement();   
		Element foo; 
		List<Element> childElements = root.elements();
		//循环输出全部book的相关信息
		for (Element child : childElements) {
//          List<Element> books = child.elements();
			String kind=child.attribute("kind").getText();
			String path=child.attribute("path").getText();
			//获取当前元素名
            String name = child.getName();
            //获取当前元素值
            String text = child.getText();
            System.out.println(name + ":" + kind);
           
        }
 
	}
 
	
}
