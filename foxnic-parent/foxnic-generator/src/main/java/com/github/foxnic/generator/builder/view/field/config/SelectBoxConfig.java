package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.sql.meta.DBField;

public class SelectBoxConfig  extends OptionFieldConfig<SelectBoxConfig> {
	private String queryApi;
	private  boolean muliti = false;



	public SelectBoxConfig(DBField field) {
		super(field);
	}

	/**
	 * 指定取数的 API 地址
	 * */
	public SelectBoxConfig queryApi(String api) {
		this.queryApi=api;
		return this;
	}

	public  SelectBoxConfig muliti() {
		this.muliti=true;
		return this;
	}

	protected  void clear() {
		this.queryApi = null;
	 	super.clear();
	}

	public boolean getMuliti() {
		return  muliti;
	}

	public String getQueryApi() {
		return this.queryApi;
	}

}
