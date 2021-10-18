package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ActionConfig;
import com.github.foxnic.generator.builder.view.config.ListConfig;
import com.github.foxnic.generator.config.ModuleContext;

public class ListOptions {

    private ListConfig config;
    private ModuleContext context;

    public ListOptions(ModuleContext context,ListConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 使用分栏布局
     * */
    public ListOptions columnLayout(Object... inputs) {
        this.config.setInputColumnLayout(inputs);
        return this;
    }




    /**
     * 禁止新建
     * */
    public ListOptions disableCreateNew() {
        this.config.setDisableCreateNew(true);
        return this;
    }

    /**
     * 禁止修改
     * */
    public ListOptions disableModify() {
        this.config.setDisableModify(true);
        return this;
    }



    /**
     * 禁止单个删除
     * */
    public ListOptions disableSingleDelete() {
        this.config.setDisableSingleDelete(true);
        return this;
    }

    /**
     * 禁止批量删除
     * */
    public ListOptions disableBatchDelete() {
        this.config.setDisableBatchDelete(true);
        return this;
    }

    /**
     * 禁止查看表单
     * */
    public ListOptions disableFormView() {
        this.config.setDisableFormView(true);
        return this;
    }

    /**
     * 禁止空白列
     * */
    public ListOptions disableSpaceColumn() {
        this.config.setDisableModify(true);
        this.config.setDisableSpaceColumn(true);
        return this;
    }

    /**
     * 最左侧操作列的配置
     * */
    public ListOperationColumnOptions operationColumn() {
        return new ListOperationColumnOptions(this.context,this.config);
    }



    /**
     * 为列表增加工具栏按钮
     * @param label 按钮标签
     * @param jsFuncName js函数名称
     * @param css 按钮 class 属性追加的样式名称
     * */
    public ActionConfig addToolButton(String label, String jsFuncName, String css) {
        ActionConfig action=new ActionConfig();
        action.setLabel(label);
        action.setFunctionName(jsFuncName);
        action.setId(jsFuncName);
        action.setCss(css);
        this.config.addToolButton(action);
        return action;
    }

    /**
     * 关闭页边距
     * */
    public ListOptions disableMargin() {
        this.config.setMarginDisable(true);
        return this;
    }

    /**
     * 加入JS变量
     * */
    public ListOptions addJsVariable(String name, String value, String note) {
        this.config.addJsVariable(name,value,note);
        return this;
    }

    /**
     * 设置列表单选或多选
     * */
    public void mulitiSelect(boolean muliti) {
        this.config.setMulitiSelect(muliti);
    }
}
