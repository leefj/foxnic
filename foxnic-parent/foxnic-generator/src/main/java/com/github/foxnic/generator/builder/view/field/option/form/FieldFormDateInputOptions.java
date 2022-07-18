package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormDateInputOptions extends SubOptions {

    public FieldFormDateInputOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }


    /**
     * 格式,如:<br/>
     * yyyy-MM-dd HH:mm:ss , yyyy-MM-dd
     * 默认，为自动识别
     * */
    public FieldFormDateInputOptions format(String format) {
        field.dateField().setFormat(format);
        return this;
    }

    /**
     * 设置默认值为当前时间
     * */
    public FieldFormDateInputOptions defaultNow() {
        field.dateField().setDefaultValue("now");
        return this;
    }

    /***
     * 未避免 iframe 区域太小而被遮挡，可设置改值使其弹出框显示在 iframe 之外 ; 默认 false
     * */
    public FieldFormDateInputOptions renderAtTop(boolean renderTop) {
        field.dateField().setRenderAtTop(renderTop);
        return this;
    }
}
