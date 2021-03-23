package com.github.foxnic.commons.code;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class InterfaceExtractor {

	private ClassPool clsPool=null;
	private Class clazz;
	private List<Method> methods;
	private MavenProject mp=null;
	private File srcFile=null;
	private String source=null;
	private String[] sourceLines=null;
	
	public InterfaceExtractor(Class clazz) throws Exception {
		this.clazz = clazz;
		this.clsPool = ClassPool.getDefault();
		this.mp=new MavenProject(this.clazz);
		this.srcFile=FileUtil.resolveByPath(this.mp.getMainSourceDir(), this.clazz.getName().replace('.', '/')+".java");
		this.source=FileUtil.readText(srcFile);
		this.sourceLines=this.source.split("\n");
		CtClass cclazz = clsPool.get(this.clazz.getName());
		this.methods=new ArrayList<>();
		Method[] methods=this.clazz.getDeclaredMethods();
		for (Method m : methods) {
			if(!Modifier.isPublic(m.getModifiers())) continue;
			if(Modifier.isStatic(m.getModifiers())) continue;
			this.methods.add(m);
			
			CtMethod cm=getMethod(m);
			
			int ln=getLineNumber(m);
			 
			String javaDoc=getJavaDoc(ln);
			 
			
			MethodInfo methodInfo = cm.getMethodInfo();
			CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
			String[] paramNames = new String[cm.getParameterTypes().length];
			int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
			for ( int i = 0; i < paramNames.length; i++) {
				paramNames[i] = attr.variableName(i + pos);
			}
			
			String iMethod="\n\t"+javaDoc+"\n\t"+m.getReturnType().getSimpleName()+" "+m.getName()+" (";
			for (int i = 0; i < m.getParameters().length; i++) {
				Parameter p=m.getParameters()[i];
				iMethod+=p.getType().getSimpleName()+" "+paramNames[i]+",";
			}
			 
			if(iMethod.endsWith(",")) iMethod=iMethod.substring(0,iMethod.length()-1);
			
			
			iMethod+=");";
			System.out.println(iMethod);
			
			
		}
	}
	
	public String getJavaDoc(int i) {
		
		int start=-1;
		int end=-1;
		i=i-2;
		String ln=null;
		while(true) {
			ln=sourceLines[i];
			if(end==-1) {
				if(ln.trim().equals("*/")) {
					end=i;
				}
				if(ln.trim().equals("}")) {
					break;
				}
			} else {
				if(ln.trim().startsWith("/**")) {
					start=i;
					break;
				}
			}
			i--;
			if(i<0) break;
		}
		String javadoc="";
		if(start!=-1 && end!=-1) {
			for (int j = start; j <= end; j++) {
				javadoc+=sourceLines[j]+"\n";
			}
			
		}
 
		return javadoc.trim();
	}
	
	
	private CtMethod getMethod(Method m) throws NotFoundException {
		CtClass cc = clsPool.get(m.getDeclaringClass().getName());
		CtClass[] pTypes=new CtClass[m.getParameterTypes().length];
		for (int i = 0; i < pTypes.length; i++) {
			pTypes[i]=clsPool.get(m.getParameterTypes()[i].getName());
		}
		return cc.getDeclaredMethod(m.getName(), pTypes);
	}
	
	private int getLineNumber(Method m) throws NotFoundException {
		CtMethod methodX=getMethod(m);
		return methodX.getMethodInfo().getLineNumber(0);
	}
	
	  
	
	
	
	

}
