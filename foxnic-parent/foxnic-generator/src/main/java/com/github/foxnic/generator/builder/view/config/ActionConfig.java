package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.bean.BeanNameUtil;

public class ActionConfig {

    public enum ActionType implements CodeTextEnum {
        /**
         * 组织对话框
         * */
        ORG_DIALOG("org-dialog"),
        /**
         * 岗位对话框
         * */
        POS_DIALOG("pos-dialog"),
        /**
         * 员工对话框
         * */
        EMP_DIALOG("emp-dialog");

        private String code;
        private ActionType(String code) {
            this.code=code;
        }

        public String code() {
            return this.code;
        }

        public String text() {
            return this.name();
        }


    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    private String functionName;
    private String label;

    public String getActionType() {
        if(actionType==null) return "";
        return actionType.code();
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    private ActionType actionType =null;

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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    private String targetType =null;

    public Boolean getSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    private Boolean single;

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    private String rootId;

}
