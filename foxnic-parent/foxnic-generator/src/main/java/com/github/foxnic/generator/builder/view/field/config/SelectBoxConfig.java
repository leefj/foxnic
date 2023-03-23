package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

public class SelectBoxConfig  extends OptionFieldConfig<SelectBoxConfig> {
	private String queryApi;

	private String queryApi4Search;
	private  boolean muliti = false;
	private String fillWith =null;

	public String getFillByConstName() {
		return fillByConstName;
	}

	private String fillByConstName=null;
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


	/**
	 * 配置为是否多选
	 * */
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

	public String getQueryApi4Search() {
		if(StringUtil.isBlank(queryApi4Search)) {
			return queryApi;
		}
		return queryApi4Search;
	}

	public void queryApi4Search(String queryApi4Search) {
		this.queryApi4Search = queryApi4Search;
	}

	/**
	 * 指定用那个属性的数据填充下拉框的已选值
	 * */
    public SelectBoxConfig fillWith(String prop) {
    	this.fillWith =prop;
    	this.fillByConstName= BeanNameUtil.instance().depart(this.fillWith).toUpperCase();
    	return this;
    }

	public String getFillWith() {
		return fillWith;
	}

	/**
	 * 是否分页
	 * */
    public SelectBoxConfig paging(boolean paging) {
    	this.paging=paging;
    	return this;
    }

	public boolean getPaging() {
		return paging;
	}

	public Integer getSize() {
		return size;
	}

	private Integer size=10;

	public void size(int size) {
		this.size=size;
	}

	private  String valueField="undefiled";
	private  String textField="undefiled";

	public String getValueField() {
		return valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField;
	}

	public String getTextField() {
		return textField;
	}

	public void setTextField(String textField) {
		this.textField = textField;
	}

	private boolean toolbar=true;
	private boolean filter=true;

	public boolean getToolbar() {
		return toolbar;
	}

	public void setToolbar(boolean toolbar) {
		this.toolbar = toolbar;
	}

	public boolean getFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}


    public void validate() {
		if(this.fillWith !=null) {
			if(this.getDictCode()!=null) {
				throw new IllegalArgumentException("不允许同时指定 fillBy 和 dict ");
			}
			if(this.getEnumTypeName()!=null) {
				throw new IllegalArgumentException("不允许同时指定 fillBy 和 enum ");
			}
		}

    }


}
