package com.github.foxnic.commons.project.maven;

import java.io.File;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.Project;
import com.github.foxnic.commons.reflect.ReflectUtil;

public class MavenProject extends Project {
 
	private File pomFile=null;
	private File targetDir=null;
	private File targetClassesDir=null;
	private File mainSourceDir=null;
	private File mainResourceDir=null;
	private File testSourceDir=null;
	private File testResourceDir=null;
	
	
	public MavenProject(File projectDir) {
		super.init(projectDir);
		initFileAndDir();
	}
	
	public MavenProject() {
		Class clz=ReflectUtil.forName((new Throwable()).getStackTrace()[1].getClassName(), true);
		init(clz);
	}
	
	public MavenProject(Class clz) {
		this.init(clz);
	}
	
	private void init(Class clz) {
		super.init(clz,"pom.xml");
		initFileAndDir();
	}
 
	
	private void initFileAndDir() {
		this.pomFile=this.getIdentityFile();
		this.targetDir=FileUtil.resolveByPath(this.getProjectDir(),"target");
		this.targetClassesDir=FileUtil.resolveByPath(this.getProjectDir(),"target","classes");
		this.mainSourceDir=FileUtil.resolveByPath(this.getProjectDir(),"src","main","java");
		this.mainResourceDir=FileUtil.resolveByPath(this.getProjectDir(),"src","main","resources");
		this.testSourceDir=FileUtil.resolveByPath(this.getProjectDir(),"src","test","java");
		this.testResourceDir=FileUtil.resolveByPath(this.getProjectDir(),"src","test","resources");
	}

	public File getSourceFile(Class clz) {
		File file=FileUtil.resolveByClass(clz);
		String rel=FileUtil.getRelativePath(targetClassesDir, file);
		rel=FileUtil.changeExtName(rel,".java");
		File src=FileUtil.resolveByPath(mainSourceDir, rel);
		if(src.exists()) {
			return src;
		}
		src=FileUtil.resolveByPath(testSourceDir, rel);
		if(src.exists()) {
			return src;
		}
		//
		return null;
	}

	public File getPomFile() {
		return pomFile;
	}

	public File getTargetDir() {
		return targetDir;
	}

	public File getTargetClassesDir() {
		return targetClassesDir;
	}

	public File getMainSourceDir() {
		return mainSourceDir;
	}

	public File getTestSourceDir() {
		return testSourceDir;
	}

	public File getMainResourceDir() {
		return mainResourceDir;
	}

	public File getTestResourceDir() {
		return testResourceDir;
	}
 
}
