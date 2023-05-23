package com.github.foxnic.generator.builder.business.config;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.dao.meta.DBColumnMeta;
import com.github.foxnic.dao.meta.DBTableMeta;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceConfig {





    public static class  InjectDesc {

        private  Class type;
        private  Class annType;
        private  String varName;
        private  String resourceName;

        private boolean muliti = false;

        private String notes;

        public InjectDesc(Class type, Class annType, String resourceName,String notes,boolean muliti) {

            this.muliti=muliti;
            this.type=type;
            this.notes= notes;
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

        public String getNotes() {
            return notes;
        }

        public Class getType() {
            return type;
        }

        public boolean getMuliti() {
            return muliti;
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


    public static class  RelationSaveDesc {

        private InjectDesc injectDesc;
        private String slaveIdListProperty;
        private DBTableMeta tableMeta;

        public InjectDesc getInjectDesc() {
            return injectDesc;
        }

        public String getSlaveIdListProperty() {
            return slaveIdListProperty;
        }

        public RelationSaveDesc(InjectDesc injectDesc,DBTableMeta tableMeta, String slaveIdListProperty) {

            //检查方法是否存在
            Method[] ms=injectDesc.getType().getDeclaredMethods();
            boolean hasSaveRelationMethod=false;
            for (Method m : ms) {
                if(m.getName().equals("saveRelation")) {
                    hasSaveRelationMethod=true;
                    break;
                }
            }
            if(!hasSaveRelationMethod) {
                throw new IllegalArgumentException(injectDesc.getType().getName()+".saveRelation 方法不存在");
            }

            this.tableMeta=tableMeta;
            this.injectDesc=injectDesc;
            this.slaveIdListProperty=slaveIdListProperty;
        }

        public String getIdPropertyGetter() {
            DBColumnMeta cm=this.tableMeta.getPKColumns().get(0);
            String getter=BeanNameUtil.instance().getGetterMethodName(cm.getColumn(), false,false);
            return getter;
        }

        public String getSlavePropertyGetter() {
            String getter=BeanNameUtil.instance().depart(this.slaveIdListProperty);
            getter=BeanNameUtil.instance().getGetterMethodName(getter, false,false);
            return getter;
        }



    }




    private  DBTableMeta tableMeta;

    public void setTableMeta(DBTableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }


    private List<InjectDesc> InjectDescs=new ArrayList<>();

    private Map<String,String> serviceImplMap=null;
    public void addServiceImpl(String serviceImplNameSuffix,String desc) {
        if(serviceImplMap==null) serviceImplMap=new HashMap<>();
        serviceImplMap.put(serviceImplNameSuffix,desc);
    }
    public  boolean hasExtraServiceImpl() {
        if(serviceImplMap==null || serviceImplMap.size()==0) return false;
        return true;
    }


    public Map<String,String> getServiceImplNameSuffix() {
        return this.serviceImplMap;
    }

    public List<InjectDesc> getInjectDescs() {
        return InjectDescs;
    }

    private List<RelationSaveDesc> relationSaveDescs=new ArrayList<>();

    public List<RelationSaveDesc> getRelationSaveDescs() {
        return relationSaveDescs;
    }

    public void addInjectType(Class type,String resourceName,String notes,boolean muliti) {
        this.InjectDescs.add(new InjectDesc(type, Resource.class,resourceName,notes,muliti));
    }

    public InjectDesc addAutowareType(Class type,String notes) {
        InjectDesc desc=new InjectDesc(type, Autowired.class,null,notes,false);
        this.InjectDescs.add(desc);
        return desc;
    }


    public RelationSaveDesc addRelationSave(InjectDesc injectDesc,String slaveIdListProperty) {
        RelationSaveDesc rsd=new RelationSaveDesc(injectDesc,this.tableMeta,slaveIdListProperty);
        relationSaveDescs.add(rsd);
        return rsd;
    }

    public void  reset() {
        this.relationSaveDescs.clear();
        this.InjectDescs.clear();
    }

}
