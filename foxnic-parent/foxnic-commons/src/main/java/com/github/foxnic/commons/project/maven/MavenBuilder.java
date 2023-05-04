package com.github.foxnic.commons.project.maven;

import java.io.File;
import java.util.*;

import org.dom4j.Node;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import com.github.foxnic.commons.cmd.CommandShell;
import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.xml.XML;

public class MavenBuilder {

	public static interface WorkDirGetter {
		File getWorkDir(File pomFile);
	}


	private WorkDirGetter workDirGetter=new WorkDirGetter() {
		@Override
		public File getWorkDir(File pomFile) {
			return pomFile.getParentFile();
		}
	};

	private CommandShell cmd = new CommandShell();

	private String mavenHome;

	private List<String> poms=new ArrayList<>();
	private Map<String,String> options=new LinkedHashMap<>();

	public MavenBuilder(String mavenHome, String settingsFile,WorkDirGetter workDirGetter) {
		this.mavenHome = mavenHome;
		if(workDirGetter!=null) {
			this.workDirGetter = workDirGetter;
		}
		this.option("--settings",settingsFile);
	}

	public MavenBuilder(String mavenHome, String settingsFile) {
		this(mavenHome,settingsFile,null);
	}



	public MavenBuilder option(String option,String arg) {
		if(StringUtil.isBlank(option)) {
			throw new IllegalArgumentException("不允许空白");
		}
		if(option.contains(" ") || option.contains("\t")) {
			throw new IllegalArgumentException("option error");
		}
		if("-f".equalsIgnoreCase(option) || "--file".equalsIgnoreCase(option)) {
			throw new IllegalArgumentException("不允许 -f 或 --file , 请使用 pom 方法指定");
		}
		options.put(option,arg);
		return this;
	}

	public MavenBuilder option(String option) {
		return option(option,null);
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

	public MavenBuilder debugEnable(boolean printDebugInfo) {
		if(printDebugInfo) {
			this.option("--debug");
		} else {
			this.options.remove("--debug");
		}
		return this;
	}

	public MavenBuilder errorEnable(boolean printErrorInfo) {
		if(printErrorInfo) {
			this.option("--errors");
		} else {
			this.options.remove("--errors");
		}
		return this;
	}


	private String makeOptionPart() {
		String opt="";
		for (Map.Entry<String, String> entry : options.entrySet()) {
			opt+=" "+entry.getKey()+(StringUtil.isBlank(entry.getValue())?"":" "+entry.getValue());
		}
		return opt;
	}


	public void clean(String pomFile) {
		String mvn = getMvnCmd();
		File pom=new File(pomFile);
		String cmdstr = mvn + " clean -f "+ pom.getName() + makeOptionPart();
		cmd.exec(cmdstr,workDirGetter.getWorkDir(pom));
	}


	public boolean deploy(String revision) {

		for (String pomFile : poms) {
			boolean suc=deploy(pomFile,revision);
			if(!suc) return false;
		}
		return true;

	}

	public boolean deploy(String pomFile,String revision)  {
		File pom=new File(pomFile);
		String cmdstr = makeDeployCommand(pomFile,revision);
		String[] r=cmd.exec(cmdstr,workDirGetter.getWorkDir(pom));
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

		File pom=new File(pomFile);
		String cmdstr =makeInstallCommand(pomFile);
		String[] r=cmd.exec(cmdstr,workDirGetter.getWorkDir(pom));
		for (int i = r.length-1; i >=0 ; i--) {
			String ln=r[i];
			System.out.println(ln);
			if("[INFO] BUILD SUCCESS".equals(ln.trim())) return true;
		}
		return false;
	}


	public String makeInstallCommand(String pomFile) {
		return  getMvnCmd() + " clean install -f " + pomFile + makeOptionPart();
	}

	public String makeDeployCommand(String pomFile) {
		return makeDeployCommand(pomFile,null);
	}
	public String makeDeployCommand(String pomFile,String revision) {
		return  getMvnCmd() + (StringUtil.isBlank(revision) ? "" : (" -Drevision="+revision) )+"  deploy -f " + pomFile + makeOptionPart();
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
