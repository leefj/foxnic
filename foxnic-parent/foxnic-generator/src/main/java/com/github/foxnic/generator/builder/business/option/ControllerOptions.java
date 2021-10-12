package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.generator.builder.business.config.ControllerConfig;

public class ControllerOptions {

    private  ControllerConfig controllerConfig;

    public ControllerOptions(ControllerConfig controllerConfig) {
        this.controllerConfig=controllerConfig;
    }

    /**
     * 使用逻辑删除；默认为自动识别是否有删除标记字段自行判断是逻辑删除还是物理删除
     * */
    public ControllerOptions useLogicalDelete() {
        this.controllerConfig.setPhysicalDelete(false);
        return this;
    }
    /**
     * 使用物理删除；默认为自动识别是否有删除标记字段自行判断是逻辑删除还是物理删除
     * */
    public ControllerOptions usePhysicalDelete() {
        this.controllerConfig.setPhysicalDelete(true);
        return this;
    }

    /**
     * 使用物理删除；默认为自动识别是否有删除标记字段自行判断是逻辑删除还是物理删除
     * */
    public ControllerOptions saveMode(SaveMode saveMode) {
        this.controllerConfig.setSaveMode(saveMode);
        return this;
    }

    /**
     * 启用批量删除
     * */
    public ControllerOptions batchInsert() {
        this.controllerConfig.setEnableBatchInsert(true);
        return this;
    }





}
