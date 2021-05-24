package com.github.foxnic.springboot.api.proxy;

import com.github.foxnic.commons.cache.LocalCache;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.springboot.spring.SpringUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 用于无差别在单体应用和微服务应用间的调用
 * */
public class APIProxy {

	 @SuppressWarnings("unchecked")
    private static LocalCache<Class,Object> PROXY_CACHE=new LocalCache<>();
 
	public static <T> T get(Class<T> intfType,String controllerName){

        //缓存获取
        Object inst=PROXY_CACHE.get(intfType);
        if(inst!=null) return (T)inst;

        //首先尝试微服务模式下的Feign实现调用
        inst=SpringUtil.getBean(intfType);
        if(inst!=null) {
            PROXY_CACHE.put(intfType,inst);
            return (T)inst;
        }

        //使用单体模式下的 JDK 动态代理实现调用
        inst=getInstance(intfType,controllerName);
        if(inst!=null) {
            PROXY_CACHE.put(intfType,inst);
            return (T)inst;
        }
        throw new RuntimeException("调用错误，"+intfType.getName()+" 实现异常");
    }


    public static <T> T getInstance(Class<T> intfType,String controllerName){
        Class ctrlClass = ReflectUtil.forName(controllerName);
        if(ctrlClass==null) {
            throw new IllegalArgumentException("控制器 "+controllerName+" 不存在");
        }
        MethodProxy invocationHandler = new MethodProxy(ctrlClass);
        Object newProxyInstance = Proxy.newProxyInstance(
                intfType.getClassLoader(),
                new Class[] { intfType },
                invocationHandler);
        return (T)newProxyInstance;
    }

}

class MethodProxy implements InvocationHandler {

    private Object controller;

    public MethodProxy(Class ctrlType){
        this.controller=(Object)SpringUtil.getBean(ctrlType);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)  throws Throwable {
        Method m=controller.getClass().getDeclaredMethod(method.getName(),method.getParameterTypes());
        Object r=m.invoke(controller,args);
        return r;
    }
}
