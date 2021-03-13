package com.github.foxnic.generator.clazz;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.DateUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.generator.Context;
import com.github.foxnic.sql.entity.naming.DefaultNameConvertor;

public abstract class FileBuilder {
	
	protected Context ctx;
	
	protected DefaultNameConvertor convertor = new DefaultNameConvertor();
	
	protected CodeBuilder code=new CodeBuilder();
 
	public FileBuilder(Context cfg) {
		this.ctx=cfg;
	}
	
	public void appendAuthorAndTime() {
		appendAuthorAndTime(0);
	}
	
	

	public void appendAuthorAndTime(int tabs) {
		code.ln(tabs," * @author "+ctx.getAuthor());
		code.ln(tabs," * @since "+DateUtil.getFormattedTime(false));
	}
	
	private HashSet<String> imports=new HashSet<String>();
	
	public void addImport(Class cls) {
		this.addImport(cls.getName());
	}
	
	public void addImport(String cls) {
		if(cls.startsWith("java.lang.") && cls.split("\\.").length==3 ) return;
		imports.add("import "+cls+";");
	}
	
	protected String appendImports() {
		
		String[] lns=this.code.toString().split("\\n");
		int z=-1;
		for (int i = 0; i < lns.length; i++) {
			String ln=lns[i];
			if(ln.trim().startsWith("import ")) {
				z = i;
				break;
			}
		}
		
		if(z==-1) z=1;
		
		List<String> lines=new ArrayList<>();
		lines.addAll(Arrays.asList(lns));
		
		lines.addAll(z, this.imports);
		
		return StringUtil.join(lines,"\n");
		
	}
	
	protected abstract void build();
	
	public abstract void buildAndUpdate();
	
	protected void buildAndUpdateJava(File mainSourceDir,String classFullName) {
		this.build();
		this.saveJava(mainSourceDir,classFullName);
	}

	protected String toPath(String fullname) {
		return fullname.replace('.', '/');
	}

	private void saveJava(File mainSourceDir, String classFullName) {
		
		File sourceFile=FileUtil.resolveByPath(mainSourceDir, toPath(classFullName)+".java");
		sourceFile=processOverride(sourceFile);
		String code=this.appendImports();
		FileUtil.writeText(sourceFile, code);
		System.out.println(classFullName+"\t\t"+sourceFile.getAbsolutePath());
		
	}

	/**
	 * 判断将要生成的文件是否存在，如果要写入其它文件，则返回其它文件对象
	 * */
	protected abstract File processOverride(File sourceFile);
	
	

}
