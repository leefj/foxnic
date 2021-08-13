package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.generator.builder.business.config.ServiceConfig;

public class ServiceOption {

    private  ServiceConfig serviceConfig;

    public ServiceOption(ServiceConfig serviceConfig) {
        this.serviceConfig=serviceConfig;
    }




    /**
     * 使用 Resource 方式注入
     * */
    public ServiceOption inject(Class type,String resourceName) {
        this.serviceConfig.addInjectType(type,resourceName);
        return this;
    }

    /**
     * 使用 Autowared 方式注入
     * */
    public ServiceOption autoware(Class... types) {
        for (Class type : types) {
            this.serviceConfig.addAutowareType(type);
        }
        return this;
    }

    /**
     * 设置在服务中调用关系保存的方法
     * */
    public ServiceOption addRelationSaveAction(Class relationService, String slaveIdListProperty) {
        ServiceConfig.InjectDesc desc=this.serviceConfig.addAutowareType(relationService);
        this.serviceConfig.addRelationSave(desc,slaveIdListProperty);

        return this;
    }
}
