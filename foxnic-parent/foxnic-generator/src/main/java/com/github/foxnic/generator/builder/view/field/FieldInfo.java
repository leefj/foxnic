package com.github.foxnic.generator.builder.view.field;

import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.view.field.config.ImageFieldConfig;
import com.github.foxnic.generator.builder.view.field.config.LogicFieldConfig;
import com.github.foxnic.generator.builder.view.field.config.RadioBoxConfig;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class FieldInfo {

	protected DBColumnMeta columnMeta;
	protected DBField dbField;

	protected String column;
	protected String varName;
	protected String label;
	protected boolean isDBTreatyFiled=false;
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
	
	

	
	public FieldInfo imageField() {
		if(imageField==null) {
			imageField=new ImageFieldConfig(dbField);
		}
		return this;
	}

	public LogicFieldConfig logicField() {
		if(logicField==null) {
			logicField=new LogicFieldConfig(dbField);
		}
		return logicField;
	}
	

	
	public FieldInfo mulitiLine() {
		this.isMulitiLine = true;
		return this;
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

	public RadioBoxConfig radioField() {
		if(radioField==null) radioField=new RadioBoxConfig();
		return radioField;
	}


	public void setRadioField(RadioBoxConfig radioField) {
		this.radioField = radioField;
	}


	protected boolean isHideInForm;
	protected boolean isHideInList;

    public FieldInfo hideInForm() {
		isHideInForm=true;
		return this;
    }

	public FieldInfo hideInList() {
		isHideInList=true;
    	return this;
	}
}
