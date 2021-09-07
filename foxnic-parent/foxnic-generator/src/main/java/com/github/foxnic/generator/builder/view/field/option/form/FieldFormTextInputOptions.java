package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;
import com.github.foxnic.generator.builder.view.option.ViewOptions;

public class FieldFormTextInputOptions extends SubOptions {

    public FieldFormTextInputOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }


    public FieldFormTextInputOptions defaultText(String text) {
        this.field.getTextField().setDefaultText(text);
        return this;
    }


}
