package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ListConfig;

public class ListOptions {

    private ListConfig config;

    public ListOptions(ListConfig config) {
        this.config=config;
    }

    /**
     * 使用分栏布局
     * */
    public void columnLayout(Object... inputs) {
        this.config.setInputColumnLayout(inputs);
    }



}
