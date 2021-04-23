package com.github.foxnic.generatorV2.builder;

public class VOClassFile extends PojoClassFile {
 
	public VOClassFile(PoClassFile poClassFile) {
		super(poClassFile.context,poClassFile.getProject(), poClassFile.getPackageName(), poClassFile.getSimpleName()+"VO");
	}

	@Override
	protected void buildOthers() {
 
	}

	
	
	

}
