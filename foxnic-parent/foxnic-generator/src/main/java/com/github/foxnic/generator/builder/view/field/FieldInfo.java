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
	protected String labelInForm;
	protected String labelInList;
	protected String labelInSearch;
	protected boolean isDBTreatyFiled=false;
	protected ValidateConfig validateConfig=null;
	protected String alignInList;
	//
	protected UploadFieldConfig uploadField;
	protected LogicFieldConfig logicField;
	protected RadioBoxConfig  radioField;
	protected CheckBoxConfig  checkField;
	protected SelectBoxConfig  selectField;
	protected DateFieldConfig dateField;
	protected SearchConfig search;
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
		this.label(field);
		this.varName= BeanNameUtil.instance().getPropertyName(field);
		this.isDBTreatyFiled=false;
	}

	private void init4DB(DBColumnMeta columnMeta, boolean isDBTreatyFiled) {
		this.columnMeta = columnMeta;
		this.column = columnMeta.getColumn();
		this.label (columnMeta.getLabel());
		this.varName = columnMeta.getColumnVarName();
		this.dbField = DBTable.getDBTable(columnMeta.getTable()).getField(this.column);
		if(columnMeta.getDBDataType()== DBDataType.DATE || columnMeta.getDBDataType()==DBDataType.TIMESTAME) {
			this.dateField=new DateFieldConfig(this.dbField);
		}

		this.isDBTreatyFiled=isDBTreatyFiled;

		if(columnMeta.getDBDataType()==DBDataType.TIMESTAME
		|| columnMeta.getDBDataType()==DBDataType.DATE
				|| columnMeta.getDBDataType()==DBDataType.TIME
				|| columnMeta.getDBDataType()==DBDataType.BIGINT
				|| columnMeta.getDBDataType()==DBDataType.BOOL
				|| columnMeta.getDBDataType()==DBDataType.BYTES
				|| columnMeta.getDBDataType()==DBDataType.DECIMAL
				|| columnMeta.getDBDataType()==DBDataType.DOUBLE
				|| columnMeta.getDBDataType()==DBDataType.INTEGER
				|| columnMeta.getDBDataType()==DBDataType.LONG
		) {
			this.alignRightInList();
		} else if(columnMeta.getDBDataType()==DBDataType.STRING
				|| columnMeta.getDBDataType()==DBDataType.CLOB) {
			this.alignLeftInList();
		} else if(columnMeta.getDBDataType()==DBDataType.OBJECT
				|| columnMeta.getDBDataType()==DBDataType.BLOB
				) {
			this.alignCenterInList();
		}

	}

	/**
	 * 设置标签，默认从数据库注释获取
	 * */
	public FieldInfo label(String text) {
		this.setLabelInForm(text);
		this.setLabelInList(text);
		this.setLabelInSearch(text);
		return this;
	}

	public String getLabelInForm() {
		return this.labelInForm ;
	}

	public void setLabelInForm(String labelInForm) {
		this.labelInForm = labelInForm;
	}

	public String getLabelInList() {
		return this.labelInList ;
	}

	public void setLabelInList(String labelInList) {
		this.labelInList = labelInList;
	}

	public String getLabelInSearch() {
		return this.labelInSearch ;
	}

	public void setLabelInSearch(String labelInSearch) {
		this.labelInSearch = labelInSearch;
	}

	/**
	 * 配置字段为文件上传
	 * */
	public UploadFieldConfig uploadField() {
		if(uploadField ==null) {
			uploadField =new UploadFieldConfig(dbField);
		}
		return uploadField;
	}

	/**
	 * 配置字段为逻辑字段
	 * */
	public LogicFieldConfig logicField() {
		if(logicField==null) {
			logicField=new LogicFieldConfig(dbField);
		}
		return logicField;
	}


	/**
	 * 配置字段为多行文本
	 * */
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

	/**
	 * 配置字段为单选框
	 * */
	public RadioBoxConfig radioField() {
		if(radioField==null) radioField=new RadioBoxConfig(this.dbField);
		return radioField;
	}

	/**
	 * 配置字段为复选框
	 * */
	public CheckBoxConfig checkField() {
		if(checkField==null) checkField=new CheckBoxConfig(this.dbField);
		return checkField;
	}

	/**
	 * 配置字段为日期选择
	 * */
	public DateFieldConfig dateField() {
		if(dateField==null) dateField=new DateFieldConfig(this.dbField);
		return dateField;
	}


	public void setRadioField(RadioBoxConfig radioField) {
		this.radioField = radioField;
	}


	protected boolean isHideInForm;
	protected boolean isHideInList;
	protected boolean isHideInSearch;

	/**
	 * 使字段不在表单中显示
	 * */
    public FieldInfo hideInForm() {
		isHideInForm=true;
		return this;
    }

	/**
	 * 使字段不在表单中显示
	 * */
	public FieldInfo hideInForm(boolean b) {
		isHideInForm=b;
		return this;
	}

    /**
	 * 使字段不在列表中显示
	 * */
	public FieldInfo hideInList() {
		isHideInList=true;
    	return this;
	}

	/**
	 * 使字段不在列表中显示
	 * */
	public FieldInfo hideInList(boolean b) {
		isHideInList=b;
		return this;
	}

	/**
	 * 使字段不在搜索中显示
	 * */
	public FieldInfo hideInSearch() {
		isHideInSearch=true;
		return this;
	}

	/**
	 * 使字段不在搜索中显示
	 * */
	public FieldInfo hideInSearch(boolean b) {
		isHideInSearch=b;
		return this;
	}



	/**
	 * 进入校验配置，获得用于配置验证信息的对象
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

	/**
	 * 使字段在列表中左对齐
	 * */
	public  FieldInfo alignLeftInList() {
		this.alignInList ="left";
		return this;
	}

	/**
	 * 使字段在列表中右对齐
	 * */
	public  FieldInfo alignRightInList() {
		this.alignInList ="right";
		return this;
	}

	/**
	 * 使字段在列表中居中对齐
	 * */
	public  FieldInfo alignCenterInList() {
		this.alignInList ="center";
		return this;
	}

	/**
	 * 配置搜索
	 * */
	public  SearchConfig search() {
		if(search==null) search=new SearchConfig();
		return  search;
	}
}
