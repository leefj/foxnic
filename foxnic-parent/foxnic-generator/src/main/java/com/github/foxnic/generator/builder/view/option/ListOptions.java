package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ListConfig;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.util.JSFunctions;

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
     * 设置数据查询前调用的JS函数
     * */
    public ListOptions jsBeforeQuery(String jsFuncId) {

        JSFunctions.JSFunction func=this.context.getJsFunction(jsFuncId);
        if(func==null) {
            throw new IllegalArgumentException(jsFuncId+" Js 函数未定义");
        }
        if(!func.hasParam("conditions")) {
            throw new IllegalArgumentException(func.getName()+" 需要定义一个名为 conditions 的参数，用于接收查询条件");
        }
        func.prefixTab(1);
        this.config.setJsBeforeQueryFunc(func);
        return this;
    }
}
