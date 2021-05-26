package com.github.foxnic.generatorV2.config;

import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.generatorV2.builder.business.ApiControllerFile;
import com.github.foxnic.generatorV2.builder.business.ControllerProxyFile;
import com.github.foxnic.generatorV2.builder.business.PageControllerFile;
import com.github.foxnic.generatorV2.builder.business.ServiceImplmentFile;
import com.github.foxnic.generatorV2.builder.business.ServiceInterfaceFile;
import com.github.foxnic.generatorV2.builder.view.FormPageHTMLFile;
import com.github.foxnic.generatorV2.builder.view.FormPageJSFile;
import com.github.foxnic.generatorV2.builder.view.ListPageHTMLFile;
import com.github.foxnic.generatorV2.builder.view.ListPageJSFile;

public class Overrides {
	
		private Map<Class, WriteMode> configs=new HashMap<>();
	 
		public Overrides setControllerAndAgent(WriteMode mode) {
			configs.put(ApiControllerFile.class, mode);
			configs.put(ControllerProxyFile.class, mode);
			return this;
		}
		
		public Overrides setFormPage(WriteMode mode) {
			configs.put(FormPageHTMLFile.class, mode);
			configs.put(FormPageJSFile.class, mode);
			return this;
		}
		
		public Overrides setListPage(WriteMode mode) {
			configs.put(ListPageHTMLFile.class, mode);
			configs.put(ListPageJSFile.class, mode);
			return this;
		}
		
		public Overrides setPageController(WriteMode mode) {
			configs.put(PageControllerFile.class, mode);
			return this;
		}
		
		public Overrides setServiceIntfAnfImpl(WriteMode mode) {
			configs.put(ServiceInterfaceFile.class, mode);
			configs.put(ServiceImplmentFile.class, mode);
			return this;
		}
 
		public WriteMode getWriteMode(Class cls) {
			WriteMode wm=configs.get(cls);
			if(wm==null) {
				wm=WriteMode.CREATE_IF_NOT_EXISTS;
			}
			return wm;
		}
		
	}