package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.generator.builder.view.config.DatePickerType;
import com.github.foxnic.sql.meta.DBField;

public class DateFieldConfig extends FieldConfig {

    public DateFieldConfig(DBField field) {
        super(field);
    }

    public String getFormat() {
        if(format==null) {
            return type.format();
        }
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

    private Boolean renderAtTop=false;
    public void setRenderAtTop(boolean renderTop) {
        this.renderAtTop=renderTop;
    }

    public Boolean getRenderAtTop() {
        return renderAtTop;
    }

    private DatePickerType type=DatePickerType.date;

    public String getType() {
        return type.name();
    }

    public void setType(DatePickerType type) {
        this.type = type;
    }

    private boolean range=false;
    public void setRange(boolean b) {
        this.range=b;
    }

    public Boolean getRange() {
        return range;
    }
}
