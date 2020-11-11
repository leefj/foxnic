package com.github.foxnic.commons.xml;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.dom4j.Element;
import org.junit.Test;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.project.maven.MavenProject;

public class XMLTest {

	@Test
	public void xPathNoNsTest()
	{
		File pomFile=FileUtil.resolveByInvoke("pom-no-ns.xml");
		XML xml=new XML(pomFile);
		Element modules=xml.selectNode("project/modules");
		assertTrue(modules!=null);
	}
	
	@Test
	public void xPathWithNsTest()
	{
		File pomFile=FileUtil.resolveByInvoke("pom-with-ns.xml");
		XML xml=new XML(pomFile);
		xml.addNamespace("t", "http://maven.apache.org/POM/4.0.0");
		Element modules=xml.selectNode("t:project/t:modules");
		assertTrue(modules!=null);
	}
	
	@Test
	public void removeNodeAndSave()
	{
		MavenProject mp=new MavenProject();
		File dir=mp.getSourceFile(this.getClass()).getParentFile();
		File pomFile=FileUtil.resolveByPath(dir,"pom-with-ns.xml");
		
		XML xml=new XML(pomFile);
		xml.addNamespace("t", "http://maven.apache.org/POM/4.0.0");
		Element modules=xml.selectNode("t:project/t:modules");
		modules.getParent().remove(modules);
 
		File newPomFile=FileUtil.resolveByPath(dir,"pom-with-ns-new.xml");
		xml.saveAs(newPomFile);
	}
	
	
}
