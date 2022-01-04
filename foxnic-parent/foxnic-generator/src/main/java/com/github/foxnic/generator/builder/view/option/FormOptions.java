package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.FormConfig;
import com.github.foxnic.generator.builder.view.config.Tab;
import com.github.foxnic.generator.config.ModuleContext;

public class FormOptions {

    private FormConfig config;
    private ModuleContext context;

    public FormOptions(ModuleContext context,FormConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 使用分栏布局
     * */
    public FormOptions columnLayout(Object[]... inputs) {
        this.config.setInputColumnLayout(inputs);
        return this;
    }

    /**
     * 添加一个分布局
     * */
    public FormOptions addGroup(String title,Object[]... inputs) {
        this.config.addGroup(title,inputs);
        return this;
    }

    public FormOptions addPage(String title, String jsFuncName) {
        this.config.addPage(title,jsFuncName);
        return this;
    }

    public FormOptions addTab(Tab... tab) {
        this.config.addTab(tab);
        return this;
    }


    /**
     * 添加一个分布局
     * */
    public FormOptions addGroup(String elId,String title,Object[]... inputs) {
        this.config.addGroup(elId,title,inputs);
        return this;
    }

    public FormOptions addPage(String elId,String title, String jsFuncName) {
        this.config.addPage(elId,title,jsFuncName);
        return this;
    }

    public FormOptions addTab(String elId,Tab... tab) {
        this.config.addTab(elId,tab);
        return this;
    }


    /**
     * 设置标签宽度，单栏次默认 65；多栏次默认 100
     * */
    public FormOptions labelWidth(Integer w) {
        this.config.setLabelWidth(w);
        return this;
    }

    /**
     * 加入JS变量
     * */
    public FormOptions addJsVariable(String name, String value, String note) {
        this.config.addJsVariable(name,value,note);
        return this;
    }


    /**
     * 禁用保存按钮，用于页面的嵌入
     * */
    public FormOptions disableFooter() {
        this.config.setFooterDisabled(true);
        return this;
    }

    /**
     * 禁用保存按钮，用于页面的嵌入
     * */
    public FormOptions disableMargin() {
        this.config.setMarginDisabled(true);
        return this;
    }
}
