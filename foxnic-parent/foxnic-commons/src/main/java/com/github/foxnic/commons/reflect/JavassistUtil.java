package com.github.foxnic.commons.reflect;

import javassist.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JavassistUtil {
	
	private static ClassPool CLS_POOL=null;
	
	private static void  initIf() {
		if(CLS_POOL==null) {
			CLS_POOL = ClassPool.getDefault();
		}
	}
	
	public static CtClass getClass(Class clz) throws NotFoundException {
		initIf();
		return  CLS_POOL.get(clz.getName());
	}
	
	public static  CtMethod getMethod(Method m) throws NotFoundException {
		if(m==null) return null;
		initIf();
		CtClass cc =getClass(m.getDeclaringClass());// CLS_POOL.get(m.getDeclaringClass().getName());
		CtClass[] pTypes=new CtClass[m.getParameterTypes().length];
		for (int i = 0; i < pTypes.length; i++) {
			pTypes[i]=CLS_POOL.get(m.getParameterTypes()[i].getName());
		}
		return cc.getDeclaredMethod(m.getName(), pTypes);
	}
	
	public static int getMethodLineNumber(Method m) throws NotFoundException {
		CtMethod methodX=getMethod(m);
		return methodX.getMethodInfo().getLineNumber(0);
	}

	 

	private static CtField getField(Field f) throws NotFoundException {
		CtClass cc =getClass(f.getDeclaringClass());
		return cc.getField(f.getName());
	}
	
	
}
