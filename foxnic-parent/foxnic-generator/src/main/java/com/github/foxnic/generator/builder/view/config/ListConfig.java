package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListConfig {

    private String pageTitle=null;

    private Boolean isRefreshAfterEdit=false;

    private List<String> defaultColumns=new ArrayList<>();

    private String tableSortField=null;
    private Boolean tableSortAsc=null;

    private  ActionConfig createNewButtonConfig = new ActionConfig();
    private  ActionConfig batchDeleteButtonConfig = new ActionConfig();



    private  ActionConfig formViewButtonConfig = new ActionConfig();
    private  ActionConfig modifyButtonConfig = new ActionConfig();
    private  ActionConfig deleteButtonConfig = new ActionConfig();



    public ActionConfig getCreateNewButtonConfig() {
        return createNewButtonConfig;
    }

    public void setCreateNewButtonConfig(ActionConfig createNewButtonConfig) {
        this.createNewButtonConfig = createNewButtonConfig;
    }

    public ActionConfig getBatchDeleteButtonConfig() {
        return batchDeleteButtonConfig;
    }

    public void setBatchDeleteButtonConfig(ActionConfig batchDeleteButtonConfig) {
        this.batchDeleteButtonConfig = batchDeleteButtonConfig;
    }

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



    private int operateColumnWidth=160;

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

    private List<ActionConfig> opColumnButtons=new ArrayList<>();

    public boolean getHasOperateColumn() {
        boolean disableAll=disableModify && disableSingleDelete && disableFormView && this.opColumnButtons.isEmpty() && opColumnMenus.isEmpty();
         return !disableAll;
    }


    public List<ActionConfig> getOpColumnButtons() {
        return opColumnButtons;
    }

    public void addOpColumnButton(ActionConfig opColumnButton) {
        this.opColumnButtons.add(opColumnButton);
    }

    private List<ActionConfig> opColumnMenus=new ArrayList<>();

    public List<ActionConfig> getOpColumnMenus() {
        return opColumnMenus;
    }

    public void addOpColumnMenu(ActionConfig opColumnMenu) {
        this.opColumnMenus.add(opColumnMenu);
    }

    public void clearOpColumnMenus() {
        this.opColumnMenus.clear();
    }

    public void clearOpColumnButtons() {
        this.opColumnButtons.clear();
    }

    public void clearToolButtons() {
        this.toolButtons.clear();
    }


    private List<ActionConfig> toolButtons=new ArrayList<>();

    public List<ActionConfig> getToolButtons() {
        return toolButtons;
    }

    public void addToolButton(ActionConfig toolButton) {
        this.toolButtons.add(toolButton);
    }

    public boolean getMarginDisable() {
        return marginDisable;
    }

    public void setMarginDisable(boolean marginDisable) {
        this.marginDisable = marginDisable;
    }

    private boolean marginDisable=false;


    public List<JsVariable> getJsVariables() {
        return jsVariables;
    }

    private  List<JsVariable> jsVariables=new ArrayList<>();

    public void addJsVariable(String name, String value, String note) {
        jsVariables.add(new JsVariable(name,value,note));
    }

    public boolean getMulitiSelect() {
        return mulitiSelect;
    }

    public void setMulitiSelect(boolean mulitiSelect) {
        this.mulitiSelect = mulitiSelect;
    }

    private boolean mulitiSelect=true;

    public ActionConfig getFormViewButtonConfig() {
        return formViewButtonConfig;
    }

    public void setFormViewButtonConfig(ActionConfig formViewButtonConfig) {
        this.formViewButtonConfig = formViewButtonConfig;
    }

    public ActionConfig getModifyButtonConfig() {
        return modifyButtonConfig;
    }

    public void setModifyButtonConfig(ActionConfig modifyButtonConfig) {
        this.modifyButtonConfig = modifyButtonConfig;
    }

    public ActionConfig getDeleteButtonConfig() {
        return deleteButtonConfig;
    }

    public void setDeleteButtonConfig(ActionConfig deleteButtonConfig) {
        this.deleteButtonConfig = deleteButtonConfig;
    }

    public Boolean getRefreshAfterEdit() {
        return isRefreshAfterEdit;
    }

    public void setRefreshAfterEdit(Boolean refreshAfterEdit) {
        isRefreshAfterEdit = refreshAfterEdit;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    private  Set jsList =new HashSet<>();

    /**
     * 添加 JS 模块
     * */
    public void addJs(List<String> js) {
        jsList.addAll(js);
    }

    public Set getJsList() {
        return jsList;
    }

    private  Set cssList =new HashSet<>();

    /**
     * 添加 JS 模块
     * */
    public void addCss(List<String> css) {
        cssList.addAll(css);
    }

    public Set getCssList() {
        return cssList;
    }


    private boolean enableImportExcel=false;
    private boolean enableExportExcel=false;

    public boolean isEnableImportExcel() {
        return enableImportExcel;
    }

    public void setEnableImportExcel(boolean enableImportExcel) {
        this.enableImportExcel = enableImportExcel;
    }

    public boolean isEnableExportExcel() {
        return enableExportExcel;
    }

    public void setEnableExportExcel(boolean enableExportExcel) {
        this.enableExportExcel = enableExportExcel;
    }

    public void setTableSortAsc(Boolean tableSortAsc) {
        this.tableSortAsc = tableSortAsc;
    }

    public void setTableSortField(String tableSortField) {
        this.tableSortField = tableSortField;
    }

    public Boolean getTableSortAsc() {
        return tableSortAsc;
    }

    public String getTableSortField() {
        return tableSortField;
    }

    private String queryApi = null;
    public void setQueryApi(String url) {
        this.queryApi=url;
    }
    public String getQueryApi() {
        return queryApi;
    }


    private String deleteApi = null;

    public void setDeleteApi(String url) {
        this.deleteApi = url;
    }

    public String getDeleteApi() {
        return deleteApi;
    }

    private String batchDeleteApi = null;

    public String getBatchDeleteApi() {
        return batchDeleteApi;
    }

    public void setBatchDeleteApi(String batchDeleteApi) {
        this.batchDeleteApi = batchDeleteApi;
    }


}
