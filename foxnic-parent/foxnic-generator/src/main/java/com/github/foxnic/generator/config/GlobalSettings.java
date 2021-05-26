package com.github.foxnic.generator.config;

public class GlobalSettings {
	
	private static GlobalSettings settings;
	
	public static GlobalSettings instance() {
		return settings;
	}

	public GlobalSettings() {
		settings=this;
	}
 
	private boolean isEnableSwagger;
	private boolean isEnableMicroService;
	
	private String author;
	private boolean frontendDepart;
	
	private String listHTMLTemplatePath="templates/list.html.vm";
	private String listJSTemplatePath="templates/list.js.vm";
	
	private String formHTMLTemplatePath="templates/form.html.vm";
	private String formJSTemplatePath="templates/form.js.vm";

 
	public boolean isEnableSwagger() {
		return isEnableSwagger;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setEnableSwagger(boolean isEnableSwagger) {
		this.isEnableSwagger = isEnableSwagger;
	}
 
	public boolean isEnableMicroService() {
		return isEnableMicroService;
	}

	public void setEnableMicroService(boolean isEnableMicroService) {
		this.isEnableMicroService = isEnableMicroService;
	}

	public void setFrontendDepart(boolean frontendDepart) {
		 this.frontendDepart=frontendDepart;
	}

	public boolean isFrontendDepart() {
		return frontendDepart;
	}
 
	public String getListHTMLTemplatePath() {
		return listHTMLTemplatePath;
	}

	public void setListHTMLTemplatePath(String listHTMLTemplatePath) {
		this.listHTMLTemplatePath = listHTMLTemplatePath;
	}

	public String getListJSTemplatePath() {
		return listJSTemplatePath;
	}

	public void setListJSTemplatePath(String listJSTemplatePath) {
		this.listJSTemplatePath = listJSTemplatePath;
	}

	public String getFormHTMLTemplatePath() {
		return formHTMLTemplatePath;
	}

	public void setFormHTMLTemplatePath(String formHTMLTemplatePath) {
		this.formHTMLTemplatePath = formHTMLTemplatePath;
	}

	public String getFormJSTemplatePath() {
		return formJSTemplatePath;
	}

	public void setFormJSTemplatePath(String formJSTemplatePath) {
		this.formJSTemplatePath = formJSTemplatePath;
	}
	
	
}
