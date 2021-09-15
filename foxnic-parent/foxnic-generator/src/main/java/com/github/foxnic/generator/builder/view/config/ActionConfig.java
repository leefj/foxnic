package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.bean.BeanNameUtil;

public class ActionConfig {

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    private String functionName;
    private String label;

    public boolean getIsFunctionInExt() {
        return isFunctionInExt;
    }

    public void setFunctionInExt(boolean functionInExt) {
        isFunctionInExt = functionInExt;
    }

    private boolean isFunctionInExt=true;

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    private String css;
    private String id;

    public String getIconHtml() {
        return icon;
    }

    public void setIconHtml(String iconHtml) {
        this.icon = iconHtml;
    }

    private String icon;


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = BeanNameUtil.instance().depart(id).replace('_','-');
    }







}
