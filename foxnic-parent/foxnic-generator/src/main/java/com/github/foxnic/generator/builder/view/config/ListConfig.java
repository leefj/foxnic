package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.util.JSFunctions;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.List;

public class ListConfig {

    private List<String> defaultColumns=new ArrayList<>();



    public void setInputColumnLayout(Object[] inputs) {
        defaultColumns.clear();
        for (Object input : inputs) {
            if(input==null) continue;
            if(input instanceof  String) {
                if(StringUtil.isBlank(input)) continue;
                defaultColumns.add(input.toString());
            } else if(input instanceof DBField) {
                if(StringUtil.isBlank(input)) continue;
                defaultColumns.add(((DBField)input).name());
            } else {
                throw new RuntimeException("仅支持 DBField 与 String 类型");
            }
        }
    }

    public List<String> getDefaultColumns() {
        return defaultColumns;
    }



    private int operateColumnWidth=125;

    public void setOperateColumnWidth(int width) {
        this.operateColumnWidth=width;
    }

    public int getOperateColumnWidth() {
        return operateColumnWidth;
    }

    public boolean getDisableCreateNew() {
        return disableCreateNew;
    }

    private boolean disableCreateNew=false;

    public void setDisableCreateNew(boolean b) {
        this.disableCreateNew=b;
    }

    private boolean disableModify=false;
    private boolean disableSingleDelete=false;
    private boolean disableBatchDelete=false;
    private boolean disableFormView=false;
    private boolean disableSpaceColumn=false;


    public boolean getDisableModify() {
        return disableModify;
    }

    public void setDisableModify(boolean disableModify) {
        this.disableModify = disableModify;
    }

    public boolean getDisableSingleDelete() {
        return disableSingleDelete;
    }

    public void setDisableSingleDelete(boolean disableSingleDelete) {
        this.disableSingleDelete = disableSingleDelete;
    }

    public boolean getDisableBatchDelete() {
        return disableBatchDelete;
    }

    public void setDisableBatchDelete(boolean disableBatchDelete) {
        this.disableBatchDelete = disableBatchDelete;
    }

    public boolean getDisableFormView() {
        return disableFormView;
    }

    public void setDisableFormView(boolean disableFormView) {
        this.disableFormView = disableFormView;
    }

    public boolean getDisableSpaceColumn() {
        return disableSpaceColumn;
    }

    public void setDisableSpaceColumn(boolean disableSpaceColumn) {
        this.disableSpaceColumn = disableSpaceColumn;
    }


    public boolean getHasOperateColumn() {
        boolean disableAll=disableSingleDelete && disableFormView && this.opColumnButtons.isEmpty();
         return !disableAll;
    }


    public List<ListActionConfig> getOpColumnButtons() {
        return opColumnButtons;
    }

    public void addOpColumnButtons(ListActionConfig opColumnButton) {
        this.opColumnButtons.add(opColumnButton);
    }

    private List<ListActionConfig> opColumnButtons=new ArrayList<>();



    private JSFunctions.JSFunction jsBeforeQueryFunc=null;

    public void setJsBeforeQueryFunc(JSFunctions.JSFunction jsBeforeQueryFunc) {
        this.jsBeforeQueryFunc=jsBeforeQueryFunc;
    }

    public  JSFunctions.JSFunction getJsBeforeQueryFunc() {
        return jsBeforeQueryFunc;
    }
}
