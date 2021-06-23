package com.github.foxnic.generator.builder.view.field;

import com.github.foxnic.generator.builder.view.field.config.*;

public class TemplateFieldInfo extends FieldInfo {

	private FieldInfo source;
	
	public TemplateFieldInfo(FieldInfo fieldInfo) {
		super(fieldInfo);
		this.source=fieldInfo;
		this.isMulitiLine=fieldInfo.isMulitiLine;
		this.imageField=fieldInfo.imageField;
		this.logicField=fieldInfo.logicField;
		this.radioField=fieldInfo.radioField;

	}
 

	public boolean isAutoIncrease() {
		return this.getColumnMeta()==null? false : this.getColumnMeta().isAutoIncrease();
	}

	public boolean isPK() {
		return getColumnMeta()==null? false: getColumnMeta().isPK();
	}

	public boolean isHideInForm() {
		return source.isHideInForm;
	}

	public boolean isHideInList() {
		return source.isHideInList;
	}

	public boolean isRadioField() {
		return radioField!=null;
	}

	public boolean getIsRadioField() {
		return isRadioField();
	}

	public boolean isCheckField() {
		return source.checkField!=null;
	}

	public boolean getIsCheckField() {
		return isCheckField();
	}

	public boolean isSelectField() {
		return source.selectField!=null;
	}

	public boolean getIsSelectField() {
		return isSelectField();
	}

	public RadioBoxConfig getRadioField() {
		return radioField();
	}

	public CheckBoxConfig getCheckField() {
		return this.source.checkField;
	}

	public SelectBoxConfig getSelectField() {
		return source.selectField;
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

	public  boolean isDateField() {
 		return source.dateField!=null;
	}
	public  boolean getIsDateField() {
		return isDateField();
	}

	public boolean isLogicField() {
		return logicField!=null;
	}
	public boolean getIsLogicField() {
		return isLogicField();
	}

	public String getVarName() {
		return source.varName;
	}

	public String getLabel() {
		return source.label;
	}

	public boolean isImageField() {
		return imageField!=null;
	}
	public boolean getIsImageField() {
		return isImageField();
	}

	public ValidateConfig getValidate() {
		return source.validateConfig;
	}





}
