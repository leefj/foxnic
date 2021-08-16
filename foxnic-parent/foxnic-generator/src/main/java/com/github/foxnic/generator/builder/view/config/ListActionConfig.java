package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.generator.util.JSFunctions;

public class ListActionConfig {

    private JSFunctions.JSFunction function;
    private String label;
    private String id;



    public JSFunctions.JSFunction getJsFunction() {
        return function;
    }

    public void setJsFunction(JSFunctions.JSFunction jsFuncSource) {
        this.function = jsFuncSource;
    }

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
