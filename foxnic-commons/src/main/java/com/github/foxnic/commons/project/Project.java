package com.github.foxnic.commons.project;

import java.io.File;

import com.github.foxnic.commons.io.FileUtil;

public abstract class Project {
	
	protected File projectDir =  null;
	protected File identityFile = null;
	
	public void init(File projectDir) {
		this.projectDir = projectDir;
	}
 
	protected void init(Class clz,String identityFileName) {
		identityFile = findProjectFile(clz,".classpath");
		projectDir=identityFile.getParentFile();
	}
	
	private File findProjectFile(Class clz , String filename) {
		File dir=FileUtil.resolveByClass(clz);
		File file=null;
		while(true) {
			file=new File(dir.getAbsolutePath()+"/"+filename);
			if(file.exists()) {
				break;
			} else {
				file=null;
			}
			dir=dir.getParentFile();
			if(dir==null) {
				break;
			}
		}
		return file;
	}

	
	public File getProjectDir() {
		return projectDir;
	}

	public File getIdentityFile() {
		return identityFile;
	}
	
}
