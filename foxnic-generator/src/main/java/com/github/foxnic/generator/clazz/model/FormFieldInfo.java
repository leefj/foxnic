package com.github.foxnic.generator.clazz.model;

public class FormFieldInfo extends FieldInfo {
	
	private String layVerifyHtml;
	private String requiredHtml;
	private String maxLenHtml;
	private String imageSrcHtml;
	private boolean displayImageUpload=false;
	
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
	public boolean isDisplayImageUpload() {
		return displayImageUpload;
	}
	public boolean getDisplayImageUpload() {
		return displayImageUpload;
	}
	public void setDisplayImageUpload(boolean displayImageUpload) {
		this.displayImageUpload = displayImageUpload;
	}
	public String getImageSrcHtml() {
		return imageSrcHtml;
	}
	public void setImageSrcHtml(String imageSrcHtml) {
		this.imageSrcHtml = imageSrcHtml;
	}

}
