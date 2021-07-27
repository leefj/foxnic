package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormRadioOptions extends SubOptions {

    public FieldFormRadioOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 配置成字典模式，并设置字典代码
     * */
    public FieldFormRadioOptions dict(CodeTextEnum dict) {
        this.field.radioField().dict(dict);
        return this;
    }

    /**
     * 配置成枚举模式，并设置 CodeTextEnum 类型的枚举
     * */
    public FieldFormRadioOptions enumType(Class<? extends CodeTextEnum> enumType) {
        this.field.radioField().enumType(enumType);
        return this;
    }


}
