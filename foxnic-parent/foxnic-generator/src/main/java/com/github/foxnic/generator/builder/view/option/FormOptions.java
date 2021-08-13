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

    /**
     * 设置标签宽度，单栏次默认 65；多栏次默认 100
     * */
    public FormOptions labelWidth(Integer w) {
        this.config.setLabelWidth(w);
        return this;
    }
}
