package com.github.foxnic.generatorV2.builder.view.model;

import com.github.foxnic.dao.meta.DBColumnMeta;

public class FieldInfo {

	private String varName;
	private String label;
	
	private boolean isImageField=false;
	private boolean isLogicField=false;
	private LogicField logicField;
	private boolean isMulitiLine=false;
	
	public FieldInfo(DBColumnMeta cm) {
		this.label=cm.getLabel();
		this.varName=cm.getColumnVarName();
	}
	
	
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isImageField() {
		return isImageField;
	}
	public boolean getIsImageField() {
		return isImageField;
	}
	public void setImageField(boolean isImageField) {
		this.isImageField = isImageField;
	}
	public boolean isLogicField() {
		return isLogicField;
	}
	public boolean getIsLogicField() {
		return isLogicField;
	}
	 
	public LogicField getLogicField() {
		return logicField;
		
	}
	public void setLogicField(LogicField logicField) {
		this.logicField = logicField;
		this.isLogicField=logicField!=null;
	}
	public boolean isMulitiLine() {
		return isMulitiLine;
	}
	public boolean getIsMulitiLine() {
		return isMulitiLine;
	}
	public void setMulitiLine(boolean isMulitiLine) {
		this.isMulitiLine = isMulitiLine;
	}
	
}
