package com.github.foxnic.dao.relation;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;

import java.util.*;

public abstract class RelationManager {

    private List<Join> joins =new ArrayList<>();
    
    @SuppressWarnings("rawtypes")
    private List<PropertyRoute> properties =new ArrayList<>();

	private Map<Class,List<PropertyRoute>> map =new HashMap<>();

	private  List<PropertyRoute> getProperties(Class poType) {
		if(poType==null) return new ArrayList<>();
		List<PropertyRoute> list=map.get(poType);
		while (list==null) {
			poType = poType.getSuperclass();
			if(poType==null) break;
			list=map.get(poType);
		}
		return list;
	}

//	private void initMapIf() {
//		if(!map.isEmpty()) return;
//		for (PropertyRoute property : properties) {
//			List<PropertyRoute> list=map.get(property.getSourcePoType());
//			if(list==null) {
//				list=new ArrayList<>();
//				map.put(property.getSourcePoType(),list);
//			}
//			if(!list.contains(property)) {
//				list.add(property);
//			}
//		}
//		System.out.println("ok");
//	}

    
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
			 this.putToMap(p);
		}
	}
    
    protected abstract void config();

    protected void clear() {
		this.joins.clear();
		this.properties.clear();
		this.map.clear();
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
        //
		putToMap(prop);
		//
        return prop;
    }

	private <T extends Entity, S extends Entity> void putToMap(PropertyRoute<S,T> prop) {
		List<PropertyRoute> list=map.get(prop.getSourcePoType());
		if(list==null) {
			list=new ArrayList<>();
			map.put(prop.getSourcePoType(),list);
		}
		if(!list.contains(prop)) {
			list.add(prop);
		}
	}


	private <S extends Entity,T extends Entity> PropertyRoute<S,T> getProperty(Class poType,String prop) {
		List<PropertyRoute> list=getProperties(poType);
		if(list==null) return null;
    	for (PropertyRoute p : list) {
			if(prop.equals(p.getProperty()) && poType.equals(p.getSourcePoType()))
				return p;
		}
		return null;
	}

    /**
     * 获得指定PO类型下的所有关联属性
     * */
    @SuppressWarnings("unchecked")
	public <E extends Entity,T extends Entity> List<PropertyRoute<E,T>> findProperties(Class<E> poType) {
		List<PropertyRoute> list=getProperties(poType);
		List<PropertyRoute<E,T>> prs=new ArrayList<>();
		if(list==null) return prs;
		for (PropertyRoute propertyRoute : list) {
			prs.add(propertyRoute);
		}
//		return  list;
//    	List<PropertyRoute<E,T>> prs=new ArrayList<>();
//        list.forEach(p->{
//            if(p.getSourcePoType().equals(poType)){
//                prs.add(p);
//            }
//        });
        return prs;
    }


    
    /**
     * 获得指定PO类型下指定类型的关联属性
     * */
    @SuppressWarnings("unchecked")
	<E extends Entity,T extends Entity> List<PropertyRoute<E,T>> findProperties(Class<E> poType, Class<T> targetType) {
		List<PropertyRoute> list=getProperties(poType);
    	List<PropertyRoute<E,T>> prs=new ArrayList<>();
		if(list==null) return prs;
		list.forEach(p->{
            if( ReflectUtil.isSubType(p.getSourcePoType(), poType) && ReflectUtil.isSubType(targetType,p.getTargetPoType())){
                prs.add(p);
            }
        });
        return prs;
    }
    
	@SuppressWarnings("rawtypes")
	<E extends Entity> PropertyRoute findProperties(Class<E> poType, String prop) {
		List<PropertyRoute> list=getProperties(poType);
		if(list==null) return null;
    	for (PropertyRoute p : list) {
			if(prop.equals(p.getProperty())) {
				if( ReflectUtil.isSubType(p.getSourcePoType(), poType)){
	               return p;
	            }
			}
		}
		return null;
	}
		 
    
//    List<Join> findJoinPath(PropertyRoute prop, DBTable poTable, DBTable targetTable,DBField[] usingProps) {
//
//
//
////        for (int i = 0; i < routeTables; i++) {
////
////        }
//
//
////    	return (new JoinPathFinder(prop,joins,poTable, targetTable, usingProps, routeTables, routeFields)).find();
//    	return  prop.getJoins();
//    }



}
