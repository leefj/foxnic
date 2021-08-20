package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ListActionConfig;
import com.github.foxnic.generator.builder.view.config.ListConfig;
import com.github.foxnic.generator.config.ModuleContext;

public class ListOperationColumnOptions {

    private ListConfig config;
    private ModuleContext context;

    public ListOperationColumnOptions(ModuleContext context,ListConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 设置操作列的宽度，默认值 160，参考值：<br/>
     * 两个按钮 125 <br/>
     * 三个按钮 160 <br/>
     * */
    public ListOperationColumnOptions width(int width) {
        this.config.setOperateColumnWidth(width);
        return this;
    }

    public ListActionConfig addActionButton(String label,String jsFuncName) {
        ListActionConfig action=new ListActionConfig();
        action.setLabel(label);
        action.setFunctionName(jsFuncName);
        action.setId(jsFuncName);
        this.config.addOpColumnButtons(action);
        return action;
    }
}
