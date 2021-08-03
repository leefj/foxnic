package com.github.foxnic.generator.builder.view.field.option.toolbar;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldSearchOptions extends SubOptions {

    public FieldSearchOptions(FieldInfo field, FieldOptions top) {
        super(field,top);
    }

    /**
     * 设置标签，默认从数据库注释获取
     * */
    public FieldSearchOptions label(String label) {
        this.field.setLabelInSearch(label);
        return this;
    }

    /**
     * 设置是否在搜索栏隐藏当前字段
     * */
    public FieldSearchOptions hidden(boolean hidden) {
        this.field.hideInSearch(hidden);
        return this;
    }

    /**
     * 设置在搜索栏隐藏当前字段
     * */
    public FieldSearchOptions hidden() {
        this.field.hideInSearch(true);
        return this;
    }

    /**
     * 当前字段都独立呈现在搜索栏中，默认 true
     * */
    public FieldSearchOptions displayAlone(boolean displayAlone) {
        this.field.search().displayAlone(displayAlone);
        return this;
    }

    /**
     * 是否使用模糊搜索
     * */
    public FieldSearchOptions fuzzySearch(boolean fuzzy){
        this.field.search().setFuzzySearch(fuzzy);
        return this;
    }

    /**
     * 使用模糊搜索
     * */
    public FieldSearchOptions fuzzySearch(){
        this.field.search().setFuzzySearch(true);
        return this;
    }

    /**
     * 设置在搜索栏中的位置
     * @param row 行号，默认为 0
     * @param column 列序号
     * */
    public FieldSearchOptions location(int row,int column) {
        this.field.search().setRowIndex(row);
        this.field.search().setColumnIndex(column);
        return this;
    }

    /**
     * 设置在搜索栏中的位置，行号默认为 0
     * @param column 列序号
     * */
    public FieldSearchOptions location(int column) {
        this.field.search().setRowIndex(0);
        this.field.search().setColumnIndex(column);
        return this;
    }

//    /**
//     * 设置在搜索栏中的位置，跟随在指定输入框的后面
//     * @param field 列序号
//     * */
//    public FieldSearchOptions locationAfter(DBField field) {
//        this.field.search().setColumnAfter(field);
//        return this;
//    }


}
