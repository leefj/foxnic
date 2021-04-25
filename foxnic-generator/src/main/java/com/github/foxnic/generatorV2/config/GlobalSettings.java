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
}
