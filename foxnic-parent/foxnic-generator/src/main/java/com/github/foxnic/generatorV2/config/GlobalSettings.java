package com.github.foxnic.generatorV2.config;

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
}
