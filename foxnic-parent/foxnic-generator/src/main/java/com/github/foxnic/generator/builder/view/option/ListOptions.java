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

    /**
     * 禁止新建
     * */
    public ListOptions disableCreateNew() {
        this.config.setDisableCreateNew(true);
        return this;
    }

    /**
     * 禁止修改
     * */
    public ListOptions disableModify() {
        this.config.setDisableModify(true);
        return this;
    }

    /**
     * 禁止单个删除
     * */
    public ListOptions disableSingleDelete() {
        this.config.setDisableSingleDelete(true);
        return this;
    }

    /**
     * 禁止批量删除
     * */
    public ListOptions disableBatchDelete() {
        this.config.setDisableBatchDelete(true);
        return this;
    }

    /**
     * 禁止查看表单
     * */
    public ListOptions disableFormView() {
        this.config.setDisableFormView(true);
        return this;
    }

    /**
     * 禁止空白列
     * */
    public ListOptions disableSpaceColumn() {
        this.config.setDisableModify(true);
        this.config.setDisableSpaceColumn(true);
        return this;
    }
}
