package com.github.foxnic.dao.relation;

import com.github.foxnic.api.bean.BeanProperty;
import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.Entity;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.meta.DBTable;

import java.util.*;

public abstract class RelationManager {

    private List<Join> joins =new ArrayList<>();

    @SuppressWarnings("rawtypes")
    private List<PropertyRoute> properties =new ArrayList<>();

	public List<PropertyRoute> getProperties() {
		return properties;
	}

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

	public List<Class<? extends Entity>> getPoTypes() {
		List<Class<? extends Entity>> list=new ArrayList<>();
		for (Class poType : map.keySet()) {
			list.add(poType);
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
    public void merge(RelationManager topManager) {

		List<RelationManager> descendants=topManager.getDescendants();
		descendants.add(topManager);

		for (RelationManager relationManager : descendants) {

			if(relationManager.joins.isEmpty() || relationManager.properties.isEmpty()) {
				relationManager.clear();
				relationManager.config();
			}

			this.joins.addAll(relationManager.joins);

			for (PropertyRoute p : relationManager.properties) {
				 if(getProperty(p.getMasterPoType(), p.getProperty())!=null) {
					 throw new IllegalArgumentException(p.getMasterPoType().getName()+"属性["+p.getProperty()+"]重复添加");
				 }
				 this.properties.add(p);
				 this.putToMap(p);
			}
		}
	}

	private List<RelationManager> getDescendants() {
		List<RelationManager> descendants = new ArrayList<>();
		for (RelationManager relationManager : relationManagers) {
			descendants.add(relationManager);
			descendants.addAll(relationManager.getDescendants());
		}
		return descendants;
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
	 * 配置一个关联
	 * @param  poType  实体类型
	 * @param  property  实体属性
	 * @return   PropertyRoute
	 * */
	public <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(Class<S> poType, String property) {
		return  property(poType,property,null);
	}

	/**
	 * 配置一个关联
	 * @param  property  实体属性对象
	 * @return   PropertyRoute
	 * */
	public <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(BeanProperty<S,T> property) {
		return  property(property.getBean(),property.getName(),property.getComponentType());
	}

	/**
	 * 配置一个关联
	 * @param  poType  实体类型
	 * @param  property  实体属性
	 * @param  targetPoType 目标实体类型，如果不使用 after 该参数可以省略
	 * @return   PropertyRoute
	 * */
	public <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(Class<S> poType, String property,Class<T> targetPoType) {

		return property(poType,property,targetPoType,"","");
	}

    /**
     * 配置一个关联属性
     * */
	private <S extends Entity,T extends Entity>  PropertyRoute<S,T> property(Class<S> poType, String property,Class<T> targetPoType,String label,String detail) {
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
		List<PropertyRoute> list=map.get(prop.getMasterPoType());
		if(list==null) {
			list=new ArrayList<>();
			map.put(prop.getMasterPoType(),list);
		}
		if(!list.contains(prop)) {
			list.add(prop);
		}
	}


	private <S extends Entity,T extends Entity> PropertyRoute<S,T> getProperty(Class poType,String prop) {
		List<PropertyRoute> list=getProperties(poType);
		if(list==null) return null;
    	for (PropertyRoute p : list) {
			if(prop.equals(p.getProperty()) && poType.equals(p.getMasterPoType()))
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
            if( ReflectUtil.isSubType(p.getMasterPoType(), poType) && ReflectUtil.isSubType(targetType,p.getSlavePoType())){
                prs.add(p);
            }
        });
        return prs;
    }

	@SuppressWarnings("rawtypes")
	public <E extends Entity> PropertyRoute findProperties(Class<E> poType, String prop) {
		List<PropertyRoute> list=getProperties(poType);
		if(list==null) return null;
    	for (PropertyRoute p : list) {
			if(prop.equals(p.getProperty())) {
				if( ReflectUtil.isSubType(p.getMasterPoType(), poType)){
	               return p;
	            }
			}
		}
		return null;
	}

	private Map<Class,List<PropertyRoute>> passBys=new HashMap<>();
	/**
	 * 查找途径的 PropertyRoute
	 * */
    public List<PropertyRoute> findPropertyRoutes(Class poType){
		List<PropertyRoute> routes=passBys.get(poType);
//		routes=null;
		if(routes!=null) return routes;
		DBTable table= EntityUtil.getDBTable(poType);
		if(table==null) return new ArrayList<>();
		routes=new ArrayList<>();
    	for (PropertyRoute property : properties) {
			List<Join> joins=property.getJoins();
			for (Join join : joins) {
				if(join.getSlaveTable().equalsIgnoreCase(table.name())) {
					routes.add(property);
				}
			}
		}
		passBys.put(poType,routes);
    	return routes;
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
