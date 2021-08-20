package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.bean.BeanNameUtil;

public class ListActionConfig {

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    private String functionName;
    private String label;
    private String id;

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
