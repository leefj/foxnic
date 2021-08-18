package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.FormConfig;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.builder.view.config.JSFunctions;

public class FormOptions {

    private FormConfig config;
    private ModuleContext context;

    public FormOptions(ModuleContext context,FormConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 使用分栏布局
     * */
    public FormOptions columnLayout(Object[]... inputs) {
        this.config.setInputColumnLayout(inputs);
        return this;
    }

    /**
     * 添加一个分布局
     * */
    public FormOptions addGroup(String title,Object[]... inputs) {
        this.config.addGroup(title,inputs);
        return this;
    }

    /**
     * 设置标签宽度，单栏次默认 65；多栏次默认 100
     * */
    public FormOptions labelWidth(Integer w) {
        this.config.setLabelWidth(w);
        return this;
    }

    /**
     *  设置表单填充之前调用的JS函数
     * */
    public FormOptions jsBeforeDataFill(String jsFuncId) {
        JSFunctions.JSFunction func=this.context.getJsFunction(jsFuncId);
        if(func==null) {
            throw new IllegalArgumentException(jsFuncId+" Js 函数未定义");
        }
        if(!func.hasParam("data")) {
            throw new IllegalArgumentException(func.getName()+" 需要定义一个名为 data 的参数，用于接收表单数据");
        }
        func.prefixTab(1);
        this.config.setJsBeforeDataFill(func);
        return this;
    }

    /**
     *  设置表单填充之后调用的JS函数
     * */
    public FormOptions jsAfterDataFill(String jsFuncId) {
        JSFunctions.JSFunction func=this.context.getJsFunction(jsFuncId);
        if(func==null) {
            throw new IllegalArgumentException(jsFuncId+" Js 函数未定义");
        }
        if(!func.hasParam("data")) {
            throw new IllegalArgumentException(func.getName()+" 需要定义一个名为 data 的参数，用于接收表单数据");
        }
        func.prefixTab(1);
        this.config.setJsAfterDataFill(func);
        return this;
    }
}
