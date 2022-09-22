package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.dao.data.SaveMode;
import com.github.foxnic.generator.builder.business.config.ControllerConfig;
import com.github.foxnic.generator.builder.business.config.RestAPIConfig;
import com.github.foxnic.generator.builder.view.config.FillWithUnit;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.util.ConfigCollector;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

public class ControllerOptions {

    private  ControllerConfig controllerConfig;

    private ModuleContext context;
    public ControllerOptions(ModuleContext context, ControllerConfig controllerConfig) {
        this.controllerConfig=controllerConfig;
        this.context=context;
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



    /**
     * 添加一个api接口
     * @param  title java 方法名称
     * @param path 请求路径，相对路径
     * @param  method 请求方式
     * */
    public APIOptions restApi(String title,String methodName, String path, RequestMethod method,String desc) {

        RestAPIConfig config=new RestAPIConfig(methodName);
        config.setPath(path);
        config.setTitle(title);
        config.setMethod(method);
        config.setComment(desc);
        this.controllerConfig.addRestAPIConfig(config);
        return new APIOptions(config);
    }

    public ControllerOptions inDoc(boolean inDoc) {
        this.controllerConfig.setInDoc(inDoc);
        return this;
    }

    public ControllerOptions restApiTagDir(String dir) {
        this.controllerConfig.setApiTagDir(dir);
        return this;
    }

    public void topic(String topic) {
        this.context.setTopic(topic);
    }
}
