package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.sql.meta.DBField;

public class DateFieldConfig extends FieldConfig {

    public DateFieldConfig(DBField field) {
        super(field);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    private String format="yyyy-MM-dd HH:mm:ss";

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    private String defaultValue;

}
