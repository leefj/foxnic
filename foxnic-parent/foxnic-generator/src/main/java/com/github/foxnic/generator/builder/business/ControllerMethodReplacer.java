package com.github.foxnic.generator.builder.business;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.reflect.JavassistUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;

import io.swagger.annotations.ApiOperation;
import javassist.CtClass;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 用于替换已经生成的控制器注解代码
 * */
public class ControllerMethodReplacer {

	private static final String FLAG_AS_REMOVED = "$$flag_as_removed$$";

	public class ApiImplicitParamPair {
		private String name;
		private String line;
		
		public ApiImplicitParamPair(String name,String line) {
			this.name=name;
			this.line=line;
		}
	}
	
	 
	private Class controllerClass;
	private Method controllerMethod;
	
	/**
	 * 方法行
	 * */
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
	
	
	 
	/**
	 * 找到代码行
	 * */
	private int findLineNumber(String statrs,String... notStars) {
		String line=null;
		int i=this.lineNumber;
		boolean matched=false;
		while(true) {
			if(i>sourceLines.length) break;
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
		// System.out.println(controllerMethod.getName()+"@"+lineNumber);
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
		//boolean isChanged=false;
		if(controllerMethod==null) {
			return;
		}
		readFile(sourceFile);
		int i=findLineNumber("@ApiOperation","@ApiOperationSupport");
//		if(controllerMethod.getName().equals("queryPagedList")) {
//			System.out.println();
//		}
		if(i>0) {
			ApiOperation ann=controllerMethod.getAnnotation(ApiOperation.class);
			String location=controllerClass.getName()+"."+controllerMethod.getName()+"@ApiOperation.value";
			//新生成的代码内容
			String current=ann.value();
			String oldcode=codePoint.getCodeInLog(location);
			boolean edited=oldcode!=null && !oldcode.equals(current);
			String newcode=codePoint.getNewCode(location);
			//如果未被编辑过，并且新代码与当前代码不一致，则替换
			if(!edited && !newcode.equals(current) ) {
				sourceLines[i] = replace(sourceLines[i], "value", "\""+current+"\"", "\""+newcode+"\"");
				//isChanged=true;
			}
		}

		//获取忽略的不是
		i=findLineNumber("@ApiOperationSupport");
		Set<String> ignors=new HashSet<>();
		if(i>-1) {
			ignors=getIgnoreParameters(sourceLines[i]);
		}
		
		//
		i=findLineNumber("@ApiImplicitParams");
		
		List<ApiImplicitParamPair> apiImplicitParamPairList=getApiImplicitParamPairList(codePoint.getApiImplicitParams(controllerClass.getName()+"."+controllerMethod.getName()));
		
		//循环 所有的 @ApiImplicitParam 代码行
		for (int j = i+1; j < lineNumber && i>0 ; j++) {
			if(!sourceLines[j].trim().startsWith("@ApiImplicitParam")) continue;
			boolean c=false;
			String name=getCurrent(sourceLines[j], "name");
			//处理 value 属性值
			c=replaceApiImplicitParam(j,"value",true);
			//if(c) {
				//isChanged=true;
			//}
			//处理 required 属性值
			c=replaceApiImplicitParam(j,"required",false);
			//if(c) {
				//isChanged=true;
			//}
			//处理 required 属性值
			c=replaceApiImplicitParam(j,"dataTypeClass",false);
			//if(c) {
			//	isChanged=true;
			//}
			
			ApiImplicitParamPair ap=findApiImplicitParamPairList(apiImplicitParamPairList,name);

			if(ap!=null) {
				ap.line = sourceLines[j];
				sourceLines[j] = FLAG_AS_REMOVED;
			} else {
//				String value=getCurrent(sourceLines[j], "value");
				//System.err.println("属性 "+name+"("+value+")"+" 已经被删除");
				//apiImplicitParamPairList.add(new ApiImplicitParamPair("any",sourceLines[j]));
			}
			
		}
 
		//替换代码行
		List<String> srcLines=new ArrayList<>();
		srcLines.addAll(Arrays.asList(sourceLines));
		 
		//移除占位行
		while(srcLines.indexOf(FLAG_AS_REMOVED)!=-1) {
			srcLines.remove(FLAG_AS_REMOVED);
		}
		//插入新行
		int j=i+1;
		for (ApiImplicitParamPair ap : apiImplicitParamPairList) {
			if(ignors.contains(ap.name)) continue;
			String ln=ap.line.trim();
			if(!ln.endsWith(",")) {
				ln+=",";
			}
		 
			if(j>1) {
				srcLines.add(j, "\t\t"+ln);
			}
			j++;
		}
		sourceLines=srcLines.toArray(new String[0]);
		
		//if(isChanged) {
			FileUtil.writeText(sourceFile, StringUtil.join(sourceLines,"\n"));
		//}
	}

	private Set<String> getIgnoreParameters(String sourceLine) {
		Set<String> pps=new HashSet<>();
		String vs=getCurrent(sourceLine,"ignoreParameters");
		if(vs==null) return pps;
		vs=vs.trim();
		vs=StringUtil.removeFirst(vs,"{");
		vs=StringUtil.removeLast(vs,"{");
		String[] vvs=vs.split(",");
		for (String v:vvs) {
			pps.add(v.trim());
		}
		return pps;

	}


	private List<ApiImplicitParamPair> getApiImplicitParamPairList(List<String> apiImplicitParams) {
		List<ApiImplicitParamPair> list=new ArrayList<>();
		for (String line : apiImplicitParams) {
			list.add(new ApiImplicitParamPair(getCurrent(line, "name"),line));
		}
		return list;
	}
	
	private ApiImplicitParamPair findApiImplicitParamPairList(List<ApiImplicitParamPair> apiImplicitParams,String name) {
		for (ApiImplicitParamPair ap : apiImplicitParams) {
			if(name.equals(ap.name)) return ap;
		}
		return null;
	}
	
	
	
	



	private boolean replaceApiImplicitParam(int j, String key, boolean quate) {
		String q="";
		if(quate) q="\"";
		boolean changed=false;
		String line=sourceLines[j].trim();
		if(!line.startsWith("@ApiImplicitParam")) return changed;
		String name=getCurrent(line, "name");
		String location=controllerClass.getName()+"."+controllerMethod.getName()+"@ApiImplicitParam."+name+"."+key;
		String current=getCurrent(line, key);
		String oldcode=q+codePoint.getCodeInLog(location)+q;
		boolean edited=oldcode!=null && !oldcode.equals(current);
		String newcode=q+codePoint.getNewCode(location)+q;
		//如果未被编辑过，并且新代码与当前代码不一致，则替换
		if(!edited && !newcode.equals(current)) {
			line= replace(sourceLines[j], key, current, newcode);
			sourceLines[j]=line;
			changed=true;
		}
		return changed;
	}




	public String replace(String line,String key,String current,String newcode) {
		int i=line.indexOf(key);
		if(i==-1) return line;
		i=line.indexOf(current, i+key.length());
		line=line.substring(0,i)+newcode+line.substring(i+current.length());
		return line;
	}
	
	public static String getCurrent(String line,String key) {
//		if(line.contains("Long.class")) {
//			System.out.println();
//		}
		int i=line.indexOf(key);
		if(i==-1) return line;
		i=line.indexOf("=", i+key.length());
		int j=line.indexOf(",", i+1);
		int k=line.indexOf(")", i+1);
		
		
		if(j==-1 && k==-1) {
			throw new RuntimeException("无法识别");
		} else if(j!=-1 && k==-1) {
			//保持j不变
		} else if(j==-1 && k!=-1) {
			j=k;
		} else if(j!=-1 && k!=-1) {
			if(k<j) {
				j=k;
			}
		}

		line=line.substring(i+1,j).trim();
		return line;
	}
	
	
	 
		
		
	
	
	
}
