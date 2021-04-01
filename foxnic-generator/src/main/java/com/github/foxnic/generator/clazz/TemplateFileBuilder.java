package com.github.foxnic.generator.clazz;

import java.io.File;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.Context;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

public abstract class TemplateFileBuilder {
	
	protected Context ctx;
	
	protected Engine engine=null;
	
	protected String code;
	
	private Kv vars = new Kv();
	
	protected Template template;
 
	public TemplateFileBuilder(Context cfg) {
		this.ctx=cfg;
		//初始化模版引擎
		Engine.setFastMode(true);
		engine=new Engine();
		engine.setDevMode(true);
		engine.setToClassPathSourceFactory();
	}
	
	public void putVar(String name,String value) {
		vars.put(name, value);
	}
	
	public void putVar(String name,Object value) {
		vars.put(name, value);
	}
	
	public void putVar(String name,CodeBuilder value) {
		String str=value.toString();
		str=StringUtil.removeLast(str, "\n");
		str=StringUtil.removeLast(str, "\r");
		vars.put(name, str);
	}
	
 
	protected abstract void build();
	
	public abstract void buildAndUpdate();
	
	private File sourceFile=null;
 
	
	protected void buildAndUpdate(File file) {
		this.sourceFile=file;
		this.build();
		code = template.renderToString(vars);
		this.saveFile();
	}

	 

	private void saveFile() {
		
		//不同类型的代码处理覆盖逻辑
		File file=processOverride(sourceFile);
		//如果返回null则不再生成代码
		if(file==null) {
			return;
		}
	 
		FileUtil.writeText(file, this.code);
		
	}

	/**
	 * 判断将要生成的文件是否存在，如果要写入其它文件，则返回其它文件对象
	 * */
	protected abstract File processOverride(File sourceFile);
 
	
	public File getSourceFile() {
		return sourceFile;
	}
	
	

}
