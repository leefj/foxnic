package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.generator.builder.business.config.ServiceConfig;

public class ServiceOptions {

    private  ServiceConfig serviceConfig;

    public ServiceOptions(ServiceConfig serviceConfig) {
        this.serviceConfig=serviceConfig;
    }




    /**
     * 使用 Resource 方式注入
     * */
    public ServiceOptions inject(Class type, String resourceName) {
        this.serviceConfig.addInjectType(type,resourceName);
        return this;
    }

    /**
     * 使用 Autowared 方式注入
     * */
    public ServiceOptions autoware(Class... types) {
        for (Class type : types) {
            this.serviceConfig.addAutowareType(type);
        }
        return this;
    }

    /**
     * 设置在服务中调用关系保存的方法
     * */
    public ServiceOptions addRelationSaveAction(Class relationService, String slaveIdListProperty) {
        ServiceConfig.InjectDesc desc=this.serviceConfig.addAutowareType(relationService);
        this.serviceConfig.addRelationSave(desc,slaveIdListProperty);
        return this;
    }

    /**
     * 支持多服务实现，即一个服务接口可以有多个实现，具体使用哪个在系统配置中配置
     * */
    public ServiceOptions multiImpl() {
        this.serviceConfig.setMultiImpl(true);
        return this;
    }
}
