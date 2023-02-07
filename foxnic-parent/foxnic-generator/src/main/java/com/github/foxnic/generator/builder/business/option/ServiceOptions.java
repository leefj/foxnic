package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.builder.business.config.ServiceConfig;

public class ServiceOptions {

    private  ServiceConfig serviceConfig;

    public ServiceOptions(ServiceConfig serviceConfig) {
        this.serviceConfig=serviceConfig;
    }




    /**
     * 使用 Resource 方式注入
     * */
    public ServiceOptions inject(Class type, String resourceName,String notes) {
        this.serviceConfig.addInjectType(type,resourceName,notes,false);
        return this;
    }

    /**
     * 使用 Resource 方式注入
     * */
    public ServiceOptions injectMuliti(Class type,String notes) {
        this.serviceConfig.addInjectType(type,null,notes,true);
        return this;
    }

    /**
     * 使用 Autowared 方式注入
     * */
    public ServiceOptions autoware(Class type,String notes) {
        this.serviceConfig.addAutowareType(type,notes);
        return this;
    }

    /**
     * 设置在服务中调用关系保存的方法
     * */
    public ServiceOptions addRelationSaveAction(Class relationService, String slaveIdListProperty) {
        return this.addRelationSaveAction(relationService,slaveIdListProperty,relationService.getSimpleName());
    }

    /**
     * 设置在服务中调用关系保存的方法
     * */
    public ServiceOptions addRelationSaveAction(Class relationService, String slaveIdListProperty,String relationServiceNotes) {
        ServiceConfig.InjectDesc desc=this.serviceConfig.addAutowareType(relationService,relationServiceNotes);
        this.serviceConfig.addRelationSave(desc,slaveIdListProperty);
        return this;
    }

    /**
     * 增加一个服务实现
     * */
    public ServiceOptions serviceImpl(String serviceImplNameSuffix,String desc) {
        this.serviceConfig.addServiceImpl(serviceImplNameSuffix,desc);
        return this;
    }
}
