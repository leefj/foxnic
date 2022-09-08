package com.github.foxnic.generator.builder.business.option;

import com.github.foxnic.generator.builder.business.config.RestAPIConfig;
import com.github.foxnic.generator.builder.model.PojoProperty;

public class APIOptions {

    private  RestAPIConfig config;
    public APIOptions(RestAPIConfig config) {
        this.config=config;
    }

    public APIOptions addSimpleParameter(Class type,String name,String title,boolean required,String example,String desc) {
        PojoProperty property=PojoProperty.simple(type,name,title,desc);
        this.config.addParameter(property);
        this.config.setParameterExtras(name,required,example);
        return this;
    }

    public APIOptions addListParameter(Class elType,String name,String title,boolean required,String example,String desc) {
        PojoProperty property=PojoProperty.list(elType,name,title,desc);
        this.config.addParameter(property);
        this.config.setParameterExtras(name,required,example);
        return this;
    }

    public APIOptions addMapParameter(Class keyType,Class elType,String name,String title,boolean required,String example,String desc) {
        PojoProperty property=PojoProperty.map(keyType,elType,name,title,desc);
        this.config.addParameter(property);
        this.config.setParameterExtras(name,required,example);
        return this;
    }

    public APIOptions simpleResult(Class resultDataType,String desc) {
        this.config.setResultType("simple");
        this.config.setResultDesc(desc);
        this.config.setResultDataType(resultDataType);
        return this;
    }

    public APIOptions listResult(Class elType,String desc) {
        this.config.setResultType("list");
        this.config.setResultDesc(desc);
        this.config.setResultElType(elType);
        return this;
    }

    public APIOptions pagedListResult(Class elType,String desc) {
        this.config.setResultType("pagedList");
        this.config.setResultDesc(desc);
        this.config.setResultElType(elType);
        return this;
    }

    public APIOptions mapResult(Class keyType,Class elType,String desc) {
        this.config.setResultType("map");
        this.config.setResultKeyType(keyType);
        this.config.setResultDesc(desc);
        this.config.setResultElType(elType);
        return this;
    }

}
