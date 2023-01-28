package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormCheckOptions extends SubOptions {

    public FieldFormCheckOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 配置成字典模式，并设置字典代码
     * */
    public FieldFormCheckOptions dict(CodeTextEnum dict) {
        this.field.checkField().dict(dict);
        return this;
    }

    /**
     * 配置成枚举模式，并设置 CodeTextEnum 类型的枚举
     * */
    public FieldFormCheckOptions enumType(Class<? extends CodeTextEnum> enumType) {
        this.field.checkField().enumType(enumType);
        return this;
    }

    /**
     * 设置默认值
     * */
    public FieldFormCheckOptions defaultValue(Object... value) {
        this.field.checkField().setDefaultValue(value);
        return this;
    }

    /**
     * 设置默认选中的序号
     * */
    public FieldFormCheckOptions defaultIndex(Integer... value) {
        this.field.checkField().setDefaultIndex(value);
        return this;
    }

    public FieldFormCheckOptions bindVar(String varName) {
        this.field.checkField().bindVar(varName);
        return this;
    }
}
