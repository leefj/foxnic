package com.github.foxnic.generator.builder.view.field;

public class TemplateFieldInfo extends FieldInfo {
	
	//表单相关
	private String layVerifyHtml;
	private String requiredHtml;
	private String maxLenHtml;
	private String imageSrcHtml;
	
	//列表相关
	private String templet;
	
	
	public TemplateFieldInfo(FieldInfo fieldInfo) {
		super(fieldInfo.getColumnMeta(),fieldInfo.isDBTreatyFiled());
		
		this.isMulitiLine=fieldInfo.isMulitiLine;
		this.imageField=fieldInfo.imageField;
		this.logicField=fieldInfo.logicField;
 
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

}
