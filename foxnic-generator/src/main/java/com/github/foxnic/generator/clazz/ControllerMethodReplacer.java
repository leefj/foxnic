package com.github.foxnic.generator.clazz;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.generator.CodePoint;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

import io.swagger.annotations.ApiOperation;
import javassist.CtClass;
import javassist.NotFoundException;


/**
 * 用于替换已经生成的控制器注解代码
 * */
public class ControllerMethodReplacer {

	 
	private Class controllerClass;
	private Method controllerMethod;
	
	private int lineNumber;
	private String[] sourceLines=null;
	private Integer limitLineNumber;
	private CodePoint codePoint;
	public ControllerMethodReplacer(CodePoint codePoint,String controllerClassFullName,String methodName,String... paramTypeClassNames) throws Exception {
 
		this.codePoint=codePoint;
		this.controllerClass=ReflectUtil.forName(controllerClassFullName);
		
		if(this.controllerClass==null) {
			System.out.println();
		}
		
		List<Class> paramTypes=new ArrayList<>();
		for (String paramTypeClassName : paramTypeClassNames) {
			Class paramType=ReflectUtil.forName(paramTypeClassName);
			paramTypes.add(paramType);
		}
		 
		try {
			controllerMethod=this.controllerClass.getDeclaredMethod(methodName, paramTypes.toArray(new Class[0]));
		} catch (Exception e) {
			return;
		}
 
	}
	
	
	 
 
	private int findLineNumber(String statrs,String... notStars) {
		String line=null;
		int i=this.lineNumber;
		boolean matched=false;
		while(true) {
			line=this.sourceLines[i].trim();
			boolean isNotStarts=false;
			for (String ns : notStars) {
				if(line.startsWith(ns)) {
					isNotStarts=true;
				}
			}
			if(!isNotStarts && line.startsWith(statrs)) {
				matched=true;
				break;
			}
			i--;
			if(i<limitLineNumber) break;
		}
		return matched?i:-1;
	}
	
	
	private void readFile(File sourceFile) throws Exception {
		CtClass cclazz = JavassistUtil.getClass(controllerClass);
		lineNumber=JavassistUtil.getMethodLineNumber(controllerMethod);
		System.out.println(controllerMethod.getName()+"@"+lineNumber);
		sourceLines=FileUtil.readText(sourceFile).split("\n");
		limitLineNumber=0;
	 
		Method[] ms=this.controllerClass.getDeclaredMethods();
		for (Method m : ms) {
			int ln=JavassistUtil.getMethodLineNumber(m);
			if(ln<lineNumber && ln>limitLineNumber ) {
				limitLineNumber=ln;
			}
		}

	}


	public void replace(File sourceFile) throws Exception {
		readFile(sourceFile);
		int i=findLineNumber("@ApiOperation","@ApiOperationSupport");
		if(i>0) {
			ApiOperation ann=controllerMethod.getAnnotation(ApiOperation.class);
			String location=controllerClass.getName()+"."+controllerMethod.getName()+"@ApiOperation.value";
			//新生成的代码内容
			String current=ann.value();
			String oldcode=codePoint.getOldCode(location);
			boolean edited=oldcode!=null && !oldcode.equals(current);
			String newcode=codePoint.getNewCode(location);
			newcode="XXX";
			if(!edited) {
				sourceLines[i] = replace(sourceLines[i], "value", "\""+current+"\"", "\""+newcode+"\"");
			}
		}
		findLineNumber("@ApiImplicitParams");
		System.out.println();
		FileUtil.writeText(sourceFile, StringUtil.join(sourceLines,"\n"));
	}
	
	
	public String replace(String line,String key,String current,String newcode) {
		int i=line.indexOf(key);
		if(i==-1) return line;
		i=line.indexOf(current, i+key.length());
		line=line.substring(0,i)+newcode+line.substring(i+current.length());
		return line;
	}
	
	
	
	
}
