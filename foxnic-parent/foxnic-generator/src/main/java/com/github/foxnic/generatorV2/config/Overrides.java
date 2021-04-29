package com.github.foxnic.generatorV2.config;

import java.util.HashMap;
import java.util.Map;

import com.github.foxnic.generatorV2.builder.business.ApiControllerFile;
import com.github.foxnic.generatorV2.builder.business.ControllerAgentFile;
import com.github.foxnic.generatorV2.builder.business.PageControllerFile;
import com.github.foxnic.generatorV2.builder.business.ServiceImplmentFile;
import com.github.foxnic.generatorV2.builder.business.ServiceInterfaceFile;

public class Overrides {
	
		private Map<Class, WriteMode> configs=new HashMap<>();
	 
		public Overrides setControllerAndAgent(WriteMode mode) {
			configs.put(ApiControllerFile.class, mode);
			configs.put(ControllerAgentFile.class, mode);
			return this;
		}
		
		public Overrides setFormPage(WriteMode mode) {
//			configs.put(FormPageHTMLBuilder.class, mode);
//			configs.put(FormPageJSBuilder.class, mode);
			return this;
		}
		
		public Overrides setListPage(WriteMode mode) {
//			configs.put(ListPageHTMLBuilder.class, mode);
//			configs.put(ListPageJSBuilder.class, mode);
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
				wm=WriteMode.DO_NOTHING;
			}
			return wm;
		}
		
	}