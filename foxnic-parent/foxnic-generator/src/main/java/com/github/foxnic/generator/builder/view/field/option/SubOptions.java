package com.github.foxnic.generator.builder.view.field.option;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.form.FieldFormOptions;
import com.github.foxnic.generator.builder.view.field.option.list.FieldListOptions;
import com.github.foxnic.generator.builder.view.field.option.toolbar.FieldSearchOptions;

public abstract class SubOptions {

    protected FieldInfo field;
    protected FieldOptions top;

    public SubOptions(FieldInfo field,FieldOptions top) {
        this.top=top;
        this.field=field;
    }

    /**
     * 返回当前字段是 search 配置
     * */
    public FieldSearchOptions search() {
        return top.search();
    }

    /**
     * 返回当前字段是 form 配置
     * */
    public FieldFormOptions form() {
        return top.form();
    }

    /**
     * 返回当前字段是 list 配置
     * */
    public FieldListOptions list() {
        return top.list();
    }

}
