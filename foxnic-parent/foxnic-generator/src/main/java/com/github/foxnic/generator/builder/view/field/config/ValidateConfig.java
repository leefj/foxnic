package com.github.foxnic.generator.builder.view.field.config;

public class ValidateConfig {

    private boolean isNotBlank;


    public boolean isNotBlank() {
        return isNotBlank;
    }

    /**
     * 不允许为空
     * */
    public void notBlank() {
        isNotBlank = true;
    }
}
