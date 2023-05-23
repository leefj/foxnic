package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.config.FillByUnit;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;
import com.github.foxnic.generator.builder.view.option.BpmOptions;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.util.ConfigCollector;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldFormOptions extends SubOptions {

    private FieldFormValidateOptions validate;
    private FieldFormRadioOptions radio;
    private FieldFormSelectOptions select;
    private FieldFormUploadOptions upload;
    private FieldFormLogicOptions logic;
    private FieldFormTextAreaOptions textArea;
    private FieldFormNumberInputOptions numberInput;
    private FieldFormDateInputOptions dateInput;
    private FieldFormCheckOptions checker;
    private FieldFormTextInputOptions textInput;
    private FieldFormButtonInputOptions buttonInputOptions;
    private ModuleContext context;


    public FieldFormOptions(ModuleContext context, FieldInfo field, FieldOptions top) {
        super(field,top);
        this.context=context;
        this.validate=new FieldFormValidateOptions(this.field,top);
        this.radio=new FieldFormRadioOptions(this.field,top);
        this.select=new FieldFormSelectOptions(context,this.field,top);
        this.upload=new FieldFormUploadOptions(this.field,top);
        this.logic=new FieldFormLogicOptions(this.field,top);
        this.numberInput=new FieldFormNumberInputOptions(this.field,top);
        this.textArea=new FieldFormTextAreaOptions(this.field,top);
        this.dateInput=new FieldFormDateInputOptions(this.field,top);
        this.checker=new FieldFormCheckOptions(this.field,top);
        this.textInput=new FieldFormTextInputOptions(this.field,top);
        this.buttonInputOptions=new FieldFormButtonInputOptions(this.field,top);
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
     * 设置当前字段为单选框，在搜索栏表现为可多选的下拉框
     * */
    public FieldFormRadioOptions radioBox() {
        return radio;
    }

    /**
     * 设置当前字段为复选框，在搜索栏表现为可多选的下拉框
     * */
    public FieldFormCheckOptions checkBox() {
        return checker;
    }

    /**
     * 设置当前字段为下拉框，在搜索栏表现为可多选的下拉框
     * */
    public FieldFormSelectOptions selectBox() {
        return select;
    }

    /**
     * 设置当前字段为文件上传
     * */
    public FieldFormUploadOptions upload() {
        return upload;
    }

    /**
     * 设置当前字段为逻辑字段，在搜索栏表现为可多选的下拉框
     * */
    public FieldFormLogicOptions logicField() {
        this.table().alignCenter();
        return logic;
    }

    /**
     * 设置当前字段为文本域(多行)
     * */
    public FieldFormTextAreaOptions textArea() {
        this.field.textArea();
        return textArea;
    }

    /**
     * 设置当前字段为文本框(单行)
     * */
    public FieldFormTextInputOptions textInput() {
        this.field.getTextField();
        return textInput;
    }

    /**
     * 设置当前字段为数字输入框
     * */
    public FieldFormNumberInputOptions numberInput() {
        return numberInput;
    }

    public FieldFormDateInputOptions dateInput() {
        this.validate().date();
        this.field.dateField();
        return dateInput;
    }

    /**
     * 指定表单字段中要填充的数据<br/> 依次指定值所在的属性，形成路径
     * 如果 select box 要使用 fillWith
     * */
    public FieldFormOptions fillBy(String... props) {
        this.field.setFormFillByPropertyNames(props);
        Throwable th=new Throwable();
        StackTraceElement el=th.getStackTrace()[1];
        List<FillByUnit> fillByUnits= ConfigCollector.collectFills(el);
        this.context.setFillByUnits(fillByUnits);
        return this;
    }

    /**
     * 表单元素为按钮
     * */
    public FieldFormButtonInputOptions button() {
        return buttonInputOptions;
    }


    /**
     * 设置为只读
     * */
    public FieldFormOptions readOnly() {
        this.field.setReadOnlyInForm(true);
        return this;
    }


    /**
     * 使在同一行
     * */
    public FieldFormOptions inlines(Object... field) {
        List<String> fields=new ArrayList<>();
        for (Object o : field) {
            if(o instanceof String) {
               fields.add((String) o);
            } else if(o instanceof DBField) {
                fields.add(((DBField) o).var());
            } else {
                throw new IllegalArgumentException("类型不支持");
            }
        }
        this.field.setInlines(fields);
        return this;
    }

    public FieldFormOptions inlineDelta(int delta) {
        this.field.setInlineDelta(delta);
        return this;
    }

    /**
     * inline
     * */
    public FieldFormOptions inputWidth(int width) {
        this.field.setInputWidth(width);
        return this;
    }
}
