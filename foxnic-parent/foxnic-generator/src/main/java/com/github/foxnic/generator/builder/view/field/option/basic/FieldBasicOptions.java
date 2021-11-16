package com.github.foxnic.generator.builder.view.field.option.basic;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldBasicOptions extends SubOptions {


    public FieldBasicOptions(FieldInfo field, FieldOptions top) {
        super(field,top);
    }

    /**
     * 设置标签，默认从数据库注释获取
     * */
    public FieldBasicOptions label(String label) {
        this.field.label(label);
        return this;
    }

    /**
     * 设置在所有位置隐藏当前字段
     * */
    public FieldBasicOptions hidden() {
        if(this.field.getValidate()!=null) {
            this.field.getValidate().required(false);
        }
        return  this.hidden(true);
    }
    /**
     * 设置是否在所有位置隐藏当前字段
     * */
    public FieldBasicOptions hidden(boolean hidden) {
        this.field.hideInList(hidden);
        this.field.hideInForm(hidden);
        this.field.hideInSearch(hidden);
        if(hidden &&  this.field.getValidate()!=null) {
            this.field.getValidate().required(false);
        }
        return this;
    }


}
