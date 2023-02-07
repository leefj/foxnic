package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.FormConfig;
import com.github.foxnic.generator.builder.view.config.Tab;
import com.github.foxnic.generator.config.ModuleContext;

import java.util.Arrays;

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
     * 添加一个分组布局
     * */
    public FormOptions addGroup(String title,Object[]... inputs) {
        this.config.addGroup(title,inputs);
        return this;
    }

    /**
     * 添加一个内嵌页面
     * */
    public FormOptions addPage(String title, String jsFuncName) {
        this.config.addPage(title,jsFuncName);
        return this;
    }

    /**
     * 添加一个内嵌的 Tab
     * */
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

    /**
     * 添加 JS
     * */
    public FormOptions addJs(String... js) {
        this.config.addJs(Arrays.asList(js));
        return this;
    }

    /**
     * 添加 CSS
     * */
    public FormOptions addCss(String... css) {
        this.config.addCss(Arrays.asList(css));
        return this;
    }

    public FormOptions enableContextMenu(boolean b) {
        this.config.setEnableContextMenu(b);
        return this;
    }

    public FormOptions enableContextMenu() {
        return this.enableContextMenu(true);
    }

    /**
     * 查询表单数据的接口地址，若无特殊要求无需指定，自动按默认生成
     * */
    public FormOptions queryAPI(String url) {
        this.config.setQueryApi(url);
        return this;
    }

    /**
     * 数据保存接口地址，若无特殊要求无需指定，自动按默认生成
     * */
    public FormOptions saveAPI(String url) {
        this.config.setInsertApi(url);
        this.config.setUpdateApi(url);
        return this;
    }

    /**
     * 数据保存接口地址，若无特殊要求无需指定，自动按默认生成
     * */
    public FormOptions saveAPI(String insertUrl,String updateUrl) {
        this.config.setInsertApi(insertUrl);
        this.config.setUpdateApi(updateUrl);
        return this;
    }

}
