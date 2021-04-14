package com.github.foxnic.dao.relation;

import java.util.ArrayList;
import java.util.List;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;

public class RelationManager {

    public List<Join> joins =new ArrayList<>();
    
    @SuppressWarnings("rawtypes")
	public List<PropertyRoute> properties =new ArrayList<>();
    
    
    public RelationManager(RelationManager... rms) {
    	for (RelationManager rm : rms) {
			this.merge(rm);
		}
    }
    
    /**
     * 合并
     * */
    public void merge(RelationManager relationManager) {
    	this.joins.addAll(relationManager.joins);
   
    	for (PropertyRoute p : relationManager.properties) {
			 if(isPropertyExists(p.getPoType(), p.getProperty())) {
				 throw new IllegalArgumentException(p.getPoType().getName()+"属性["+p.getProperty()+"]重复添加");
			 }
			 this.properties.add(p);
		}
	}
    

    private BeanNameUtil beanNameUtil=new BeanNameUtil();
    /**
     * 创建一个 join ， 建立两表连接关系
     * */
    public Join join(String sourceTable, String targetTable){
        Join join=new Join();
        joins.add(join);
        return join.join(sourceTable,targetTable);
    }

    /**
     * 创建一个 leftJoin ， 建立两表连接关系
     * */
    public Join leftJoin(String sourceTable, String targetTable){
        Join join=new Join();
        joins.add(join);
        return join.leftJoin(sourceTable,targetTable);
    }

    /**
     * 创建一个 rightJoin ， 建立两表连接关系
     * */
    public Join rightJoin(String sourceTable, String targetTable){
        Join join=new Join();
        joins.add(join);
        return join.rightJoin(sourceTable,targetTable);
    }
    
    
    private boolean isPropertyExists(Class poType,String prop) {
    	for (PropertyRoute p : properties) {
    	 
    		if(prop.equals("allChildren") && p.getPoType().getName().equals("com.scientific.tailoring.domain.system.Menu") && p.getProperty().equals("allChildren")) {
    			System.out.println();
    		}
			if(prop.equals(p.getProperty()) && poType.equals(p.getPoType())) return true;
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
            if(p.getPoType().equals(poType)){
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
            if( ReflectUtil.isSubType(p.getPoType(), poType) && ReflectUtil.isSubType(targetType,p.getTargetPoType())){
                prs.add(p);
            }
        });
        return prs;
    }
    
	@SuppressWarnings("rawtypes")
	<E extends Entity> PropertyRoute findProperties(Class<E> poType, String prop) {
		for (PropertyRoute p : properties) {
			if(prop.equals(p.getProperty())) {
				if( ReflectUtil.isSubType(p.getPoType(), poType)){
	               return p;
	            }
			}
		}
		return null;
	}
		 
    
    List<Join> findJoinPath(String poTable, String targetTable,String[] usingProps) {
    	List<Join> result=new ArrayList<>();
    	List<Join> left=new ArrayList<>();
    	
    	//排除空数组的情况
    	if(usingProps!=null && usingProps.length==0) {
    		usingProps=null;
    	}
    	
    	for (Join join : joins) {
    		left.add(join);
    		left.add(join.getRevertJoin());
		}
 
    	findJoinPath(poTable, targetTable,left,result,usingProps);
    	//Collections.reverse(result);
    	return result;
    }

	private void findJoinPath(String poTable, String targetTable,List<Join> left,List<Join> result,String[] usingProps) {
 
		//寻找关联
		List<Join> joins=new ArrayList<>();
		List<Join> joinsRevert=new ArrayList<>();
		String propertyName=null;
		String departedPropertyName=null;
		String fieldName=null;
		 
		//查找
		for (Join join : left) {
			//一次性关联
			boolean isSourceTable=join.getSourceTable().equalsIgnoreCase(poTable);
			boolean isFieldMatch=true;
			if(usingProps!=null) {
				List<String> srcFields=join.getSourceTableFields();
				if(usingProps.length!=srcFields.size()) {
					throw new RuntimeException("关联字段数量错误");
				}
				
				for (int i = 0; i < usingProps.length; i++) {
					propertyName=usingProps[i];
					departedPropertyName=beanNameUtil.depart(propertyName);
					fieldName=srcFields.get(i);
					isFieldMatch= isFieldMatch && (fieldName.equalsIgnoreCase(propertyName) || fieldName.equalsIgnoreCase(departedPropertyName));
					if(!isFieldMatch) break;
				}
			}
			
			boolean isTargetTable=join.getTargetTable().equalsIgnoreCase(targetTable);
			
			if( isSourceTable && isFieldMatch && isTargetTable ) {
				result.add(join);
				return;
			}
			if(join.getSourceTable().equalsIgnoreCase(poTable)) {
				joins.add(join);
				joinsRevert.add(join.getRevertJoin());
			}
		}
		//没有找到匹配的
		if(joins.isEmpty()) return;
 
		//
		left.removeAll(joins);
		left.removeAll(joinsRevert);
		if(left.isEmpty()) {
			return;
		}
 
		int i=0;
		for (Join join : joins) {
			List<Join> newLeft=new ArrayList<>();
			newLeft.addAll(left);
			i=result.size();
			findJoinPath(join.getTargetTable(), targetTable,newLeft,result,null);
			if(result.size()>i) {
				result.add(join);
			}
		}
 
	}

	 


}
