package com.github.foxnic.generatorV2.config;

public class GlobalSettings {
	
	public static enum Mode {
		/**
		 * 所有的代码生成到一个项目
		 * */
		ONE_PROJECT,
		/**
		 * 代码生成多个不同的项目
		 * */
		MULTI_PROJECT;
	}
	

	public GlobalSettings() {
		
	}
	
	/**
	 * 项目分解模式
	 * */
	private Mode generatorMode=null;
	
	private boolean isEnableSwagger;
	private boolean isEnableMicroService;
	
	private String author;
 
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

	public Mode getGeneratorMode() {
		return generatorMode;
	}

	public void setGeneratorMode(Mode generatorMode) {
		this.generatorMode = generatorMode;
	}

 
	public boolean isEnableMicroService() {
		return isEnableMicroService;
	}

	public void setEnableMicroService(boolean isEnableMicroService) {
		this.isEnableMicroService = isEnableMicroService;
	}
}
