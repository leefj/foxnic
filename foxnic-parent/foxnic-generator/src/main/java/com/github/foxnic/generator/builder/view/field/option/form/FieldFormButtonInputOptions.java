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
     * 组织节点选择对话框，包括公司和部门
     * @param single 是否单选
     * */
    public FieldFormButtonInputOptions chooseOrganization(boolean single) {
        return chooseOrganization(single,null);
    }

    /**
     * 组织节点选择对话框，包括公司和部门
     * @param single 是否单选
     * @param root  根节点 id 或 code 优先匹配 id
     * */
    public FieldFormButtonInputOptions chooseOrganization(boolean single,String root)
    {
        field.buttonField().setText("请选择组织节点");
        ActionConfig action=new ActionConfig();
        action.setLabel("请选择组织节点");
        action.setFunctionName("fox.chooseOrgNode");
        action.setId("chooseOrgNode");
        action.setCss("");
        action.setFunctionInExt(false);
        action.setTargetType("org");
        action.setActionType(ActionConfig.ActionType.ORG_DIALOG);
        action.setSingle(single);
        action.setRootId(root);
        action.setIconHtml("<i class='layui-icon layui-icon-search'></i>");
        field.buttonField().setAction(action);
        return this;
    }

    /**
     * 组织节点选择对话框，包括公司
     * @param single 是否单选
     * */
    public FieldFormButtonInputOptions chooseCompany(boolean single) {
        return chooseCompany(single,null);
    }

    /**
     * 组织节点选择对话框，仅包括公司
     * @param single 是否单选
     * @param root  根节点 id 或 code 优先匹配 id
     * */
    public FieldFormButtonInputOptions chooseCompany(boolean single,String root)
    {

        field.buttonField().setText("请选择公司");
        ActionConfig action=new ActionConfig();
        action.setLabel("请选择公司");
        action.setFunctionName("fox.chooseOrgNode");
        action.setId("chooseOrgNode");
        action.setCss("");
        action.setFunctionInExt(false);
        action.setTargetType("com");
        action.setActionType(ActionConfig.ActionType.ORG_DIALOG);
        action.setSingle(single);
        action.setRootId(root);
        action.setIconHtml("<i class='layui-icon layui-icon-search'></i>");
        field.buttonField().setAction(action);
        return this;
    }

    /**
     * 组织节点选择对话框，仅包括部门
     * @param single 是否单选
     */
    public FieldFormButtonInputOptions chooseDepartment(boolean single)
    {
        return chooseDepartment(single,null);
    }
    /**
     * 组织节点选择对话框，仅包括部门
     * @param single 是否单选
     * @param root  根节点 id 或 code 优先匹配 id
     * */
    public FieldFormButtonInputOptions chooseDepartment(boolean single,String root)
    {
        field.buttonField().setText("请选择部门");
        ActionConfig action=new ActionConfig();
        action.setLabel("请选择部门");
        action.setFunctionName("fox.chooseOrgNode");
        action.setId("chooseOrgNode");
        action.setCss("");
        action.setFunctionInExt(false);
        action.setTargetType("dept");
        action.setActionType(ActionConfig.ActionType.ORG_DIALOG);
        action.setSingle(single);
        action.setRootId(root);
        action.setIconHtml("<i class='layui-icon layui-icon-search'></i>");
        field.buttonField().setAction(action);
        return this;
    }

    /**
     * 组织节点选择对话框
     * @param single 是否单选
     * */
    public FieldFormButtonInputOptions choosePosition(boolean single)
    {
        return choosePosition(single,null);
    }

    /**
     * 组织节点选择对话框
     * @param single 是否单选
     * @param root  根节点 id 或 code 优先匹配 id
     * */
    public FieldFormButtonInputOptions choosePosition(boolean single,String root)
    {
        field.buttonField().setText("请选择岗位");
        ActionConfig action=new ActionConfig();
        action.setLabel("请选择岗位");
        action.setFunctionName("fox.chooseOrgNode");
        action.setId("chooseOrgNode");
        action.setCss("");
        action.setFunctionInExt(false);
        action.setTargetType("pos");
        action.setActionType(ActionConfig.ActionType.POS_DIALOG);
        action.setRootId(root);
        action.setSingle(single);
        action.setIconHtml("<i class='layui-icon layui-icon-search'></i>");
        field.buttonField().setAction(action);
        return this;
    }







}
