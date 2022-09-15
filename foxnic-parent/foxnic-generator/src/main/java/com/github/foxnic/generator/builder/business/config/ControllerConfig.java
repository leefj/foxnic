package com.github.foxnic.generator.builder.business.config;

import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.meta.DBTableMeta;
import com.github.foxnic.generator.builder.view.config.FillWithUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerConfig {


    private DBTableMeta tableMeta;
    /**
     * 指定保存模式
     * */
    private SaveMode saveMode=SaveMode.DIRTY_OR_NOT_NULL_FIELDS;

    /**
     * 指定删除模式,如果未null，则自动识别是否有删除标记字段
     * */
    private Boolean isPhysicalDelete = null;

    public Boolean getEnableBatchInsert() {
        return enableBathchInsert;
    }

    public void setEnableBatchInsert(Boolean enableBathchInsert) {
        this.enableBathchInsert = enableBathchInsert;
    }

    /**
     * 是否加入批量删除接口
     * */
    private Boolean enableBathchInsert = false;

    public Boolean isPhysicalDelete() {
        return isPhysicalDelete;
    }

    public void setPhysicalDelete(boolean physicalDelete) {
        isPhysicalDelete = physicalDelete;
    }



    public void setTableMeta(DBTableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }


    public SaveMode getSaveMode() {
        return saveMode;
    }

    public void setSaveMode(SaveMode saveMode) {
        this.saveMode = saveMode;
    }

    private Map<String, FillWithUnit> fillWithUnits=new HashMap<>();

    public Map<String, FillWithUnit> getFillWithUnits() {
        return fillWithUnits;
    }

    public void setFillWithUnits(Map<String, FillWithUnit> fillWithUnits) {
        this.fillWithUnits = fillWithUnits;
    }

    private List<RestAPIConfig> restAPIConfigList=new ArrayList<>();

    public void  addRestAPIConfig(RestAPIConfig restAPIConfig) {

        for (RestAPIConfig config : restAPIConfigList) {
            if(restAPIConfig.getPath().equals(config.getPath())) {
                throw new IllegalArgumentException(restAPIConfig.getPath() + " 已存在");
            }
        }

        this.restAPIConfigList.add(restAPIConfig);
    }

    public List<RestAPIConfig> getRestAPIConfigList() {
        return restAPIConfigList;
    }

    private boolean inDoc=true;

    public boolean getInDoc() {
        return inDoc;
    }

    public void setInDoc(boolean inDoc) {
        this.inDoc = inDoc;
    }
}
