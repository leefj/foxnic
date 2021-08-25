package com.github.foxnic.generator.config;

import com.github.foxnic.generator.builder.business.*;
import com.github.foxnic.generator.builder.view.*;

import java.util.HashMap;
import java.util.Map;

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

		public Overrides setExtendJsFile(WriteMode mode) {
			configs.put(ExtendJSFile.class, mode);
			return this;
		}
		
		public Overrides setListPage(WriteMode mode) {
			configs.put(ListPageHTMLFile.class, mode);
			configs.put(ListPageJSFile.class, mode);
			return this;
		}

		public WriteMode getListPage() {
			return  configs.get(ListPageHTMLFile.class);
		}

		public WriteMode getFormPage() {
			return  configs.get(FormPageHTMLFile.class);
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