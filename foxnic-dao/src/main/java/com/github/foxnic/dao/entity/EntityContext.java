package com.github.foxnic.dao.entity;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.github.foxnic.commons.bean.BeanNameUtil;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.cache.LocalCache;

public class EntityContext {

	private static final String CLEAR_MODIFIES = "clearModifies";
	private static final String FINALIZE = "finalize";
	private static final String IS = "is";
	private static final String GET = "get";
	private static final String SET = "set";
	
	/**
	 * 修改过的属性的 get 方法名称
	 * */
	private static LocalCache<Integer,Set<String>> DIRTY_GETTER_CACHE=new LocalCache<>();
	
	/**
	 * 被调用过 set 方法的属性的 get 方法名称
	 * */
	private static LocalCache<Integer,Set<String>> BESET_GETTER_CACHE=new LocalCache<>();
	/**
	 * CGLib 的 Enhancer 缓存
	 * */
	private static LocalCache<Class,Enhancer> ENHANCER_CACHE=new LocalCache<>();
	
	/**
	 * 代理类型缓存
	 * */
	private static LocalCache<Class,Class> PROXY_TYPE_CACHE=new LocalCache<>();
	
	
	private static LocalCache<String,String> PROP_CACHE=new LocalCache<>();
	
 
	
	public static <T extends Entity> T create(Class<T> type) {
 
		EntitySourceBuilder<T> esb=new EntitySourceBuilder<T>(type);
		return esb.create();
//		Enhancer enhancer = ENHANCER_CACHE.get(type);
//		if(enhancer==null) {
//			enhancer=createEnhancer(type);
//		}
//        return (T)enhancer.create();
	}
	
 
	public static Class<? extends Entity> getProxyType(Class<? extends Entity> type) {
		Class cls=PROXY_TYPE_CACHE.get(type);
		if(cls!=null) return cls;
		Enhancer enhancer = ENHANCER_CACHE.get(type);
		if(enhancer==null) {
			enhancer=createEnhancer(type);
		}
		cls=enhancer.create().getClass();
		PROXY_TYPE_CACHE.put(type,cls);
        return cls;
	}
	
	private static BeanNameUtil beanNameUtil=new BeanNameUtil();
 
	private static Enhancer createEnhancer(Class<? extends Entity> type) {
		Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//                String setter=method.getName();
//                
//                if(!setter.startsWith(SET)) {
//	                if(CLEAR_MODIFIES.equals(setter)) {
//	                	proxy.invokeSuper(obj, args);
//	                	clearModifies(obj.hashCode());
//	                	return null;
//	                } else if("hasDirtyProperties".equals(setter)) {
//	                	return hasDirtyProperties(obj.hashCode());
//	                } else if("wrap".equals(setter)) {
//	                	return create((Class<? extends Entity>)obj.getClass());
//	                } else if("hasBeSetProperties".equals(setter)) {
//	                	return hasBeSetProperties(obj.hashCode());
//	                } else if("getBeSetProperties".equals(setter)) {
//	                	return getBeSetProperties(obj.hashCode());
//	                } else if("getDirtyProperties".equals(setter)) {
//	                	return getDirtyProperties(obj.hashCode());
//	                } else  if(FINALIZE.equals(setter)) {  //调用析构函数
//	                	int hashCode=obj.hashCode();
//	                	proxy.invokeSuper(obj, args);
//	                	//解除注册
//	                	unregistModify(hashCode);
//	                	return null;
//	                } else {
//	                	return proxy.invokeSuper(obj, args);
//	                }
//                }
// 
//                //参数不符合set方法的调价的，不处理
//                if(args.length!=1)   {
//                	return proxy.invokeSuper(obj, args);
//                }
//                Class paramType=method.getParameterTypes()[0];
//                String getter=PROP_CACHE.get(setter);
//                if(getter==null) {
//	                getter=setter.substring(3);
//	                getter=beanNameUtil.depart(getter);
//	                getter=beanNameUtil.getPropertyName(getter);
//	                PROP_CACHE.put(setter, getter);
//                }
//
//                Object oldValue= BeanUtil.getFieldValue(obj, getter);
//                Object result = proxy.invokeSuper(obj, args);
//                registModify(obj.hashCode(),getter,oldValue,args[0]);
//                return result;
//            	return null;
            	return proxy.invokeSuper(obj, args);
            }
        });
        ENHANCER_CACHE.put(type, enhancer);
		return enhancer;
	}

	protected static Object hasBeSetProperties(int hashCode) {
		Set<String> cache=BESET_GETTER_CACHE.get(hashCode);
		return cache!=null && !cache.isEmpty();
	}


	protected static boolean hasDirtyProperties(int hashCode) {
		Set<String> cache=DIRTY_GETTER_CACHE.get(hashCode);
		return cache!=null && !cache.isEmpty();
		
	}


	private static void unregistModify(Integer hashCode) {
		DIRTY_GETTER_CACHE.remove(hashCode);
		BESET_GETTER_CACHE.remove(hashCode);
	}

	private static void registModify(Integer hashCode,String getter,Object oldValue,Object newValue) {
		
		Set<String> mcache=DIRTY_GETTER_CACHE.get(hashCode);
		if(mcache==null) {
			mcache=new HashSet<String>();
			DIRTY_GETTER_CACHE.put(hashCode, mcache);
		}
		
		Set<String> bcache=BESET_GETTER_CACHE.get(hashCode);
		if(bcache==null) {
			bcache=new HashSet<String>();
			BESET_GETTER_CACHE.put(hashCode, bcache);
		}
		
		boolean isModified=false;
		if(oldValue==null && newValue==null) {
			isModified=false;
		} else if(oldValue==null && newValue!=null) {
			isModified=true;
		} else if(oldValue!=null && newValue==null) {
			isModified=true;
		} else {
			isModified=!oldValue.equals(newValue);
		}
		
		//设置是否被修改
		if(isModified) {
			mcache.add(getter);
		}
		//是否被设置过
		bcache.add(getter);
	}
	
	public static boolean isModified(Object po,String getter) {
		if(po==null) return false;
		Set<String> cache=DIRTY_GETTER_CACHE.get(po.hashCode());
		return cache.contains(getter);
	}
 
	private static String[] getBeSetProperties(Object po) {
		if(po==null) return new String[0];
		Set<String> cache=DIRTY_GETTER_CACHE.get(po.hashCode());
		if(cache==null) return new String[0];
		return cache.toArray(new String[0]);
	}
	
	private static String[] getDirtyProperties(Object po) {
		if(po==null) return new String[0];
		Set<String> cache=DIRTY_GETTER_CACHE.get(po.hashCode());
		if(cache==null) return new String[0];
		return cache.toArray(new String[0]);
	}
	
	


	/**
	 * 清除日志登记
	 * */
	public static void clearModifies(Object entity) {
		unregistModify(entity.hashCode());
	}
	
	
	
	
	
	
}
