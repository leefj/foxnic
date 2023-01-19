package com.github.foxnic.commons.project.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.foxnic.commons.io.FileUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.github.foxnic.commons.xml.XML;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class POMFile {

	public static enum NotExistAction {
		/**
		 * 常规操作，节点不存在就报异常
		 * */
		EXCEPTION,
		IGNORE,
		CREATE;
	}

	private XML pom;
	private Element properties;

	private File pomFile;

	public POMFile(File pom) {
		this.pomFile=pom;
		this.pom=new XML(pom);
		this.pom.addNamespace("n", "http://maven.apache.org/POM/4.0.0");
		this.properties=this.pom.selectNode("/n:project/n:properties");
	}

	private String castAsXPath(String path) {
		return path.replace("/","/n:");
	}

	public File getPomFile() {
		return pomFile;
	}

	public String getVersion() {
		return this.pom.selectNode("/n:project/n:version").getText();
	}

	public Element getModules() {
		return this.pom.selectNode("/n:project/n:modules");
	}

	public Set<String> getModulePaths() {
		Set<String> set=new HashSet<>();
		Element modules= this.getModules();
		List<Element> list=modules.elements("module");
		for (Element element : list) {
			set.add(element.getStringValue());
		}
		return set;
	}

	public Set<File> getModulePomFiles() {
		Set<String> set=this.getModulePaths();
		Set<File> files=new HashSet<>();
		for (String f : set) {
			f=f.trim();
			if(f.startsWith("./")) {
				f=f.substring(2);
			}
			File file=new File(this.pomFile.getParentFile().getAbsolutePath()+"/"+f+"/pom.xml");
			files.add(file);
		}
		return files;
	}

	public String getArtifactId() {
		return this.pom.selectNode("/n:project/n:artifactId").getText();
	}

	public void setVersion(String version) {
		setVersion(version, NotExistAction.EXCEPTION);
	}
	public void setVersion(String version, NotExistAction notExistAction) {
		String xpath="/n:project/n:version";
		Element e=this.pom.selectNode(xpath);
		if(notExistAction == NotExistAction.EXCEPTION) {
			e.setText(version);
		} else if (notExistAction == NotExistAction.IGNORE) {
			if(e!=null) {
				e.setText(version);
			}
		} else if (notExistAction == NotExistAction.CREATE) {
			e=makeElements(xpath);
			e.setText(version);
		}
	}

	public Element makeElements(String xpath) {
		String[] parts=xpath.split("/");
		Element e =null;
		for (int i = 0; i < parts.length; i++) {
			// 待实现
		}
		return e;
	}

	public void setParentVersion(String version) {
		this.pom.selectNode("/n:project/n:parent/n:version").setText(version);
	}

	public void setDistribution(String repositoryURL,String snapshotRepositoryURL) {
		this.pom.selectNode("/n:project/n:distributionManagement/n:repository/n:url").setText(repositoryURL);
		this.pom.selectNode("/n:project/n:distributionManagement/n:snapshotRepository/n:url").setText(snapshotRepositoryURL);
	}

	public void removeModules() {
		Element m=this.pom.selectNode("/n:project/n:modules");
		if(m==null) return;
		m.getParent().remove(m);
	}

	public void setProperty(String name,String value) {
		setProperty(name,value, NotExistAction.EXCEPTION);
	}

	public void setProperty(String name, String value, NotExistAction notExistAction) {
		String xpath="/n:project/n:properties/n:"+name;
		Element p=this.pom.selectNode(xpath);
		if(notExistAction == NotExistAction.EXCEPTION) {
			p.setText(value);
		} else if (notExistAction == NotExistAction.IGNORE) {
			if(p!=null) {
				p.setText(value);
			}
		} else if (notExistAction == NotExistAction.CREATE) {
			if(p==null) {
				p=DocumentHelper.createElement(new QName(name,Namespace.NO_NAMESPACE));
				p.setText(value);
				this.properties.add(p);
			} else {
				p.setText(value);
			}
		}

	}

	public void setScmTag(String version) {
		setScmTag(version, NotExistAction.EXCEPTION);
	}

	public void setScmTag(String version, NotExistAction notExistAction) {
		String xpath= castAsXPath("/project/scm/tag");
		Element tag=this.pom.selectNode(xpath);
		if(notExistAction == NotExistAction.EXCEPTION) {
			tag.setText(version);
		} else if(notExistAction == NotExistAction.IGNORE) {
			if(tag!=null) {
				tag.setText(version);
			}
		}   else if(notExistAction == NotExistAction.CREATE) {
			tag=makeElements(xpath);
			tag.setText(version);
		}
	}

	public List<Element> getPlugins() {
		Element plugins=this.pom.selectNode("/n:project/n:build/n:plugins");
		List<Element> es=plugins.elements();
		return es;
	}

	public Element getPlugin(String groupId,String artifactId) {
		List<Element> es=getPlugins();
		List<Element> els=new ArrayList<>();
		for (Element e : es) {
			String gId=e.element("groupId").getText();
			String aId=e.element("artifactId").getText();
 			if(groupId.equals(gId) && artifactId.equals(aId)) {
				 els.add(e);
			}
		}
		if(els.size()==0) {
			throw new RuntimeException("Plugin groupId="+groupId+" , artifactId="+artifactId+" 不存在");
		}
		if(els.size()>1) {
			throw new RuntimeException("Plugin groupId="+groupId+" , artifactId="+artifactId+" 匹配多个");
		}
		return els.get(0);
	}

	public void setSourceDirs(String... dirs) {
		Element e= getPlugin("org.codehaus.mojo","build-helper-maven-plugin");
		if(e==null) {
			return;
		}
		Element sources =  e.element("executions").element("execution").element("configuration").element("sources");

	 	List<Element> sourceList=sources.elements("source");
		for (Element element : sourceList) {
			sources.remove(element);
		}
		for (String dir  : dirs) {
			Element s=DocumentHelper.createElement(new QName("source"));
			s.setText(dir);
			sources.add(s);
		}
	}

	public void saveAs(File file) {
		this.pom.saveAs(file);
		this.removeNodeEmptyNameSpace(file);
	}

	public void save() {
		this.pom.save();
		this.removeNodeEmptyNameSpace(this.pomFile);
	}

	private void removeNodeEmptyNameSpace(File file) {
		String text= FileUtil.readText(file);
		String kw=" xmlns=\"\"";
		if(text.contains(kw)) {
			text = text.replace(kw,"");
		}
		FileUtil.writeText(file,text);
	}



}
