package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.ListActionConfig;
import com.github.foxnic.generator.builder.view.config.ListConfig;

public class ListOperationColumnOptions {
    private ListConfig config;

    public ListOperationColumnOptions(ListConfig config) {
        this.config=config;
    }

    /**
     * 设置操作列的宽度，默认值 125
     * */
    public ListOperationColumnOptions width(int width) {
        this.config.setOperateColumnWidth(width);
        return this;
    }

    public ListActionConfig addWindowOpenButton(String label,String uri) {
        ListActionConfig action=new ListActionConfig();
        action.setLabel(label);
        action.setWindowTitle(label);
        action.setActionType(ListActionConfig.ActionType.open_window);
        action.setUri(uri);
        action.setId("operate-"+this.config.getOpColumnButtons().size());
        this.config.addOpColumnButtons(action);
        return action;
    }
}
