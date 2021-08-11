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
    public ListOptions columnLayout(Object... inputs) {
        this.config.setInputColumnLayout(inputs);
        return this;
    }


    /**
     * 设置操作列的宽度
     * */
    public ListOptions operateColumnWidth(int width) {
        this.config.setOperateColumnWidth(width);
        return this;
    }
}
