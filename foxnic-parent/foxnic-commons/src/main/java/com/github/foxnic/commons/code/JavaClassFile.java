package com.github.foxnic.commons.code;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.reflect.ReflectUtil;

public class JavaClassFile {

	public static final JavaClassFile EMPTY=new JavaClassFile(null, "empty", "empty");
	
	protected static final BeanNameUtil beanNameUtil=new BeanNameUtil();
 
	protected CodeBuilder code;
	
	private String packageName;
	private String simpleName;
	private MavenProject project;
	private String var;
	
	protected Set<String> imports;
	
	
	private Class superType=null;
	private JavaClassFile superTypeFile=null;
	
	
	public JavaClassFile(MavenProject project,String packageName,String simpleName) {
		this.project=project;
		this.packageName=packageName;
		this.simpleName=simpleName;
		this.var=this.simpleName.substring(0,1).toLowerCase()+this.simpleName.substring(1);
		//
		this.code=new CodeBuilder();
		//
		imports=new LinkedHashSet<>();
		imports.add("");
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
		code.ln("");
		code.ln("");
		buildBody();
		
		String source=insertImports();
		
		return source;
		
	}
	
	private String insertImports() {
		
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
		if(cls.contains("MaxDate")) {
			System.out.println();
		}
		
		String clsPkg=cls.substring(0,cls.lastIndexOf("."));
		if(clsPkg.equals(this.packageName)) {
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
		String source = getSourceCode();
		FileUtil.writeText(f, source);
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

	public String getVar() {
		return var;
	}
	
	public Class getType() {
		return ReflectUtil.forName(this.getFullName());
	}
	
	/**
	 * 从 superTypeFile 和  superType 中提取父类名称，并自动加入 import 
	 * */
	public String getSuperTypeSimpleName() {
		if(this.superTypeFile!=null) {
			this.addImport(this.superTypeFile.getFullName());
			return this.superTypeFile.getSimpleName();
		}
		if(this.superType!=null) {
			this.addImport(superType);
			return superType.getSimpleName();
		}
		
		return null;
	}
	
	/**
	 * 从 superTypeFile 和  superType 中提取父类名称，并自动加入 import 
	 * */
	public String getSuperTypeFullName() {
		if(this.superTypeFile!=null) {
			this.addImport(this.superTypeFile.getFullName());
			return this.superTypeFile.getFullName();
		}
		if(this.superType!=null) {
			this.addImport(superType);
			return superType.getName();
		}
		
		return null;
	}

	public void setSuperType(Class superType) {
		if(Object.class.equals(superType)) superType=null;
		this.superType = superType;
		this.superTypeFile = null;
	}
 
	public void setSuperTypeFile(JavaClassFile superTypeFile) {
		if(EMPTY==superTypeFile) superTypeFile=null;
		this.superTypeFile = superTypeFile;
		this.superType = null;
	}
	
}
