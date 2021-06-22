package com.github.foxnic.generator.builder.view.field;

import com.github.foxnic.generator.builder.view.field.config.LogicFieldConfig;
import com.github.foxnic.generator.builder.view.field.config.RadioBoxConfig;

public class TemplateFieldInfo extends FieldInfo {
	
	//表单相关
	private String layVerifyHtml;
	private String requiredHtml;
	private String maxLenHtml;
	private String imageSrcHtml;
	
	//列表相关
	private String templet;

	private FieldInfo source;
	
	public TemplateFieldInfo(FieldInfo fieldInfo) {
		super(fieldInfo.getColumnMeta(),fieldInfo.isDBTreatyFiled());
		this.source=fieldInfo;
		this.isMulitiLine=fieldInfo.isMulitiLine;
		this.imageField=fieldInfo.imageField;
		this.logicField=fieldInfo.logicField;
		this.radioField=fieldInfo.radioField;

	}
 
	public String getLayVerifyHtml() {
		return layVerifyHtml;
	}
	public void setLayVerifyHtml(String layVerifyHtml) {
		this.layVerifyHtml = layVerifyHtml;
	}
	public String getRequiredHtml() {
		return requiredHtml;
	}
	public void setRequiredHtml(String requiredHtml) {
		this.requiredHtml = requiredHtml;
	}
	public String getMaxLenHtml() {
		return maxLenHtml;
	}
	public void setMaxLenHtml(String maxLenHtml) {
		this.maxLenHtml = maxLenHtml;
	}
	 
	public String getImageSrcHtml() {
		return imageSrcHtml;
	}
	public void setImageSrcHtml(String imageSrcHtml) {
		this.imageSrcHtml = imageSrcHtml;
	}
	
	public String getTemplet() {
		return templet;
	}

	public void setTemplet(String templet) {
		this.templet = templet;
	}

	public boolean isAutoIncrease() {
		return this.getColumnMeta().isAutoIncrease();
	}

	public boolean isPK() {
		return getColumnMeta().isPK();
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

	public RadioBoxConfig getRadioField() {
		return radioField();
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

}
