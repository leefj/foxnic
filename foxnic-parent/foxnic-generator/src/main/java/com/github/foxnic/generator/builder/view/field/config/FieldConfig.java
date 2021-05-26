package com.github.foxnic.generator.builder.view.field.config;

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
