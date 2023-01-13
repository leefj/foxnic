package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.SearchAreaConfig;

public class SearchAreaOptions {

    private SearchAreaConfig config;

    public  SearchAreaOptions(SearchAreaConfig config) {
        this.config=config;
    }


    /**
     * 设置行布局
     * */
    public SearchAreaOptions inputLayout(Object[]... inputRows) {
        this.config.setInputLayout(inputRows);
        return this;
    }

    /**
     * 设置默认输入框宽度
     * */
    public SearchAreaOptions inputWidth(int w) {
        this.config.setInputWidth(w);
        return this;
    }

    /**
     * 按栏次设置标签宽度
     * @param columnIndex 栏次，从1开始编号
     * */
    public SearchAreaOptions labelWidth(int columnIndex, int width) {
        if(columnIndex<0) columnIndex=0;
        columnIndex=columnIndex-1;
        this.config.setLabelWidth(columnIndex,width);
        return this;
    }

    /**
     * 按栏次设置标签宽度
     * @param columnIndex 栏次，从1开始编号
     * */
    public SearchAreaOptions inputWidth(int columnIndex, int width) {
        if(columnIndex<0) columnIndex=0;
        columnIndex=columnIndex-1;
        this.config.setInputWidth(columnIndex,width);
        return this;
    }

    /**
     * 显示的搜索行数
     * */
    public SearchAreaOptions rowsDisplay(int rows) {
        this.config.setRowsDisplay(rows);
        return this;
    }

    /**
     * 禁用搜索区域
     * */
    public SearchAreaOptions disable() {
        this.config.setDisable(true);
        return this;
    }
}
