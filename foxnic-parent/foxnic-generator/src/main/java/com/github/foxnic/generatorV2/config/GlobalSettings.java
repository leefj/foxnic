package com.github.foxnic.generatorV2.config;

public class GlobalSettings {
	
	 
	

	public GlobalSettings() {
		
	}
 
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
 
	public boolean isEnableMicroService() {
		return isEnableMicroService;
	}

	public void setEnableMicroService(boolean isEnableMicroService) {
		this.isEnableMicroService = isEnableMicroService;
	}
}
