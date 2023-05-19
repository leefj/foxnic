package com.github.foxnic.generator.builder.view.field.option.toolbar;

import com.github.foxnic.api.query.MatchType;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.InputType;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;
import com.github.foxnic.sql.meta.DBField;

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

//    /**
//     * 当前字段都独立呈现在搜索栏中，默认 true
//     * */
//    public FieldSearchOptions displayAlone(boolean displayAlone) {
//        this.field.search().displayAlone(displayAlone);
//        return this;
//    }

    /**
     * 是否使用模糊搜索
     * @param valuePrefix  为搜索值自动加入前缀
     * @param valueSuffix  为搜索值自动加入后缀
     * */
    public FieldSearchOptions fuzzySearch(boolean fuzzy,String valuePrefix,String valueSuffix,Boolean splitValue){
        this.field.search().setFuzzySearch(fuzzy,valuePrefix,valueSuffix,splitValue);
        return this;
    }

    /**
     * 是否使用模糊搜索
     * @param valuePrefix  为搜索值自动加入前缀
     * @param valueSuffix  为搜索值自动加入后缀
     * */
    public FieldSearchOptions fuzzySearch(boolean fuzzy,String valuePrefix,String valueSuffix){
        this.field.search().setFuzzySearch(fuzzy,valuePrefix,valueSuffix,false);
        return this;
    }

    /**
     * 使用模糊搜索
     * */
    public FieldSearchOptions fuzzySearch(Boolean splitValue) {
        this.field.search().setFuzzySearch(true,null,null,splitValue);
        return this;
    }

    /**
     * 使用模糊搜索
     * */
    public FieldSearchOptions fuzzySearch() {
        this.field.search().setFuzzySearch(true,null,null,false);
        return this;
    }

    /**
     * 使用模糊搜索，并为搜索值自动加入双引号作为前后缀<br/>
     * 一般用于多选，且数据以JSON Array 格式存储于字段的情况
     * */
    public FieldSearchOptions fuzzySearchWithDoubleQM() {
        this.field.search().setFuzzySearch(true,"\\\"","\\\"",false);
        return this;
    }

//    /**
//     * 设置在搜索栏中的位置
//     * @param row 行号，默认为 0
//     * @param column 列序号
//     * */
//    public FieldSearchOptions location(int row,int column) {
//        this.field.search().setRowIndex(row);
//        this.field.search().setColumnIndex(column);
//        return this;
//    }

//    /**
//     * 设置在搜索栏中的位置，行号默认为 0
//     * @param column 列序号
//     * */
//    public FieldSearchOptions location(int column) {
//        this.field.search().setRowIndex(0);
//        this.field.search().setColumnIndex(column);
//        return this;
//    }

    /**
     * 设置搜索输入框宽度
     * */
    public FieldSearchOptions inputWidth(int w) {
        this.field.search().setInputWidth(w);
        return this;
    }

    /**
     * 设置搜索输入框标签宽度
     * */
    public FieldSearchOptions labelWidth(int w) {
        this.field.search().setLabelWidth(w);
        return this;
    }

    /**
     * 是否使用范围搜索，仅支持 日期 和 数字 类型
     * */
    public FieldSearchOptions range(boolean useRange) {
        this.field.search().setSearchInRange(useRange);
        return this;
    }

    /**
     * 是否使用范围搜索，仅支持 日期 和 数字 类型 <br/>
     * 搜索栏出现两个输入框，指定搜索范围
     * */
    public FieldSearchOptions range() {
        this.field.search().setSearchInRange(true);
        return this;
    }

    /**
     * 用于匹配JSON数组的查询，适用的情况:<br/>
     * 如，字段内存储格式为字符串，且JSON数组格式，如 ["red","blue","orange"]
     * */
    public FieldSearchOptions checkJsonElement() {
        this.field.search().setSearchInRange(true);
        return this;
    }

    /**
     * 指定搜索字段，默认为当前配置的字段 <br/>
     * 当非本表字段时，需要指定 table().fillBy 或 form 的 fillwith 实现自动的关联查询
     * */
    public FieldSearchOptions on(DBField field) {
        this.field.search().setSearchField(field.table().name()+"."+field.name());
        return this;
    }

    /**
     * 针对某些需要选择的组件，是否在选择后立即触发查询
     * */
    public FieldSearchOptions triggerOnSelect(boolean trigger) {
        if(this.field.getType()== InputType.TEXT_INPUT || this.field.getType()== InputType.TEXT_AREA || this.field.getType()== InputType.NUMBER_INPUT) {
            throw new RuntimeException("不支持 triggerOnSelect");
        }
        this.field.search().setTriggerOnSelect(trigger);
        return this;
    }

    /**
     * 返回当前字段的 table 配置
     * */
    public FieldSearchOptions selectMuliti(boolean muliti) {
        this.field.search().setSelectMuliti(muliti);
        return this;
    }

    /**
     * 查询时的值匹配模式
     * */
    public FieldSearchOptions matchType(MatchType type) {
        this.field.search().setMatchType(type.code());
        return this;
    }

//    /**
//     * 指定搜索字段，默认为当前配置的字段
//     * */
//    public FieldSearchOptions on(String field) {
//        this.field.search().setSearchField(field);
//        return this;
//    }

}
