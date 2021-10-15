package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.dao.entity.EntityNavigator;
import com.github.foxnic.generator.builder.business.config.ControllerConfig;
import com.github.foxnic.generator.builder.view.config.FillByUnit;
import com.github.foxnic.generator.builder.view.config.FillWithUnit;
import com.github.foxnic.generator.util.ConfigCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void collectControllerFillWith() {
        //if(this.controllerConfig.getFillWithUnits()!=null) return;
        Throwable th=new Throwable();
        StackTraceElement el=th.getStackTrace()[2];
        Map<String,FillWithUnit> units=new HashMap<>();
        units.put("fillQueryPagedList",ConfigCollector.collectControllerFillWith(el,"fillQueryPagedList"));
        units.put("fillGetById",ConfigCollector.collectControllerFillWith(el,"fillGetById"));
        units.put("fillGetByIds",ConfigCollector.collectControllerFillWith(el,"fillGetByIds"));
        units.put("fillQueryList",ConfigCollector.collectControllerFillWith(el,"fillQueryList"));
        this.controllerConfig.setFillWithUnits(units);
    }

    /**
     * 指定填充 QueryList 方法的填充
     * */
    public EntityNavigatorOptions fillQueryList() {
        collectControllerFillWith();
        return new EntityNavigatorOptions();
    }

    /**
     * 指定填充 QueryPagedList 方法的填充
     * */
    public EntityNavigatorOptions fillQueryPagedList() {
        collectControllerFillWith();
        return new EntityNavigatorOptions();
    }

    /**
     * 指定填充 getById 方法的填充
     * */
    public EntityNavigatorOptions fillGetById() {
        collectControllerFillWith();
        return new EntityNavigatorOptions();
    }

    /**
     * 指定填充 getByIds 方法的填充
     * */
    public EntityNavigatorOptions fillGetByIds() {
        collectControllerFillWith();
        return new EntityNavigatorOptions();
    }
}
