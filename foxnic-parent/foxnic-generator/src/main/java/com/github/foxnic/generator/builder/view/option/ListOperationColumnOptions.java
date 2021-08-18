package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ListActionConfig;
import com.github.foxnic.generator.builder.view.config.ListConfig;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.util.JSFunctions;

public class ListOperationColumnOptions {

    private ListConfig config;
    private ModuleContext context;

    public ListOperationColumnOptions(ModuleContext context,ListConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 设置操作列的宽度，默认值 125，参考值：<br/>
     * 两个按钮 125 <br/>
     * 三个按钮 175 <br/>
     * */
    public ListOperationColumnOptions width(int width) {
        this.config.setOperateColumnWidth(width);
        return this;
    }

    public ListActionConfig addActionButton(String label,String jsFuncId) {

        JSFunctions.JSFunction func=this.context.getJsFunction(jsFuncId);
        if(func==null) {
            throw new IllegalArgumentException(jsFuncId+" Js 函数未定义");
        }
        if(!func.hasParam("data")) {
            throw new IllegalArgumentException(func.getName()+" 需要定义一个名为 data 的参数，用于接收行数据");
        }
        func.prefixTab(1);
        ListActionConfig action=new ListActionConfig();
        action.setLabel(label);
        action.setJsFunction(func);
        action.setId(jsFuncId);
        this.config.addOpColumnButtons(action);
        return action;
    }
}
