package com.github.foxnic.commons.project.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Node;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import com.github.foxnic.commons.cmd.CommandShell;
import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.xml.XML;

public class MavenBuilder {

	private CommandShell cmd = new CommandShell();

	private String settingsFile;
	private String mavenHome;

	private List<String> poms=new ArrayList<>();
	
	public MavenBuilder(String mavenHome, String settingsFile) {
		this.mavenHome = mavenHome;
		this.settingsFile = settingsFile;
	}
	
	/**
	 * 加入pom文件
	 * */
	public MavenBuilder pom(String pom,String... more) {
		poms.add(pom);
		poms.addAll(Arrays.asList(pom));
		return this;
	}
	
	/**
	 * 加入pom文件
	 * */
	public MavenBuilder pom(File pom,File... more) {
		poms.add(pom.getAbsolutePath());
		for (File file : more) {
			poms.add(file.getAbsolutePath());
		}
		return this;
	}
	
	
	public void clean() {
		for (String pomFile : poms) {
			clean(pomFile);
		}
	}
	
	
	public void clean(String pomFile) {
		String mvn = getMvnCmd();
		File pom=new File(pomFile);
		String cmdstr = mvn + " clean -f "+ pom.getName() + " --settings " + this.settingsFile;
		cmd.exec(cmdstr,pom.getParentFile());
	}
	
	
	public boolean deploy() {
		
		for (String pomFile : poms) {
			boolean suc=deploy(pomFile); 
			if(!suc) return false;
		}
		return true;
		
	}
	
	public boolean deploy(String pomFile)  {
		String mvn = getMvnCmd();
		File pom=new File(pomFile);
		String cmdstr = mvn + " deploy -e -f " + pomFile + " --settings " + this.settingsFile;
		String[] r=cmd.exec(cmdstr,pom.getParentFile());
		for (int i = r.length-1; i >=0 ; i--) {
			String ln=r[i];
			if("[INFO] BUILD SUCCESS".equals(ln)) return true;
		}
		return false;
	}
	
	
	public boolean install() {
		for (String pomFile : poms) {
			boolean suc=install(pomFile);
			if(!suc) return false;
		}
		return true;
	}
	
	public boolean install(String pomFile) {
		String mvn = getMvnCmd();
		File pom=new File(pomFile);
		String cmdstr = mvn + " clean install -e -f " + pomFile + " --settings " + this.settingsFile;
		String[] r=cmd.exec(cmdstr,pom.getParentFile());
		for (int i = r.length-1; i >=0 ; i--) {
			String ln=r[i];
			System.out.println(ln);
			if("[INFO] BUILD SUCCESS".equals(ln.trim())) return true;
		}
		return false;
	}

	
	
	private String getMvnCmd() {
		String mvn = null;
		if(OSType.isWindows()) {
			mvn=StringUtil.joinPath(mavenHome,"/bin/mvn.cmd");
		} else {
			mvn=StringUtil.joinPath(mavenHome,"/bin/mvn");
		}
		return mvn;
	}
	
	
	

}
