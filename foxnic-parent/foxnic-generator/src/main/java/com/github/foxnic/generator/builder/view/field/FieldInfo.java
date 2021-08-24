package com.github.foxnic.generator.builder.view.field;

import com.alibaba.fastjson.JSONArray;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.generator.builder.view.field.config.*;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBDataType;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

import java.util.ArrayList;
import java.util.List;

public class FieldInfo {

	private InputType type=InputType.TEXT_INPUT;

	public String getTypeName() {
		return type.name().toLowerCase();
	}

	public InputType getType() {
		return type;
	}

	private DBColumnMeta columnMeta;
	private DBField dbField;

	private String column;
	private String varName;
	private String labelInForm;
	private String labelInList;
	private String labelInSearch;
	private boolean isDBTreatyFiled=false;
	private ValidateConfig validateConfig=null;
	private String alignInList;

	//
	private TextInputConfig textField;
	private UploadFieldConfig uploadField;
	private LogicFieldConfig logicField;
	private RadioBoxConfig  radioField;
	private CheckBoxConfig  checkField;
	private SelectBoxConfig  selectField;
	private DateFieldConfig dateField;

	public ButtonFieldConfig getButtonField() {
		return buttonField;
	}

	private ButtonFieldConfig buttonField;


	private TextAreaConfig textArea;
	private NumberInputConfig numberField;
	private SearchConfig search=null;

	//
	public FieldInfo(ModuleContext context,String field) {
		init4String(field);
		search=new SearchConfig(this,context.getSearchAreaConfig());
	}



	public FieldInfo(ModuleContext context,FieldInfo fieldInfo) {
		if(fieldInfo.getColumnMeta()==null) {
			init4String(fieldInfo.column);
		} else {
			init4DB(fieldInfo.getColumnMeta(), fieldInfo.isDBTreatyFiled());
		}
		search=new SearchConfig(this,context.getSearchAreaConfig());
	}

