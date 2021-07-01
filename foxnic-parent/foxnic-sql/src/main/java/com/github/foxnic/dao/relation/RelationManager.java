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
    		relationManager.config();
    	}
    	
    	this.joins.addAll(relationManager.joins);
   
    	for (PropertyRoute p : relationManager.properties) {
			 if(isPropertyExists(p.getSourcePoType(), p.getProperty())) {
				 throw new IllegalArgumentException(p.getSourcePoType().getName()+"属性["+p.getProperty()+"]重复添加");
			 }
			 this.properties.add(p);
		}
	}
    
    protected abstract void config();
    
    public void reconfig() {
//         JoinPathFinder.clearCache();
		 this.joins.clear();
		 this.properties.clear();
		 this.config();
		 if(relationManagers!=null) {
			for (RelationManager rm : relationManagers) {
				rm.reconfig();
				this.merge(rm);
			}
		 }
    }
    
//    public void validate() {
//    	for (PropertyRoute prop : this.properties) {
//    		if(prop.isIgnoreJoin()) continue;
//			this.findJoinPath(prop,prop.getSourceTable(), prop.getTargetTable(), prop.getUsingProperties(), prop.getRouteTables(), prop.getRouteFields());
//		}
//    }
    

    private BeanNameUtil beanNameUtil=new BeanNameUtil();
//    /**
//     * 创建一个 join ， 建立两表连接关系
//     * */
//    public Join join(String sourceTable, String targetTable){
//        Join join=new Join();
//        joins.add(join);
//        return join.join(sourceTable,targetTable);
//    }
    
    
//    public Join from(DBField... sourceField) {
//        Join join=new Join();
//        joins.add(join);
//        join.from(new JoinPoint(sourceField));
//        return join;
//    }

//    /**
//     * 创建一个 leftJoin ， 建立两表连接关系
//     * */
//    public Join leftJoin(String sourceTable, String targetTable){
//        Join join=new Join();
//        joins.add(join);
//        return join.leftJoin(sourceTable,targetTable);
//    }
//
//    /**
//     * 创建一个 rightJoin ， 建立两表连接关系
//     * */
//    public Join rightJoin(String sourceTable, String targetTable){
//        Join join=new Join();
//        joins.add(join);
//        return join.rightJoin(sourceTable,targetTable);
//    }
    
    
    private boolean isPropertyExists(Class poType,String prop) {
    	for (PropertyRoute p : properties) {
    	 
//    		if(prop.equals("allChildren") && p.getPoType().getName().equals("com.scientific.tailoring.domain.system.Menu") && p.getProperty().equals("allChildren")) {
//    			System.out.println();
//    		}
    		
			if(prop.equals(p.getProperty()) && poType.equals(p.getSourcePoType())) return true;
		}
    	return false;
    }

    /**
     * 配置一个关联属性
     * */
    public <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(Class<S> poType, String property,Class<T> targetPoType,String label,String detail){
        if(isPropertyExists(poType,property)) {
        	throw new IllegalArgumentException(poType.getName()+"属性["+property+"]重复添加");
        }
    	PropertyRoute<S,T> prop=new PropertyRoute<S,T>(poType,property,targetPoType,label,detail);
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
