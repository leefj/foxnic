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


	private String listHTMLTemplatePath="templates/list.html.vm";
	private String listJSTemplatePath="templates/list.js.vm";

	private String formHTMLTemplatePath="templates/form.html.vm";
	private String formJSTemplatePath="templates/form.js.vm";



	private String extJSTemplatePath="templates/ext.js.vm";

	public boolean isRebuildEntity() {
		return rebuildEntity;
	}

	/**
	 * 是否重新生成实体,判断实体对象是否有变化，如果无变化，则不重新生成实体
	 * */
	public void setRebuildEntity(boolean rebuildAllEntity) {
		this.rebuildEntity = rebuildAllEntity;
	}

	private boolean rebuildEntity=false;


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

	public String getExtJSTemplatePath() {
		return extJSTemplatePath;
	}

	public void setFormJSTemplatePath(String formJSTemplatePath) {
		this.formJSTemplatePath = formJSTemplatePath;
	}




}
