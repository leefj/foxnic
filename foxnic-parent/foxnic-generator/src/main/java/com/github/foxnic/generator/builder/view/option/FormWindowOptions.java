package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.commons.lang.DataParser;
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

    /**
     * 设置表单窗口的宽度，默认500px
     * @param  width  宽度 ，如 100px 或 80%
     * */
    public FormWindowOptions width(String width) {

        String w=width.trim();
        if(width.toLowerCase().endsWith("px")) {
            w=w.substring(0,w.length()-2);
        }
        else if(width.endsWith("%")) {
            w=w.substring(0,w.length()-1);
        } else {
            throw new IllegalArgumentException("错误的宽度值");
        }
        Integer iw= DataParser.parseInteger(w);
        if(iw==null) {
            throw new IllegalArgumentException("错误的宽度值");
        }
        this.config.setWidth(width);
        return this;
    }
}
