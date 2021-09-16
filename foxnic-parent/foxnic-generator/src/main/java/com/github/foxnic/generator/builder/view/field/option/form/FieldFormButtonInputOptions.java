package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.config.ActionConfig;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormButtonInputOptions extends SubOptions {

    public FieldFormButtonInputOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 设置按钮
     * */
    public FieldFormButtonInputOptions action(String buttonText,String jsFuncName){
        return action(buttonText,jsFuncName,null,null);
    }

    /**
     * 设置按钮
     * */
    public FieldFormButtonInputOptions action(String buttonText,String jsFuncName,String css,String iconHtml){
        field.buttonField().setText(buttonText);
        ActionConfig action=new ActionConfig();
        action.setLabel(buttonText);
        action.setFunctionName(jsFuncName);
        action.setId(jsFuncName);
        action.setCss(css);
        action.setIconHtml(iconHtml);
        field.buttonField().setAction(action);
        return this;
    }

    /**
     * 组织节点选择对话框
     * */
    public FieldFormButtonInputOptions chooseOrganization(boolean single)
    {
        field.buttonField().setText("请选择部门");
        ActionConfig action=new ActionConfig();
        action.setLabel("请选择部门");
        action.setFunctionName("fox.chooseOrgNode");
        action.setId("chooseOrgNode");
        action.setCss("");
        action.setFunctionInExt(false);
        action.setTargetType("org");
        action.setSingle(single);
        action.setIconHtml("<i class='layui-icon layui-icon-search'></i>");
        field.buttonField().setAction(action);
        return this;
    }

    /**
     * 组织节点选择对话框
     * */
    public FieldFormButtonInputOptions choosePosition(boolean single)
    {
        field.buttonField().setText("请选择职位");
        ActionConfig action=new ActionConfig();
        action.setLabel("请选择职位");
        action.setFunctionName("fox.chooseOrgNode");
        action.setId("chooseOrgNode");
        action.setCss("");
        action.setFunctionInExt(false);
        action.setTargetType("pos");
        action.setSingle(single);
        action.setIconHtml("<i class='layui-icon layui-icon-search'></i>");
        field.buttonField().setAction(action);
        return this;
    }







}
