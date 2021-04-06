package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class RelationManager {

    public List<Join> joins =new ArrayList<>();
    public List<PropertyRoute> properties =new ArrayList<>();

    /**
     * 创建一个 join
     * */
    public Join join(String sourceTable, String targetTable){
        Join join=new Join();
        joins.add(join);
        return join.join(sourceTable,targetTable);
    }

    /**
     * 创建一个 leftJoin
     * */
    public Join leftJoin(String sourceTable, String targetTable){
        Join join=new Join();
        joins.add(join);
        return join.leftJoin(sourceTable,targetTable);
    }

    /**
     * 创建一个 rightJoin
     * */
    public Join rightJoin(String sourceTable, String targetTable){
        Join join=new Join();
        joins.add(join);
        return join.rightJoin(sourceTable,targetTable);
    }

    /**
     * 配置一个关联属性
     * */
    public PropertyRoute property(Class<? extends Entity> poType, String property,String label,String detail){
        PropertyRoute prop=new PropertyRoute(poType,property,label,detail);
        properties.add(prop);
        return prop;
    }

    /**
     * 获得指定PO类型下的所有关联属性
     * */
    public <E extends Entity,T extends Entity> List<PropertyRoute> findProperties(Class<E> poType) {
        List<PropertyRoute> prs=new ArrayList<>();
        this.properties.forEach(p->{
            if(p.getPoType().equals(poType)){
                prs.add(p);
            }
        });
        return prs;
    }

    /**
     * 获得指定PO类型下指定类型的关联属性
     * */
    public <E extends Entity,T extends Entity> List<PropertyRoute> findProperties(Class<E> poType, Class<T> targetType) {
        List<PropertyRoute> prs=new ArrayList<>();
        this.properties.forEach(p->{
            if(p.getPoType().equals(poType) && ReflectUtil.isSubType(targetType,p.getTargetPoType())){
                prs.add(p);
            }
        });
        return prs;
    }
}