	public FieldInfo(ModuleContext context,DBColumnMeta columnMeta, boolean isDBTreatyFiled) {
		init4DB(columnMeta,isDBTreatyFiled);
		search=new SearchConfig(this,context.getSearchAreaConfig());
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


		if(columnMeta.getDBDataType()==DBDataType.TIMESTAME
				|| columnMeta.getDBDataType()==DBDataType.DATE)
		{
			this.dateField();
		}
		else if(columnMeta.getDBDataType()==DBDataType.INTEGER
				|| columnMeta.getDBDataType()==DBDataType.BIGINT)
		{
			this.numberField().integer();
		}
		else if(columnMeta.getDBDataType()==DBDataType.DECIMAL
				|| columnMeta.getDBDataType()==DBDataType.BIGINT)
		{
			this.numberField().decimal();
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


	public TextInputConfig getTextField() {
		return textField;
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
		this.type=InputType.UPLOAD;
		return uploadField;
	}

	/**
	 * 配置字段为逻辑字段
	 * */
	public LogicFieldConfig logicField() {
		if(logicField==null) {
			logicField=new LogicFieldConfig(dbField);
		}
		this.type=InputType.LOGIC_SWITCH;
		return logicField;
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
		this.type=InputType.RADIO_BOX;
		return radioField;
	}

	/**
	 * 配置字段为复选框
	 * */
	public CheckBoxConfig checkField() {
		if(checkField==null) checkField=new CheckBoxConfig(this.dbField);
		this.type=InputType.CHECK_BOX;
		return checkField;
	}

	/**
	 * 配置字段为日期选择
	 * */
	public DateFieldConfig dateField() {
		if(dateField==null) dateField=new DateFieldConfig(this.dbField);
		this.type=InputType.DATE_INPUT;
		return dateField;
	}

	/**
	 * 配置字段为日期选择
	 * */
	public ButtonFieldConfig buttonField() {
		if(buttonField==null) buttonField=new ButtonFieldConfig(this);
		this.type=InputType.BUTTON;
		return buttonField;
	}


	public void setRadioField(RadioBoxConfig radioField) {
		this.radioField = radioField;
	}


	private boolean isHideInForm=false;
	private boolean isHideInList=false;
	private boolean isHideInSearch=false;

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
		this.type=InputType.SELECT_BOX;
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
		return  search;
	}


	public boolean isAutoIncrease() {
		return this.getColumnMeta()==null? false : this.getColumnMeta().isAutoIncrease();
	}

	public boolean isPK() {
		return getColumnMeta()==null? false: getColumnMeta().isPK();
	}

	public boolean isHideInForm() {
		return this.isHideInForm;
	}

	public boolean getIsHideInForm() {
		return isHideInForm();
	}

	public boolean isHideInList() {
		return this.isHideInList;
	}

	public boolean getIsHideInList() {
		return isHideInList();
	}


	public boolean isHideInSearch() {
		return this.isHideInSearch;
	}

	public boolean getIsHideInSearch() {
		return   isHideInSearch();
	}

	public RadioBoxConfig getRadioField() {
		return radioField();
	}

	public CheckBoxConfig getCheckField() {
		return this.checkField;
	}

	public SelectBoxConfig getSelectField() {
		return this.selectField;
	}

	public LogicFieldConfig getLogicField() {
		return logicField();
	}

	public String getVarName() {
		return this.varName;
	}

	public ValidateConfig getValidate() {
		return this.validateConfig;
	}


	public UploadFieldConfig getUploadField() {
		return uploadField;
	}

	public String getAlignInList() {
		return this.alignInList;
	}

	public SearchConfig getSearch() {
		return  this.search;
	}

	public TextAreaConfig getTextArea() {
		return textArea;
	}

	public TextAreaConfig textArea() {
		if(textArea==null) textArea=new TextAreaConfig();
		this.type=InputType.TEXT_AREA;
		return textArea;
	}

	public NumberInputConfig numberField() {
		if(numberField==null) numberField=new NumberInputConfig();
		this.type=InputType.NUMBER_INPUT;
		return numberField;
	}

	public NumberInputConfig getNumberInput() {
		return  numberField;
	}

	public int getFormLayoutIndex() {
		return formLayoutIndex;
	}

	public void setFormLayoutIndex(int formLayoutIndex) {
		this.formLayoutIndex = formLayoutIndex;
	}

	private int formLayoutIndex=-1;

	public int getListLayoutIndex() {
		return listLayoutIndex;
	}

	private int listLayoutIndex= Integer.MAX_VALUE;
    public void setListLayoutIndex(int index) {
    	this.listLayoutIndex=index;
    }



	private boolean sortInList=true;

	public void sortInList(boolean sort) {
		this.sortInList=sort;
	}

	public boolean getSortInList() {
		return sortInList;
	}



	private boolean fixInList=false;

	public void fixInList(boolean fix) {
		this.fixInList=fix;
	}

	public boolean getFixInList() {
		return fixInList;
	}

	public JSONArray getListFillByPropertyNames() {
		return listFillByPropertyNames;
	}

	private JSONArray listFillByPropertyNames;

	public void setListFillByPropertyNames(String[] propertyName) {
		this.listFillByPropertyNames=new JSONArray();
		for (String prop : propertyName) {
			this.listFillByPropertyNames.add(prop);
		}
	}

	public static class JoinPropertyConst {
		private String lable;
		private String constName;
		public JoinPropertyConst(String constName,String lable) {
			this.constName=constName;
			this.lable=lable;
		}
		public String getLabel() {
			return lable;
		}
		public String getConstName() {
			return constName;
		}
	}

	public List<JoinPropertyConst> getQueryJoinPropertyConstNames() {
		List<JoinPropertyConst> list=new ArrayList<>();
		String na=null;
		if(this.listFillByPropertyNames!=null && this.listFillByPropertyNames.size()>0){
			na=this.listFillByPropertyNames.getString(0);
			na=BeanNameUtil.instance().depart(na).toUpperCase();
			list.add(new JoinPropertyConst(na,this.getLabelInList()));
		}

		if(this.getType()==InputType.SELECT_BOX && this.selectField.getQueryApi()!=null){
			na=this.selectField.getFillByConstName();
			if(na!=null) {
				list.add(new JoinPropertyConst(na,this.getLabelInList()));
			}
		}
		return list;
 	}

	private boolean disableInList=false;


	public boolean getDisableInList() {
		return disableInList;
	}

	public void setDisableInList(boolean disableInList) {
		this.disableInList = disableInList;
	}


	private  boolean isFormElem=false;

	/**
	 * 是否在表单元素内
	 * */
	public boolean getIsFormElem() {
		return isFormElem;
	}

	public void setFormElem(boolean formElem) {
		isFormElem = formElem;
	}


}
