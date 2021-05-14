package com.github.foxnic.commons.compiler;

import groovy.lang.GroovyClassLoader;

public class GroovyCompiler {

	public Class<?> compile(String source) {
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
		return groovyClassLoader.parseClass(source);
	}
	
}
