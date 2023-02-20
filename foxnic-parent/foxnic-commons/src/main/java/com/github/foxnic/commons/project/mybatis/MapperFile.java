package com.github.foxnic.commons.project.mybatis;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.xml.XML;
import org.dom4j.Element;

import java.io.File;
import java.util.List;

public class MapperFile {



	private XML mapper;
	private Element properties;

	private File pomFile;

	public MapperFile(File pom) {
		this.pomFile=pom;
		this.mapper =new XML(pom);
	}

	public File getMapperFile() {
		return pomFile;
	}

	public List<Element> getResultMaps() {
		return this.mapper.getDocument().selectNodes("mapper/resultMap");
	}


	public void saveAs(File file) {
		this.mapper.saveAs(file);
		this.removeNodeEmptyNameSpace(file);
	}

	public void save() {
		this.mapper.save();
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
