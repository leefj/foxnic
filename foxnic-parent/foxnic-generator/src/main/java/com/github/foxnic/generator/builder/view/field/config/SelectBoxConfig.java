package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.sql.meta.DBField;

public class SelectBoxConfig  extends OptionFieldConfig<SelectBoxConfig> {
	private String queryApi;
	private  boolean muliti = false;
	private String fillBy="未指定";
	private boolean paging=false;


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

	public  SelectBoxConfig muliti(boolean m) {
		this.muliti=m;
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

	/**
	 * 指定用那个属性的数据填充下拉框的已选值
	 * */
    public SelectBoxConfig fillBy(String prop) {
    	this.fillBy=prop;
    	return this;
    }

	public String getFillBy() {
		return fillBy;
	}

    public SelectBoxConfig paging(boolean paging) {
    	this.paging=paging;
    	return this;
    }

	public boolean getPaging() {
		return paging;
	}
}
