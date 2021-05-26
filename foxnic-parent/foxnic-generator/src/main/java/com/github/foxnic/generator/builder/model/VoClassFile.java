package com.github.foxnic.generator.builder.model;

public class VoClassFile extends PojoClassFile {
 
	public VoClassFile(PoClassFile poClassFile) {
		super(poClassFile.context,poClassFile.getProject(), poClassFile.getPackageName(), poClassFile.getSimpleName()+"VO");
		this.setSuperTypeFile(poClassFile);
	}

	@Override
	protected void buildOthers() {
		
	}

	private PojoProperty idsProperty;
	
	public void setIdsPropertyName(PojoProperty p) {
		idsProperty=p;
	}

	public PojoProperty getIdsProperty() {
		return idsProperty;
	}
	
	 

	
	
	

}
