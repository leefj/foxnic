package com.github.foxnic.generator.builder.business.config;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class ServiceConfig {

    public static class  InjectDesc {

        private  Class type;
        private  Class annType;
        private  String varName;
        private  String resourceName;

        public InjectDesc(Class type, Class annType, String resourceName) {
            this.type=type;
            this.annType=annType;
            this.resourceName=resourceName;
            this.varName=type.getSimpleName();
            if(this.varName.startsWith("I")) {
                char c=type.getSimpleName().charAt(1);
                if(Character.isUpperCase(c)) {
                    this.varName=this.varName.substring(1);
                }
            }
            this.varName=this.varName.substring(0,1).toLowerCase()+this.varName.substring(1);
        }

        public Class getType() {
            return type;
        }

        public String getTypeName() {
            return type.getSimpleName();
        }

        public Class getAnnType() {
            return annType;
        }

        public String getAnnTypeName() {
            return annType.getSimpleName();
        }

        public String getVarName() {
            return varName;
        }

        public String getResourceName() {
            return resourceName;
        }

    }

    private List<InjectDesc> InjectDescs=new ArrayList<>();

    public List<InjectDesc> getInjectDescs() {
        return InjectDescs;
    }

    public void addInjectType(Class type,String resourceName) {
        this.InjectDescs.add(new InjectDesc(type, Resource.class,resourceName));
    }

    public void addAutowareTypes(Class[] autowareTypes) {
        for (Class type : autowareTypes) {
            if(type==null) continue;
            this.InjectDescs.add(new InjectDesc(type, Autowired.class,null));
        }
    }



}
