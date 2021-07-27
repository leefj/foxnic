package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormLogicOptions extends SubOptions {

    public FieldFormLogicOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 设置开启状(逻辑真)态下的标签与值
     * */
    public FieldFormLogicOptions on(String label, Object value) {
        this.field.logicField().on(label,value);
        return this;
    }

    /**
     * 设置关闭(逻辑假)状态下的标签与值
     * */
    public FieldFormLogicOptions off(String label,Object value) {
        this.field.logicField().off(label,value);
        return this;
    }

}
