package com.github.foxnic.commons.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

public class XML {

	private Document doc;
	private File file;

	private Map<String, String> namespaceURIs = new HashMap<>();

	public XML(File file) {
		SAXReader reader = new SAXReader();
		try {
			this.doc = reader.read(file);
			this.file = file;
		} catch (DocumentException e) {
			Logger.error("XML Read Error, file : " + file.getAbsolutePath(), e);
		}
	}

	public XML(String path) {
		this(new File(path));
	}

	public Document getDocument() {
		return doc;
	}

	public Element getRootElement() {
		return doc.getRootElement();
	}

	public void addNamespace(String prefix, String uri) {
		namespaceURIs.put(prefix, uri);
	}

	/**
	 * xpath 示例：<br>
	 * classroom/persons/students <br>
	 * ns:project/ns:modules
	 */
	public Element selectNode(String xpath) {

		XPath xp = getXPath(xpath);
		try {
			return (Element) xp.selectSingleNode(this.doc);
		} catch (JaxenException e) {
			Logger.error("xpath 异常", e);
			return null;
		}
	}

	private XPath getXPath(String xpath) {
		try {
			XPath path = new Dom4jXPath(xpath);
			String ns = this.getRootElement().getNamespaceURI();
			if (!StringUtil.isBlank(ns)) {
				if (namespaceURIs == null || namespaceURIs.isEmpty()) {
					throw new IllegalArgumentException("未设定Namespace");
				}
				for (Entry<String, String> e : namespaceURIs.entrySet()) {
					path.addNamespace(e.getKey(), e.getValue());
				}
			}
			return path;
		} catch (JaxenException e) {
			Logger.error("xpath Error", e);
			return null;
		}
	}

	public void saveAs(File newPomFile) {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		format.setIndentSize(4);
		try {
			XMLWriter writer = new XMLWriter(new FileWriter(newPomFile), format);
			writer.write(this.doc);
			writer.close(); 
		} catch (IOException e) {
			Logger.error("xml write error",e);
		}

	}
	
	public void save() {
		this.saveAs(file);
	}

}
