package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormTextAreaOptions extends SubOptions {

    public FieldFormTextAreaOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    public FieldFormTextAreaOptions height(int h) {
        this.field.textArea().setHeight(h);
        return this;
    };




}
