package com.github.foxnic.commons.project.maven;

import java.io.File;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.github.foxnic.commons.xml.XML;

public class POMFile {
	
	private XML pom;
	private Element properties;
	
	public POMFile(File pom) {
		this.pom=new XML(pom);
		this.pom.addNamespace("n", "http://maven.apache.org/POM/4.0.0");
		this.properties=this.pom.selectNode("/n:project/n:properties");
	}
	
	public String getVersion() {
		return this.pom.selectNode("/n:project/n:version").getText();
	}
	
	public String getArtifactId() {
		return this.pom.selectNode("/n:project/n:artifactId").getText();
	}
	
	public void setVersion(String version) {
		this.pom.selectNode("/n:project/n:version").setText(version);
	}
	
	public void setParentVersion(String version) {
		this.pom.selectNode("/n:project/n:parent/n:version").setText(version);
	}
	
	public void setDistribution(String repositoryURL,String snapshotRepositoryURL) {
		this.pom.selectNode("/n:project/n:distributionManagement/n:repository/n:url").setText(repositoryURL);
		this.pom.selectNode("/n:project/n:distributionManagement/n:snapshotRepository/n:url").setText(repositoryURL);
	}
	
	public void removeModules() {
		Element m=this.pom.selectNode("/n:project/n:modules");
		if(m==null) return;
		m.getParent().remove(m);
	}
	
	public void setProperty(String name,String value) {
		Element p=this.pom.selectNode("/n:project/n:properties/n:"+name);
		if(p==null) {
			p=DocumentHelper.createElement(name);
			p.setText(value);
			this.properties.add(p);
		} else {
			p.setText(value);
		}
	}

	public void saveAs(File file) {
		this.pom.saveAs(file);
	}
	
	public void save() {
		this.pom.save();
	}
	
	

}
