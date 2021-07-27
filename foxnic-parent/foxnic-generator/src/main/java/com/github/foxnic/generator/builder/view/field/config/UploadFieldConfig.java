package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.Arrays;
import java.util.List;

public class UploadFieldConfig extends FieldConfig  {
 
	public UploadFieldConfig(DBField field) {
		super(field);
	}

	private int count =1;

	public int getCount() {
		return count;
	}

	/**
	 * 文件数量
	 * */
    public UploadFieldConfig maxFileCount(int count) {
		this.count=count;
		return this;
    }

    private String[] exts={};
	/**
	 * 允许上传的文件扩展名
	 * */
	public UploadFieldConfig acceptExts(String... exts) {
		this.exts=exts;
		return this;
	}

	private String[] mimes;
	/**
	 * 允许上传的 mime 类型
	 * */
	public UploadFieldConfig acceptMime(String... mimes) {
		this.mimes=mimes;
		return this;
	}

	private String fileType;

	/**
	 * 允许上传的 acceptType 类型
	 * */
	public UploadFieldConfig acceptAllType() {
		this.fileType="all";
		return this;
	}

	/**
	 * 允许上传的 acceptType 类型
	 * */
	public UploadFieldConfig acceptImageType() {
		this.fileType="image";
		return this;
	}

	/**
	 * 允许上传的 acceptType 类型
	 * */
	public UploadFieldConfig acceptAudioType() {
		this.fileType="audio";
		return this;
	}

	/**
	 * 允许上传的 acceptType 类型
	 * */
	public UploadFieldConfig acceptVideoType() {
		this.fileType="video";
		return this;
	}



	private boolean isAcceptSingleFile=false;

	/**
	 * 配置为单文件上传
	 * */
	public UploadFieldConfig acceptSingleFile() {
		this.acceptAllType();
		this.maxFileCount(1);
		isAcceptSingleFile=true;
		return this;
	}

	/**
	 * 配置为单图片上传
	 * */
	public UploadFieldConfig acceptSingleImage() {
		acceptSingleFile();
		this.acceptImageType();
		this.acceptMime("image/*");
		this.maxFileCount(1);
		this.displayFileName(false);
		this.acceptExts(IMAGE_EXTS.toArray(new String[0]));
		return this;
	}

	public boolean isAcceptSingleFile() {
		return isAcceptSingleFile;
	}

	private static List<String> IMAGE_EXTS= Arrays.asList("png","jpg","bmp","gif","jpeg");

	public boolean getIsImageOnly() {
		return isImageOnly();
	}

	public boolean isImageOnly() {
		boolean tag1=false;
		if(fileType!=null && "image".equals(fileType)) {
			tag1=true;
		}
		//
		boolean tag2=false;
		if(mimes!=null && mimes.length>0) {
			int z=0;
			for (String mime : mimes) {
				if(mime.trim().toLowerCase().startsWith("image/")) {
					z++;
				}
			}
			if(z==mimes.length) tag2=true;
		}
		//
		boolean tag3=false;
		if(exts!=null && exts.length>0) {
			int z=0;
			for (String ext : exts) {
				if(IMAGE_EXTS.contains(ext.trim().toLowerCase())) {
					z++;
				}
			}
			if(z==exts.length) tag3=true;
		}
		return tag1 && tag2 && tag3;
	}

	public  String getFileType(){
		return fileType;
	}

	public  Integer getMaxFileCount(){
		return count;
	}

	public String getAcceptMimes(String sep) {
		if(mimes==null || mimes.length==0) return null;
		return StringUtil.join(mimes,sep);
	}

	public String getAcceptExts(String sep) {
		if(exts==null || exts.length==0) return null;
		return StringUtil.join(exts,sep);
	}

	private boolean displayFileName=true;


	public boolean isDisplayFileName() {
		return displayFileName;
	}

	public boolean getDisplayFileName() {
		return displayFileName;
	}

	/**
	 * 是否显示上传的文件名
	 * */
	public UploadFieldConfig displayFileName(boolean displayFileName) {
		this.displayFileName = displayFileName;
		return this;
	}

	private String  buttonLabel="选择附件";

	/**
	 * 文件浏览按钮的显示文本
	 * */
	public UploadFieldConfig buttonLabel(String label) {
		this.buttonLabel=label;
		return this;
	}

	public  String getButtonLabel() {
		return  buttonLabel;
	}

}
