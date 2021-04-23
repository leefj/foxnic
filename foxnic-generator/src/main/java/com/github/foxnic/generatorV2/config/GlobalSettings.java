package com.github.foxnic.generatorV2.config;

public class GlobalSettings {

	public GlobalSettings(boolean isEnableSwagger) {
		this.isEnableSwagger=isEnableSwagger;
	}
	
	
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
}
