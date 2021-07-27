package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBField;

public class ViewOptions {

    private ModuleContext context;
    //


    public ViewOptions(ModuleContext moduleContext) {
        this.context=moduleContext;
    }

    /**
     * 指定字段，开始配置字段在界面上的呈现
     * */
    public FieldOptions field(DBField field) {
        return this.context.field(field);
    }

    /**
     * 指定字段，开始配置字段在界面上的呈现
     * */
    public FieldOptions field(String field) {
        return this.context.field(field);
    }

    /**
     * 指定字段，开始配置字段在界面上的呈现
     * */
    public FormWindowOptions formWindow() {
        return new FormWindowOptions(this.context.getFormWindowConfig());
    }





}
