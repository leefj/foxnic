package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.FormWindowConfig;

public class FormWindowOptions {

    private FormWindowConfig config;

    public FormWindowOptions(FormWindowConfig config) {
        this.config=config;
    }

    /**
     * 设置底部按钮和最下方之间的间距，用于撑高表单窗口的高度，便于下拉框展示
     * */
    public FormWindowOptions bottomSpace(Integer height) {
        this.config.setBottomSpace(height);
        return this;
    }




}
