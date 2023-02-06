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
     * 增加一个服务实现
     * */
    public ServiceOptions serviceImpl(String serviceImplNameSuffix,String desc) {
        this.serviceConfig.addServiceImpl(serviceImplNameSuffix,desc);
        return this;
    }
}
