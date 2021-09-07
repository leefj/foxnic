package com.github.foxnic.generator.builder.view.field.config;

public class TextAreaConfig {

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int height=120;

    public String getDefaultText() {
        return defaultText;
    }

    private String defaultText;

    public void setDefaultText(String text) {
        this.defaultText=text;
    }
}
