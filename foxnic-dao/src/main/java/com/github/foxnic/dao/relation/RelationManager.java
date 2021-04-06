package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(Class<S> poType, String property,Class<T> targetPoType,String label,String detail){
        PropertyRoute<S,T> prop=new PropertyRoute<S,T>(poType,property,targetPoType,label,detail);
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
            if( ReflectUtil.isSubType(p.getPoType(), poType) && ReflectUtil.isSubType(targetType,p.getTargetPoType())){
                prs.add(p);
            }
        });
        return prs;
    }
    
	public <E extends Entity> PropertyRoute findProperties(Class<E> poType, String prop) {
		for (PropertyRoute p : properties) {
			if( ReflectUtil.isSubType(p.getPoType(), poType) &&  prop.equals(p.getProperty())){
               return p;
            }
		}
		return null;
	}
		 
    
    public List<Join> findJoinPath(String poTable, String targetTable) {
    	List<Join> result=new ArrayList<>();
    	List<Join> left=new ArrayList<>();
    	
    	for (Join join : joins) {
    		left.add(join);
    		left.add(join.getRevertJoin());
		}
 
    	findJoinPath(poTable, targetTable,left,result);
    	//Collections.reverse(result);
    	return result;
    }

	private void findJoinPath(String poTable, String targetTable,List<Join> left,List<Join> result) {
 
		//寻找关联
		List<Join> joins=new ArrayList<>();
		List<Join> joinsRevert=new ArrayList<>();
		//查找
		for (Join join : left) {
			//一次性关联
			if(join.getSourceTable().equalsIgnoreCase(poTable) && join.getTargetTable().equalsIgnoreCase(targetTable)) {
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
			findJoinPath(join.getTargetTable(), targetTable,newLeft,result);
			if(result.size()>i) {
				result.add(join);
			}
		}
 
	}


}
