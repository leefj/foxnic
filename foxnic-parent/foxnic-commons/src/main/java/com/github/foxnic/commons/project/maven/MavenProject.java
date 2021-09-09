package com.github.foxnic.commons.project.maven;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.Project;
import com.github.foxnic.commons.reflect.ReflectUtil;

import java.io.File;

public class MavenProject extends Project {
 
	private File pomFile=null;
	private File targetDir=null;
	private File targetClassesDir=null;
	private File mainSourceDir=null;
	private File mainResourceDir=null;
	private File testSourceDir=null;
	private File testResourceDir=null;
	
	/**
	 * 在指定路径上寻找Maven项目
	 * */
	public MavenProject(File projectDir) {
		this.init(projectDir);
		initFileAndDir();
	}

	/**
	 * 在指定路径上寻找Maven项目
	 * */
	public MavenProject(String path) {
		this(new File(path));
	}

	public void init(File projectDir) {

		while(true) {
			this.projectDir = projectDir;
			identityFile=FileUtil.resolveByPath(projectDir,"pom.xml");
//			File src=FileUtil.resolveByPath(projectDir,"src");
			if(identityFile.exists()) {
				break;
			}
			projectDir=projectDir.getParentFile();
			if(projectDir==null) {
				throw new RuntimeException("no maven project in path");
			}
		}
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

	public String getSourcePath(String targetPath) {
		
		//针对 window 的特殊处理
		targetPath=targetPath.replace('\\', '/');
		String baseTargetPath=this.getTargetDir().getAbsolutePath().replace('\\', '/');
		if(!targetPath.startsWith(baseTargetPath)){
			throw new RuntimeException("not int target path of this project , "+this.projectDir.getAbsolutePath());
		}
		int i=FileUtil.resolveByPath(this.getTargetDir(),"classes").getAbsolutePath().length();
		return FileUtil.resolveByPath(this.getMainSourceDir(),targetPath.substring(i)).getAbsolutePath();
		
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
