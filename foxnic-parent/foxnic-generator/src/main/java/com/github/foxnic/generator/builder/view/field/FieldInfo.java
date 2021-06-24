package com.github.foxnic.generator.builder.view.field;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.view.field.config.*;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

public class FieldInfo {



	protected DBColumnMeta columnMeta;
	protected DBField dbField;

	protected String column;
	protected String varName;
	protected String label;
	protected boolean isDBTreatyFiled=false;
	protected ValidateConfig validateConfig=null;
	//
	protected ImageFieldConfig imageField;
	protected LogicFieldConfig logicField;
	protected RadioBoxConfig  radioField;
	protected CheckBoxConfig  checkField;
	protected SelectBoxConfig  selectField;
	protected DateFieldConfig dateField;
	protected boolean isMulitiLine=false;
	//
	public FieldInfo(String field) {
		init4String(field);
	}



	public FieldInfo(FieldInfo fieldInfo) {
		if(fieldInfo.getColumnMeta()==null) {
			init4String(fieldInfo.column);
		} else {
			init4DB(fieldInfo.getColumnMeta(), fieldInfo.isDBTreatyFiled());
		}
	}

	public FieldInfo(DBColumnMeta columnMeta,boolean isDBTreatyFiled) {
		init4DB(columnMeta,isDBTreatyFiled);
	}

	private void init4String(String field) {
		this.column=field;
		this.label=field;
		this.varName= BeanNameUtil.instance().getPropertyName(field);
		this.isDBTreatyFiled=false;
	}

	private void init4DB(DBColumnMeta columnMeta, boolean isDBTreatyFiled) {
		this.columnMeta = columnMeta;
		this.column = columnMeta.getColumn();
		this.label = columnMeta.getLabel();
		this.varName = columnMeta.getColumnVarName();
		this.dbField = DBTable.getDBTable(columnMeta.getTable()).getField(this.column);
		if(columnMeta.getDBDataType()== DBDataType.DATE || columnMeta.getDBDataType()==DBDataType.TIMESTAME) {
			this.dateField=new DateFieldConfig(this.dbField);
		}

		this.isDBTreatyFiled=isDBTreatyFiled;
	}

	/**
	 * 设置标签，默认从数据库注释获取
	 * */
	public FieldInfo label(String text) {
		this.label=text;
		return this;
	}

	
	public ImageFieldConfig imageField() {
		if(imageField==null) {
			imageField=new ImageFieldConfig(dbField);
		}
		return imageField;
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
		if(radioField==null) radioField=new RadioBoxConfig(this.dbField);
		return radioField;
	}

	public CheckBoxConfig checkField() {
		if(checkField==null) checkField=new CheckBoxConfig(this.dbField);
		return checkField;
	}

	public DateFieldConfig dateField() {
		if(dateField==null) dateField=new DateFieldConfig(this.dbField);
		return dateField;
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

	/**
	 * 获得用于配置验证信息的对象
	 * */
	public ValidateConfig validate() {
    	if(validateConfig==null) validateConfig=new ValidateConfig();
		return validateConfig;
	}

	/**
	 * 获得用于配置验证信息的对象
	 * */
	public SelectBoxConfig selectField() {
		if(selectField==null) selectField=new SelectBoxConfig(this.dbField);
		return selectField;
	}
}
