package com.github.foxnic.generator.builder.view.field.option.list;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldListOptions extends SubOptions {


    public FieldListOptions(FieldInfo field, FieldOptions top) {
        super(field,top);
    }

    /**
     * 设置标签，默认从数据库注释获取
     * */
    public FieldListOptions label(String label) {
        this.field.setLabelInList(label);
        return this;
    }

    /**
     * 设置是否在列表中隐藏当前字段
     * */
    public FieldListOptions hidden(boolean hidden) {
        this.field.hideInList(hidden);
        return this;
    }

    /**
     * 设置在列表中隐藏当前字段
     * */
    public FieldListOptions hidden() {
        this.field.hideInList(true);
        return this;
    }


    /**
     * 使字段在列表中左对齐
     * */
    public  FieldListOptions alignLeft() {
         this.field.alignLeftInList();
        return this;
    }

    /**
     * 使字段在列表中右对齐
     * */
    public  FieldListOptions alignRight() {
        this.field.alignRightInList();
        return this;
    }

    /**
     * 使字段在列表中居中对齐
     * */
    public  FieldListOptions alignCenter() {
        this.field.alignCenterInList();
        return this;
    }

    /**
     * 设置是否可排序，默认可排序
     * */
    public FieldListOptions sort(boolean sort) {
        this.field.sortInList(sort);
        return this;
    }

    /**
     * 设置列锁定，默认不锁定
     * */
    public FieldListOptions fix(boolean fix) {
        this.field.fixInList(fix);
        return this;
    }
}
