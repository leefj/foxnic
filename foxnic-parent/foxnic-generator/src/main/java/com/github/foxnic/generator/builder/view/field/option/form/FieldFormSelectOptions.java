package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;
import com.github.foxnic.sql.meta.DBField;

public class FieldFormSelectOptions extends SubOptions {

    public FieldFormSelectOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 配置成字典模式，并设置字典代码
     * */
    public FieldFormSelectOptions dict(CodeTextEnum dict) {
        this.field.selectField().dict(dict);
        this.field.selectField().setValueField("code");
        this.field.selectField().setTextField("text");
        this.toolbar(false);
        this.filter(false);
        return this;
    }

    /**
     * 配置成枚举模式，并设置 CodeTextEnum 类型的枚举
     * */
    public FieldFormSelectOptions enumType(Class<? extends CodeTextEnum> enumType) {
        this.field.selectField().enumType(enumType);
        this.field.selectField().setValueField("code");
        this.field.selectField().setTextField("text");
        this.toolbar(false);
        this.filter(false);
        return this;
    }

    /**
     * 指定取数的 API 地址
     * */
    public FieldFormSelectOptions queryApi(String api) {
        this.field.selectField().queryApi(api);
        if(api.endsWith("/query-paged-list")) {
            this.paging(true);
        }
        return this;
    }

    /**
     * 配置为是否多选
     * */
    public  FieldFormSelectOptions muliti(boolean m) {
        this.field.selectField().muliti(m);
        return this;
    }

    /**
     * 指定用那个属性的数据填充下拉框的已选值
     * */
    public FieldFormSelectOptions fillBy(String prop) {
        this.field.selectField().fillBy(prop);
        return this;
    }



    /**
     * 是否分页
     * */
    public FieldFormSelectOptions paging(boolean paging) {
        this.field.selectField().paging(paging);
        return this;
    }

    /**
     * 分页大小
     * */
    public FieldFormSelectOptions size(int size) {
        this.field.selectField().size(size);
        return this;
    }

    /**
     * 设置值列名
     * */
    public FieldFormSelectOptions valueField(String field) {
        this.field.selectField().setValueField(field);
        return this;
    }

    /**
     * 设置显示列名
     * */
    public FieldFormSelectOptions textField(String field) {
        this.field.selectField().setTextField(field);
        return this;
    }

    /**
     * 设置值列名
     * */
    public FieldFormSelectOptions valueField(DBField field) {
        this.field.selectField().setValueField(field.name());
        return this;
    }

    /**
     * 设置显示列名
     * */
    public FieldFormSelectOptions textField(DBField field) {
        this.field.selectField().setTextField(field.name());
        return this;
    }

    /**
     * 是否使用工具栏
     * */
    public FieldFormSelectOptions toolbar(boolean displayToolbar) {
        this.field.selectField().setToolbar(displayToolbar);
        return this;
    }

    /**
     * 是否使用过滤功能
     * */
    public FieldFormSelectOptions filter(boolean displayFilter) {
        this.field.selectField().setFilter(displayFilter);
        return this;
    }





}
