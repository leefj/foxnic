package com.github.foxnic.generatorV2.builder.view.field.config;

import com.github.foxnic.sql.meta.DBField;

public class FieldConfig {
	
	private DBField field;
	
	public FieldConfig(DBField field) {
		this.field=field;
	}
	
	public DBField getField() {
		return field;
	}

}
