package com.github.foxnic.generator.builder.view.field;

import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.view.field.config.ImageFieldConfig;
import com.github.foxnic.generator.builder.view.field.config.LogicFieldConfig;
import com.github.foxnic.generator.builder.view.field.config.RadioBoxConfig;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class FieldInfo {

	private DBColumnMeta columnMeta;
	private DBField dbField;
	
	private String column;
	private String varName;
	private String label;
	private boolean isDBTreatyFiled=false;
	//
	protected ImageFieldConfig imageField;
	protected LogicFieldConfig logicField;
	protected RadioBoxConfig  radioField;
	protected boolean isMulitiLine=false;
	//
	public FieldInfo(DBColumnMeta columnMeta,boolean isDBTreatyFiled) {
		this.column=columnMeta.getColumn();
		this.columnMeta=columnMeta;
		this.label=columnMeta.getLabel();
		this.varName=columnMeta.getColumnVarName();
		this.dbField=DBTable.getDBTable(columnMeta.getTable()).getField(this.column);
		this.isDBTreatyFiled=isDBTreatyFiled;
	}
	
	
	public String getVarName() {
		return varName;
	}
	 
	public String getLabel() {
		return label;
	}
 
	public boolean isImageField() {
		return imageField!=null;
	}
	public boolean getIsImageField() {
		return isImageField();
	}
	
	public FieldInfo imageField() {
		if(imageField==null) {
			imageField=new ImageFieldConfig(dbField);
		}
		return this;
	}
	
	public boolean isLogicField() {
		return logicField!=null;
	}
	public boolean getIsLogicField() {
		return isLogicField();
	}
	
	
	public LogicFieldConfig logicField() {
		if(logicField==null) {
			logicField=new LogicFieldConfig(dbField);
		}
		return logicField;
		
	}
	
	public LogicFieldConfig getLogicField() {
		return logicField();
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


	public DBColumnMeta getColumnMeta() {
		return columnMeta;
	}

	public boolean isDBTreatyFiled() {
		return isDBTreatyFiled;
	}

	 


	public String getColumn() {
		return column;
	}
	
	
	
	public boolean isRadioField() {
		return radioField!=null;
	}
	public boolean getIsRadioField() {
		return isRadioField();
	}


	public RadioBoxConfig getRadioField() {
		return radioField();
	}
	
	public RadioBoxConfig radioField() {
		if(radioField==null) radioField=new RadioBoxConfig();
		return radioField;
	}


	public void setRadioField(RadioBoxConfig radioField) {
		this.radioField = radioField;
	}
	
	
	
}
