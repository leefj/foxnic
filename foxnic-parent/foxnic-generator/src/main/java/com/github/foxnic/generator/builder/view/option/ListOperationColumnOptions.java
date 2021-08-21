package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ActionConfig;
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


    /**
     * 为列表的操作列增加一个按钮
     * @param label 按钮标签
     * @param jsFuncName js函数名称
     * */
    public ActionConfig addActionButton(String label, String jsFuncName) {
        return addActionButton(label,jsFuncName,null);
    }

    /**
     * 为列表的操作列增加一个按钮
     * @param label 按钮标签
     * @param jsFuncName js函数名称
     * @param css 按钮 class 属性追加的样式名称
     * */
    public ActionConfig addActionButton(String label, String jsFuncName, String css) {
        ActionConfig action=new ActionConfig();
        action.setLabel(label);
        action.setFunctionName(jsFuncName);
        action.setId(jsFuncName);
        action.setCss(css);
        this.config.addOpColumnButton(action);
        return action;
    }



    /**
     * 为列表的操作列增加一个更多菜单
     * @param label 按钮标签
     * @param actionId 动作ID，用于判断点击了哪个菜单项目
     * @param css 按钮 class 属性追加的样式名称
     * */
    public ActionConfig addActionMenu(String actionId,String label) {
        ActionConfig action=new ActionConfig();
        action.setLabel(label);
        action.setId(actionId);
        this.config.addOpColumnMenu(action);
        return action;
    }
}
