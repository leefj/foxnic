package com.github.foxnic.commons.environment;

import com.github.foxnic.commons.project.EclipseProject;
import com.github.foxnic.commons.project.IDEAProject;
import com.github.foxnic.commons.project.Project;
import com.github.foxnic.commons.project.maven.MavenProject;

/**
 * 用于检测当前程序的运行环境
 * */
public class RunEnv {
	
	private static Boolean IS_RUN_IN_IDE = null;
	
	private static Boolean IS_RUN_IN_ECLIPSE = null;
	
	private static Boolean IS_RUN_IN_IDEA = null;
	
	private static Boolean IS_MAVEN_PROJECT = null;

 
	/**
	 * 是否在集成开发环境中启动应用
	 * */
	public static boolean isRunInIDE(Class cls) {
		if(IS_RUN_IN_IDE!=null) return IS_RUN_IN_IDE;
		IS_RUN_IN_IDE=(isRunInEclipse(cls) || isRunInIDE(cls)) && isMavenProject(cls);
		return IS_RUN_IN_IDE;
	}
	
	/**
	 * 是否在Eclipse环境运行
	 * */
	public static boolean isRunInEclipse(Class cls) {
		if(IS_RUN_IN_ECLIPSE!=null) return IS_RUN_IN_ECLIPSE;
		Project project=null;
		try {
			project=new EclipseProject(cls);
		} catch (Exception e) {}
		IS_RUN_IN_ECLIPSE=project!=null && project.getProjectDir().exists();
		return IS_RUN_IN_ECLIPSE;
	}
	
	/**
	 * 是否在 IDEA 环境运行
	 * */
	public static boolean isRunInIdea(Class cls) {
		if(IS_RUN_IN_IDEA!=null) return IS_RUN_IN_IDEA;
		Project project=null;
		try {
			project=new IDEAProject(cls);
		} catch (Exception e) {}
		IS_RUN_IN_IDEA=project!=null && project.getProjectDir().exists();
		return IS_RUN_IN_IDEA;
	}
	
	
	/**
	 * 是否在 IDEA 环境运行
	 * */
	public static boolean isMavenProject(Class cls) {
		if(IS_MAVEN_PROJECT!=null) return IS_MAVEN_PROJECT;
		Project project=null;
		try {
			project=new MavenProject(cls);
		} catch (Exception e) {}
		IS_MAVEN_PROJECT=project!=null && project.getProjectDir().exists();
		return IS_MAVEN_PROJECT;
	}
 
}
