package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.FormConfig;

public class FormOptions {

    private FormConfig config;

    public FormOptions(FormConfig config) {
        this.config=config;
    }

    /**
     * 使用分栏布局
     * */
    public void columnLayout(Object[]... inputs) {
        this.config.setInputColumnLayout(inputs);
    }

    /**
     * 添加一个分布局
     * */
    public void addGroup(String title,Object[]... inputs) {
        this.config.addGroup(title,inputs);
    }

}
