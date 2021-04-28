package com.github.foxnic.generatorV2.config;

import com.github.foxnic.generator.CodeGenerator.Mode;

public class GlobalSettings {

	public GlobalSettings() {
		
	}
	
	/**
	 * 项目分解模式
	 * */
	private Mode generatorMode=null;
	
	private boolean isEnableSwagger;
	private boolean isEnableMicroService;
	
	private String author;
	
	private Class superController;
	
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

	public Class getSuperController() {
		return superController;
	}

	public void setSuperController(Class superController) {
		this.superController = superController;
	}

	public boolean isEnableMicroService() {
		return isEnableMicroService;
	}

	public void setEnableMicroService(boolean isEnableMicroService) {
		this.isEnableMicroService = isEnableMicroService;
	}
}
