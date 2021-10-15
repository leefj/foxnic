package com.github.foxnic.generator.builder.view.field.option;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.basic.FieldBasicOptions;
import com.github.foxnic.generator.builder.view.field.option.form.FieldFormOptions;
import com.github.foxnic.generator.builder.view.field.option.list.FieldListOptions;
import com.github.foxnic.generator.builder.view.field.option.toolbar.FieldSearchOptions;
import com.github.foxnic.generator.config.ModuleContext;

public class FieldOptions {

    private FieldInfo field;

    private FieldBasicOptions basic;
    private FieldFormOptions form;
    private FieldListOptions list;
    private FieldSearchOptions search;


    public FieldOptions(ModuleContext context, FieldInfo field) {
        this.field=field;
        this.basic=new FieldBasicOptions(this.field,this);
        this.form=new FieldFormOptions(context,this.field,this);
        this.search=new FieldSearchOptions(this.field,this);
        this.list=new FieldListOptions(context,this.field,this);

    }

    /**
     * 当前字段的基础默认显示配置
     * */
    public FieldBasicOptions basic() {
        return this.basic;
    }

    /**
     * 当前字段在表单中的显示配置
     * */
    public FieldFormOptions form() {
        return this.form;
    }

    /**
     * 当前字段在列表中的显示配置
     * */
    public FieldListOptions table() {
        return this.list;
    }

    /**
     * 当前字段在搜索栏的显示配置
     * */
    public FieldSearchOptions search() {
        return this.search;
    }



}
