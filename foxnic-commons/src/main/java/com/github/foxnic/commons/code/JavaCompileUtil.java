package com.github.foxnic.commons.code;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Stack;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

public class JavaCompileUtil {
	
	
	private static HashMap<String, Boolean> IS_FIRST_COMPILE=new HashMap<String, Boolean>();
	//private static String WORK_PATH = null;
	
	/**
	 * 编译源码
	 * */
	public synchronized static void compile(String dir,String source,String className) {
		
		
		
		if(StringUtil.isBlank(dir)) {
			dir="dymamic-source";
		}
		//初始化目录
		//if(WORK_PATH==null) {
//			 ;
//			File dir=FileUtil.createTempFile("fox-", "-nic");
//	 
//			//删除之前的无效文件
//			File[] fs=dir.getParentFile().listFiles();
//			for (File f : fs) {
//				if(dir.getAbsolutePath().equals(f.getAbsolutePath())) continue;
//				if(f.isDirectory() && f.getName().startsWith("fox-") && f.getName().endsWith("-nic")) {
//					FileUtil.delete(f, true);
//				}
//			}
			
			dir = FileUtil.resolveByPath(System.getProperty("java.io.tmpdir"),dir).getAbsolutePath();
		//}
			
			
		Boolean isFirstCompile=IS_FIRST_COMPILE.get(dir);
		if(isFirstCompile==null) isFirstCompile=true;
		if(isFirstCompile) {
			FileUtil.delete(new File(dir), true);
		}
		
		String pack = "";
		final String[] split = className.split("\\.");
		String clazz = split[split.length-1];
		for(int i=0;i<split.length-1;i++){
			pack+=split[i]+File.separator;
		}
		
	    String filePath = dir+File.separator+pack;  
	    File f = new File(filePath);  
	    if(!f.exists())
	    	f.mkdirs();
	    
	    final String pathname = filePath+clazz+".java";
		f = new File(pathname);
		try (FileWriter fw = new FileWriter(f);){
			fw.write(source);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	      
	    //获取jdk编译器  
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();  
	    if(compiler==null) {
	    	throw new RuntimeException("获取编译器失败");
	    }
	    StandardJavaFileManager fileMgr = null;
	    try{
	    	fileMgr = compiler.getStandardFileManager(null, null, null);  
	        final Iterable<? extends JavaFileObject> javaFileObjects = fileMgr.getJavaFileObjects(pathname);  
		    //编译
		    compiler.getTask(null, fileMgr, null, null, null, javaFileObjects).call();
		    loadClass(dir);
		    IS_FIRST_COMPILE.put(dir,false);
	    }catch (Exception e) {
	    	throw new RuntimeException("编译失败",e);
		}finally{
			if(fileMgr!=null)
				try {
					fileMgr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	private static void loadClass(String dir) {
		try {
			 // 例如/usr/java/classes下有一个test.App类，则/usr/java/classes即这个类的根路径，而.class文件的实际位置是/usr/java/classes/test/App.class
			 File clazzPath = new File(dir);
			 // 记录加载.class文件的数量
			 int clazzCount = 0;
			 if (clazzPath.exists() && clazzPath.isDirectory()) {
			 	// 获取路径长度
			 	int clazzPathLen = clazzPath.getAbsolutePath().length() + 1;
 
			 	Stack<File> stack = new Stack<>();
			 	stack.push(clazzPath);
 
			 	// 遍历类路径
			 	while (stack.isEmpty() == false) {
			 		File path = stack.pop();
			 		File[] classFiles = path.listFiles(new FileFilter() {
			 			public boolean accept(File pathname) {
			 				return pathname.isDirectory() || pathname.getName().endsWith(".class");
			 			}
			 		});
			 		
			 		for (File subFile : classFiles) {
			 			if (subFile.isDirectory()) {
			 				stack.push(subFile);
			 			} else {
			 				if (clazzCount++ == 0) {
			 					Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			 					boolean accessible = method.isAccessible();
			 					try {
			 						if (accessible == false) {
			 							method.setAccessible(true);
			 						}
			 						// 设置类加载器
			 						URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			 						// 将当前类路径加入到类加载器 强制将累加入当前classpath中
			 						method.invoke(classLoader, clazzPath.toURI().toURL());
			 					} finally {
			 						method.setAccessible(accessible);
			 					}
			 				}
			 				// 文件名称
			 				String className = subFile.getAbsolutePath();
			 				className = className.substring(clazzPathLen, className.length() - 6);
			 				className = className.replace(File.separatorChar, '.');
			 				// 加载Class类
			 				Logger.info("动态加载[class:{"+className+"}]");
			 			}
			 		}
			 	}
			 } 
		} catch (Exception e) {
		}
	}
	

}
