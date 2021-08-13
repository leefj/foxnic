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
        this.serviceConfig.addAutowareTypes(types);
        return this;
    }

}
