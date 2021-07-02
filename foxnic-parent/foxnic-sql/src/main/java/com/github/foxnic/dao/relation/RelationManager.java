package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.meta.DBField;
import com.github.foxnic.sql.meta.DBTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class RelationManager {

    private List<Join> joins =new ArrayList<>();
    
    @SuppressWarnings("rawtypes")
    private List<PropertyRoute> properties =new ArrayList<>();
    
    protected List<RelationManager> relationManagers;
    
    public RelationManager(RelationManager... rms) {
    	for (RelationManager rm : rms) {
			this.merge(rm);
		}
    	relationManagers=new ArrayList<>();
    	relationManagers.addAll(Arrays.asList(rms));
    }
    
    /**
     * 合并
     * */
    public void merge(RelationManager relationManager) {
    	
    	if(relationManager.joins.isEmpty() || relationManager.properties.isEmpty()) {
			relationManager.clear();
    		relationManager.config();
    	}
    	
    	this.joins.addAll(relationManager.joins);
   
    	for (PropertyRoute p : relationManager.properties) {
			 if(getProperty(p.getSourcePoType(), p.getProperty())!=null) {
				 throw new IllegalArgumentException(p.getSourcePoType().getName()+"属性["+p.getProperty()+"]重复添加");
			 }
			 this.properties.add(p);
		}
	}
    
    protected abstract void config();

    protected void clear() {
		this.joins.clear();
		this.properties.clear();
	}

	public void reconfig() {
		this.clear();
		this.config();
		if (relationManagers != null) {
			for (RelationManager rm : relationManagers) {
				rm.reconfig();
				this.merge(rm);
			}
		}
	}

    private BeanNameUtil beanNameUtil=BeanNameUtil.instance();
    
    
    private <S extends Entity,T extends Entity> PropertyRoute<S,T> getProperty(Class poType,String prop) {
    	for (PropertyRoute p : properties) {
			if(prop.equals(p.getProperty()) && poType.equals(p.getSourcePoType()))
				return p;
		}
    	return null;
    }

    /**
     * 配置一个关联属性
     * */
    public <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(Class<S> poType, String property,Class<T> targetPoType,String label,String detail){
		PropertyRoute<S,T> prop=getProperty(poType,property);
    	if(prop!=null) {
        	throw new IllegalArgumentException(poType.getName()+"属性["+property+"]重复添加");
        }
    	prop=new PropertyRoute<S,T>(poType,property,targetPoType,label,detail);
        properties.add(prop);
        return prop;
    }

    /**
     * 获得指定PO类型下的所有关联属性
     * */
    @SuppressWarnings("unchecked")
	public <E extends Entity,T extends Entity> List<PropertyRoute<E,T>> findProperties(Class<E> poType) {
        List<PropertyRoute<E,T>> prs=new ArrayList<>();
        this.properties.forEach(p->{
            if(p.getSourcePoType().equals(poType)){
                prs.add(p);
            }
        });
        return prs;
    }

    
    /**
     * 获得指定PO类型下指定类型的关联属性
     * */
    @SuppressWarnings("unchecked")
	<E extends Entity,T extends Entity> List<PropertyRoute<E,T>> findProperties(Class<E> poType, Class<T> targetType) {
        List<PropertyRoute<E,T>> prs=new ArrayList<>();
        this.properties.forEach(p->{
            if( ReflectUtil.isSubType(p.getSourcePoType(), poType) && ReflectUtil.isSubType(targetType,p.getTargetPoType())){
                prs.add(p);
            }
        });
        return prs;
    }
    
	@SuppressWarnings("rawtypes")
	<E extends Entity> PropertyRoute findProperties(Class<E> poType, String prop) {
		for (PropertyRoute p : properties) {
			if(prop.equals(p.getProperty())) {
				if( ReflectUtil.isSubType(p.getSourcePoType(), poType)){
	               return p;
	            }
			}
		}
		return null;
	}
		 
    
    List<Join> findJoinPath(PropertyRoute prop, DBTable poTable, DBTable targetTable,DBField[] usingProps) {



//        for (int i = 0; i < routeTables; i++) {
//
//        }


//    	return (new JoinPathFinder(prop,joins,poTable, targetTable, usingProps, routeTables, routeFields)).find();
    	return  prop.getJoins();
    }



}
