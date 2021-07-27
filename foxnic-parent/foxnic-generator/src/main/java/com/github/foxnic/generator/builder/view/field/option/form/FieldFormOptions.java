package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormOptions extends SubOptions {

    private FieldFormValidateOptions validate;
    private FieldFormRadioOptions radio;
    private FieldFormSelectOptions select;
    private FieldFormUploadOptions upload;
    private FieldFormLogicOptions logic;

    public FieldFormOptions(FieldInfo field, FieldOptions top) {
        super(field,top);
        this.validate=new FieldFormValidateOptions(this.field,top);
        this.radio=new FieldFormRadioOptions(this.field,top);
        this.select=new FieldFormSelectOptions(this.field,top);
        this.upload=new FieldFormUploadOptions(this.field,top);
        this.logic=new FieldFormLogicOptions(this.field,top);
    }

    /**
     * 设置标签，默认从数据库注释获取
     * */
    public FieldFormOptions label(String label) {
        this.field.setLabelInForm(label);
        return this;
    }

    /**
     * 设置是否在表单隐藏当前字段
     * */
    public FieldFormOptions hidden(boolean hidden) {
        this.field.hideInForm(hidden);
        return this;
    }

    /**
     * 设置在表单隐藏当前字段
     * */
    public FieldFormOptions hidden() {
        this.field.hideInForm(true);
        return this;
    }

    /**
     * 设置设置当前字段的校验逻辑
     * */
    public FieldFormValidateOptions validate() {
        return this.validate;
    }

    /**
     * 设置当前字段为单选框
     * */
    public FieldFormRadioOptions radio() {
        return radio;
    }

    /**
     * 设置当前字段为下拉框
     * */
    public FieldFormSelectOptions select() {
        return select;
    }

    /**
     * 设置当前字段为文件上传
     * */
    public FieldFormUploadOptions upload() {
        return upload;
    }

    /**
     * 设置当前字段为逻辑字段
     * */
    public FieldFormLogicOptions logic() {
        return logic;
    }
}
