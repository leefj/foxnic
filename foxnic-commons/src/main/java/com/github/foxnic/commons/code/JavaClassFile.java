package com.github.foxnic.commons.code;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;

public class JavaClassFile {

	protected CodeBuilder code;
	
	private String packageName;
	private String simpleName;
	private MavenProject project;
	
	private Set<String> imports;
	
	
	public JavaClassFile(MavenProject project,String packageName,String simpleName) {
		this.project=project;
		this.packageName=packageName;
		this.simpleName=simpleName;
		//
		this.code=new CodeBuilder();
		//
		imports=new LinkedHashSet<>();
	}
	
	/**
	 * 子类覆盖
	 * */
	protected void buildBody() {
 
	}
	
	/**
	 * 获取源码
	 * */
	public String getSourceCode() {
	
		code.clear();
		code.ln("package "+this.packageName+";");
		code.ln("");
		for (String imp : imports) {
			code.ln(imp);
		}
		code.ln("");
		code.ln("");
		buildBody();
		
		return code.toString();
		
	}
	
	protected void addJavaDoc(int tabs,String... doc) {
		code.ln(tabs,"");
		code.ln(tabs,"/**");
		for (int i = 0; i <doc.length ; i++) {
			if(StringUtil.isBlank(doc[i])) continue;
			code.ln(tabs," * "+doc[i]+(i<doc.length?"":""));
		}
		code.ln(tabs,"*/");
	}
	
 
	public void addImport(Class cls) {
		this.addImport(cls.getName());
	}
	
	public void addImport(String cls) {
		if(cls.equals("[Ljava.lang.Byte;")) {
			return;
		}
		if(cls.startsWith("java.lang.") && cls.split("\\.").length==3 ) return;
		imports.add("import "+cls+";");
	}
	
	public String getFullName() {
		return packageName+"."+simpleName;
	}
	
	public File getSourceFile() {
		return  FileUtil.resolveByPath(project.getMainSourceDir(),this.getFullName().replace('.', File.separatorChar)+".java");
	}
	
	/**
	 * 写入
	 * */
	public void save(boolean override) {
		File f=getSourceFile();
		if(!override && f.exists()) return;
		getSourceCode();
		code.wirteToFile(f);
	}

	public String getPackageName() {
		return packageName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public MavenProject getProject() {
		return project;
	}
	
	
}
