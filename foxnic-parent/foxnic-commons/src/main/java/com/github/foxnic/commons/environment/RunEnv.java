package com.github.foxnic.commons.environment;

import com.github.foxnic.commons.project.EclipseProject;
import com.github.foxnic.commons.project.Project;

/**
 * 用于检测当前程序的运行环境
 * */
public class RunEnv {

	public static boolean isRunInIDE(Class cls) {
		
		Project project=null;
		try {
			project=new EclipseProject(cls);
		} catch (Exception e) {}
		if(project!=null && project.getProjectDir().exists()) {
			return true;
		}
		
		return false;
	}
	
	
	public static void main(String[] args) {
		System.out.println(isRunInIDE(RunEnv.class));
	}
	
}
