package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.builder.view.config.ActionConfig;
import com.github.foxnic.generator.builder.view.config.ListConfig;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.config.ModuleContext;

import java.util.Arrays;

public class ListOptions {

    private ListConfig config;
    private ModuleContext context;

    public ListOptions(ModuleContext context,ListConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 指定列布局
     * */
    public ListOptions columnLayout(Object... inputs) {

        for (Object input : inputs) {
            FieldInfo fi= context.getField(input);
            if(fi!=null && fi.isDBTreatyFiled()) {
                fi.setDisplayWhenDBTreaty(true);
            }
        }

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
        return addToolButton(label,jsFuncName,css,null);
    }

    /**
     * 为列表增加工具栏按钮
     * @param label 按钮标签
     * @param jsFuncName js函数名称
     * @param css 按钮 class 属性追加的样式名称
     * @param perm 权限代码
     * */
    public ActionConfig addToolButton(String label, String jsFuncName, String css,String perm) {
        ActionConfig action=new ActionConfig();
        action.setLabel(label);
        action.setFunctionName(jsFuncName);
        action.setId(jsFuncName);
        action.setCss(css);
        action.setPerm(perm);
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



    /**
     * 配置新建按钮，如果不改变默认值，则传入 null 即可
     * */
    public ListOptions configCreateNewButton(String label, String jsFuncName, String css) {
        ActionConfig action=new ActionConfig();
        action.setLabel(label);
        action.setFunctionName(jsFuncName);
        if(StringUtil.hasContent(jsFuncName)) {
            action.setId(jsFuncName);
        }
        action.setCss(css);
        this.config.setCreateNewButtonConfig(action);
        return this;
    }

    /**
     * 配置批量删除按钮，如果不改变默认值，则传入 null 即可
     * */
    public ListOptions configBatchDeleteButton(String label, String jsFuncName, String css) {
        ActionConfig action=new ActionConfig();
        action.setLabel(label);
        action.setFunctionName(jsFuncName);
        if(StringUtil.hasContent(jsFuncName)) {
            action.setId(jsFuncName);
        }
        action.setCss(css);
        this.config.setBatchDeleteButtonConfig(action);
        return this;
    }

    /**
     * 是否在编辑窗口保存、关闭后，刷新整个表格数据，如果 true 刷新所有行，如果 false ，刷新当前编辑的行
     * */
    public ListOptions refreshAfterEdit(boolean b) {
        this.config.setRefreshAfterEdit(b);
        return this;
    }

    /**
     * 设置页面标题
     * */
    public ListOptions pageTitle(String title) {
        this.config.setPageTitle(title);
        return this;
    }

    /**
     * 添加 JS
     * */
    public ListOptions addJs(String... js) {
        this.config.addJs(Arrays.asList(js));
        return this;
    }

    /**
     * 添加 CSS
     * */
    public ListOptions addCss(String... module) {
        this.config.addJs(Arrays.asList(module));
        return this;
    }

    //
    public ListOptions excel(boolean exp, boolean imp) {
        this.config.setEnableExportExcel(exp);
        this.config.setEnableImportExcel(imp);
        return this;
    }
}
