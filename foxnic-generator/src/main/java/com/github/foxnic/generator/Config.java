package com.github.foxnic.generator;

import java.io.File;

public class Config {
	
	private String modulePackageName;
	private String microServiceNameConst;
	 
	
	private File destination=null;
	
	public String getModulePackageName() {
		return modulePackageName;
	}
	public void setModulePackageName(String modulePackageName) {
		this.modulePackageName = modulePackageName;
	}
	public String getMicroServiceNameConst() {
		return microServiceNameConst;
	}
	public void setMicroServiceNameConst(String microServiceNameConst) {
		this.microServiceNameConst = microServiceNameConst;
	}
	
	public File getDestination() {
		return destination;
	}
	public void setDestination(File destination) {
		this.destination = destination;
	}
	
}
