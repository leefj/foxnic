package com.github.foxnic.dao.entity;

import com.github.foxnic.api.bean.BeanProperty;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.lang.ObjectUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.sql.entity.EntityUtil;
import com.github.foxnic.sql.meta.DBTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityContext {

	static final String PROXY_CLASS_NAME="$$proxy$$";


	/**
	 * 创建一个空对象
	 * */
	public static <T extends Entity> T create(Class<T> type) {
		return EntitySourceBuilder.create(type);
	}

	/**
	 * 创建一个对象，并填充数据
	 * */
	public static <T extends Entity> T create(Class<T> type,Map<String,Object> data) {
		T t=create(type);
		copyProperties(t, data);
		t.clearModifies();
		return t;
	}

	/**
	 * 创建一个对象，并填充数据
	 * */
	public static <T extends Entity> T create(Class<T> type,Object pojo) {
		T t=create(type);
		copyProperties(t, pojo);
		t.clearModifies();
		return t;
	}

	/**
	 * 克隆对象
	 * */
	public static <T extends Entity> T  clone(Class<T> type,Entity entity,boolean deep) {
		entity = clone(type,entity);
		try {
			return (T)ObjectUtil.copy(entity);
		} catch (Exception e) {
			Logger.exception("clone error",e);
			return null;
		}
	}

	/**
	 * 克隆对象
	 * */
	public static <T extends Entity> T  clone(Class<T> type,Entity entity) {
		return create(type,entity);
	}

	public static void  cloneProperty(Entity owner, BeanProperty property) {
		cloneProperty(owner,property,false);
	}

	public static void  clonePropertyChain(Entity root, boolean deep,BeanProperty... property) {
		Entity owner=root;
		Object ownerValue=null;
		for (BeanProperty prop : property) {
			cloneProperty(owner,prop,deep);
			if(prop.isSimple()) {
				ownerValue=BeanUtil.getFieldValue(owner, prop.getName());
				if(ownerValue==null){
					continue;
				}
				if(ownerValue instanceof Entity) {
					owner = (Entity)ownerValue;
				} else {
					throw new IllegalArgumentException("not support yet");
				}
			} else {
				throw new IllegalArgumentException("not support yet");
			}
		}

	}

	/**
	 * 克隆对象
	 * */
	public static void  cloneProperty(Entity owner, BeanProperty property,boolean deep) {
		if(owner==null) return;
		Object propValue=BeanUtil.getFieldValue(owner,property.getName());
		if(propValue==null) return;
		if(property.isSimple()) {
			if(propValue instanceof Entity) {
				Entity newValue=clone((Class<Entity>)propValue.getClass(),(Entity)propValue,deep);
				BeanUtil.setFieldValue(owner,property.getName(),newValue);
			} else {
				Object newValue=BeanUtil.clone(propValue,deep);
				BeanUtil.setFieldValue(owner,property.getName(),newValue);
			}
		} else if(property.isList()) {
			List list=(List) propValue;
			if(list.isEmpty()) {
				list=new ArrayList();
				BeanUtil.setFieldValue(owner,property.getName(),list);
				return;
			}
			Object firstValue=null;
			for (int i = 0; i < list.size(); i++) {
				firstValue=list.get(i);
				if(firstValue!=null) break;
			}
			Class type=Object.class;
			if(firstValue!=null) {
				type=firstValue.getClass();
			}
			List newList=new ArrayList();
			Object value=null;
			boolean isEntity=ReflectUtil.isSubType(Entity.class,type);
			for (int i = 0; i < list.size(); i++) {
				 value=list.get(i);
				 if(value!=null) {
					 if (isEntity) {
						 newList.add(clone(type, (Entity) value, deep));
					 } else {
						 newList.add(BeanUtil.clone(value, deep));
					 }
				 } else {
					 newList.add(value);
				 }
			}
			BeanUtil.setFieldValue(owner,property.getName(),newList);

		} else  {
			throw new IllegalArgumentException("not support type "+property.getBean().getSimpleName()+"."+property.getName());
		}
	}



	public static <T> T copyProperties(T target,Object source) {
		if(source==null) {
			new IllegalArgumentException("pojo is require");
		}
		//针对复杂对象，后续进一步扩展
		BeanUtil.copy(source, target, false);
		return target;
	}


	public static <T> T copyProperties(T entity,Map<String,Object> data) {
		if(data==null) {
			new IllegalArgumentException("data is require");
		}
		//针对复杂对象，后续进一步扩展
		BeanUtil.copy(data, entity);
		return entity;
	}

	public static <T extends Entity>  Class<T> getProxyType(Class<T> type) {
		return EntitySourceBuilder.getProxyType(type);
	}

	public static void clearModifies(Object entity) {
		if(entity instanceof Entity) {
			((Entity)entity).clearModifies();
		}
	}

	/**
	 * 是否是一个代理实体，等价于 isProxyType
	 * */
	public static boolean isManaged(Object pojo) {
		if(!(pojo instanceof Entity)) return false;
		return isProxyType(pojo.getClass());
	}

	/**
	 * 是否实体类型
	 * */
	public static boolean isEntityType(Class type) {
		return ReflectUtil.isSubType(Entity.class, type);
	}

	/**
	 * 判断是是代理类型 ( Meta.$$proxy$$ 类型 )
	 * */
	public static boolean isProxyType(Class type) {
		if(type==null) return false;
		if(PROXY_CLASS_NAME.equals(type.getSimpleName()) && Entity.class.isAssignableFrom(type) ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 将包装类转为原始类
	 */
	public static Class getPoType(Class<? extends Entity> type) {
		DBTable table = null;
		while (true) {
			table = EntityUtil.getDBTable(type);
			if (table != null) {
				break;
			}
			if (Entity.class.isAssignableFrom(type.getSuperclass())) {
				type = (Class<? extends Entity>) type.getSuperclass();
			} else {
				break;
			}
		}
		return type;
	}


	public static String getPoClassName(String dataType) {
		if(dataType==null) return null;
		Class type=ReflectUtil.forName(dataType, true);
		if(!ReflectUtil.isSubType(Entity.class,type)) return type.getName();
		type=getPoType(type);
		if(type==null) return dataType;
		return type.getName();
	}


}
