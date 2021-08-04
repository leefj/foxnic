package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormValidateOptions extends SubOptions {

    public FieldFormValidateOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 必填项
     * */
    public FieldFormValidateOptions required() {
        this.field.validate().required();
        return this;
    }

    /**
     * 手机号
     * */
    public FieldFormValidateOptions phone() {
        this.field.validate().phone();
        return this;
    }

    /**
     * 邮箱
     * */
    public FieldFormValidateOptions email() {
        this.field.validate().email();
        return this;
    }
    /**
     * URL地址
     * */
    public FieldFormValidateOptions url() {
        this.field.validate().url();
        return this;
    }

    /**
     * 日期
     * */
    public FieldFormValidateOptions date() {
        this.field.validate().date();
        return this;
    }

    /**
     * 身份证
     * */
    public FieldFormValidateOptions identity() {
        this.field.validate().identity();
        return this;
    }



}
