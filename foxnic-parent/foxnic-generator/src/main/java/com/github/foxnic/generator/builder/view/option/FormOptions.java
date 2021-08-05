package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.FormConfig;

public class FormOptions {

    private FormConfig config;

    public FormOptions(FormConfig config) {
        this.config=config;
    }


    public void inputColumnLayout(int column,Object... inputs) {
        this.config.setInputColumnLayout(column,inputs);
    }

}
