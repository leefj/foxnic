package com.github.foxnic.generatorV2.builder.business;

import java.io.File;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.code.JavaClassFile;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generatorV2.config.MduCtx;
import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

import net.bytebuddy.asm.Advice.This;

public class TemplateJavaFile extends JavaClassFile {

	protected MduCtx context=null;
	//
	private static Engine engine=null;
	//
	protected Template template;
	private Kv vars = new Kv();
	//
	private String templateFilePath;
	
	public TemplateJavaFile(MduCtx context,MavenProject project, String packageName, String simpleName,String templateFilePath) {
		super(project, packageName, simpleName);
		this.context=context;
		this.templateFilePath=templateFilePath;
		if(engine==null) {
			Engine.setFastMode(true);
			engine=new Engine();
			engine.setDevMode(true);
			engine.setToClassPathSourceFactory();
		}
		template = engine.getTemplate(templateFilePath);
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
 
	
	public void save() {
		
		this.putVar("package", this.getPackageName());
		this.putVar("simpleName", this.getSimpleName());
		
		this.buildBody();
		
		this.putVar("poVar", this.context.getPoClassFile().getVar());
		this.putVar("poListVar", this.context.getPoClassFile().getVar()+"List");
		
		this.putVar("imports",  StringUtil.join(this.imports,"\n"));
		
		String source = template.renderToString(vars);
		File file=this.getSourceFile();
		FileUtil.writeText(file, source);
	}
	
	@Override
	public void save(boolean override) {
		this.save();
	}

	public MduCtx getContext() {
		return context;
	}
	 

	
	
}
