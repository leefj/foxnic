package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.generator.builder.view.config.ActionConfig;
import com.github.foxnic.generator.builder.view.field.FieldInfo;

public class ButtonFieldConfig {

    /**
     * 按钮上的文本
     * */
    private String text;

    private ActionConfig action;

    private FieldInfo fieldInfo;

    public ButtonFieldConfig(FieldInfo fieldInfo) {
        this.fieldInfo=fieldInfo;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ActionConfig getAction() {
        return action;
    }

    public void setAction(ActionConfig action) {
        this.action = action;
    }

}
