package com.github.foxnic.generatorV2.builder.view.model;

import com.github.foxnic.dao.meta.DBColumnMeta;

public class ListFieldInfo extends FieldInfo {

	private String templet;
	
	public ListFieldInfo(DBColumnMeta cm) {
		super(cm);
	}

	
	public String getTemplet() {
		return templet;
	}

	public void setTemplet(String templet) {
		this.templet = templet;
	}
	
}
