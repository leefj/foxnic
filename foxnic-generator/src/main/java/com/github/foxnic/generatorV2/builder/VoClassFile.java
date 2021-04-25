package com.github.foxnic.generatorV2.builder;

public class VoClassFile extends PojoClassFile {
 
	public VoClassFile(PoClassFile poClassFile) {
		super(poClassFile.context,poClassFile.getProject(), poClassFile.getPackageName(), poClassFile.getSimpleName()+"VO");
		this.setSuperTypeFile(poClassFile);
	}

	@Override
	protected void buildOthers() {
		
	}

	
	
	

}
