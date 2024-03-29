package com.github.foxnic.springboot.spring;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.collection.TypedHashMap;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.Map.Entry;




public class SpringUtil {

	/**
	 * 获得本次启动节点的实例ID
	 * */
	public static String getNodeInstanceId() {
		return nodeInstanceId;
	}

	private  static String nodeInstanceId= IDGenerator.getSUID(true);

	private static Class startupClass = null;

	/**
	 * 获得启动类
	 * */
	public static Class getStartupClass() {
		return startupClass;
	}

	private static ApplicationContext context = null;
	private static Environment environment = null;
	private static Binder binder = null;
	private static HashMap<String,Object> configs=new HashMap<String,Object>();
	private static boolean isSpringReady=false;

	/**
	 * Spring环境，容器是否就绪
	 * */
	public static boolean isReady() {
		return isSpringReady;
	}

	public static void setContextInWebIf(ApplicationContext ctx)
	{
		AnnotationConfigServletWebServerApplicationContext xx;

		if (context != null) {
			return;
		}
		context = ctx;

		if (context != null) {
			isSpringReady=true;
		}

		initStartupClass();



	}

	private static void initStartupClass() {
		if(startupClass!=null) {
			return;
		}

		Throwable ta=new Throwable();
		StackTraceElement[] tas= ta.getStackTrace();
		String clsName=null;
//		try {

			for (int i = tas.length-1; i >= 0; i--) {
				clsName=tas[i].getClassName();
				System.out.println(i+".boot from\t--\t"+clsName);
				try {
					startupClass = Class.forName(clsName);
				} catch (Exception e) {
					try {
						startupClass = Class.forName(clsName,false, Thread.currentThread().getContextClassLoader());
					} catch (ClassNotFoundException e1) {
						System.err.println("clsName = "+clsName);
						e.printStackTrace();
					}
				}
				//Class.forName(clsName, isSpringReady, null);

				SpringBootApplication an=(SpringBootApplication)startupClass.getAnnotation(SpringBootApplication.class);
				if(an!=null) {
					break;
				}
			}
//		} catch (ClassNotFoundException e) {
//			System.err.println("clsName = "+clsName);
//			e.printStackTrace();
//		}
	}

	/**
	 * 获得ComponentScan注解的扫描范围
	 * */
	public static ArrayList<String> getComponentScanRange()
	{
		if(startupClass==null)
		{
			initStartupClass();
		}
		ArrayList<String> range=new ArrayList<String>();

		Annotation[]  anns=startupClass.getAnnotationsByType((ComponentScan.class));
		for (Annotation ann : anns) {
			ComponentScan cs=(ComponentScan)ann;
			for (Class cls : cs.basePackageClasses()) {
				if(range.contains(cls.getPackage().getName())) {
					continue;
				}
				range.add(cls.getPackage().getName());
			}
			for (String pkg : cs.basePackages()) {
				if(range.contains(pkg)) {
					continue;
				}
				range.add(pkg);
			}
		}
		return range;
	}

	private static ConfigurableListableBeanFactory beanFactory = null;

	public static void setBeanFactoryIf(ConfigurableListableBeanFactory fac)
	{
		if (beanFactory != null) {
			return;
		}
		beanFactory=fac;
	}

	public static void setEnvironmentIf(Environment env)
	{
		if (environment != null) {
			return;
		}
		environment = env;

		binder = Binder.get(environment);

		ConfigurableEnvironment ce=null;
		if(environment instanceof ConfigurableEnvironment)
		{
			ce=(ConfigurableEnvironment)environment;
		}
		if(ce==null) {
			return;
		}

		Iterator<PropertySource<?>> pps=ce.getPropertySources().iterator();
		PropertySource ps = null;
		while(pps.hasNext())
		{
			ps=pps.next();
			if(ps==null) {
				continue;
			}
			if (ps instanceof MapPropertySource) {
                configs.putAll(((MapPropertySource) ps).getSource());
            }
		}
	}

	/**
	 * 得到Spring上下文
	 * */
	public static ApplicationContext getSpringContext()
	{
		return context;
	}

	/**
	 *  通过name获取 Bean.
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return context.getBean(name);
	}

	/**
	 * 通过class获取Bean.
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		try {
			return context.getBean(clazz);
		} catch (BeansException e) {
			return null;
		}
	}

	/**
	 * 通过class获取Bean.
	 * @param superType
	 * @return
	 */
	public static <T> List<T> getBeans(Class<T> superType) {
		try {
			String[] names=context.getBeanNamesForType(superType);
			List<T> list=new ArrayList<>();
			for (String name : names) {
				T bean=(T)context.getBean(name);
				list.add(bean);
			}
			return list;
		} catch (BeansException e) {
			return null;
		}
	}

	/**
	 * 通过class获取Bean.
	 * @return
	 */
	public static String[] getBeanNames() {
		try {
			return context.getBeanDefinitionNames();
		} catch (BeansException e) {
			return null;
		}
	}

	/**
	 * 通过name,以及Clazz返回指定的Bean
	 * @param name
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> clazz) {
		return context.getBean(name, clazz);
	}




	 /**
     * 动态注册Bean，并返回Bean对象
     */
    public static <T> T registerBean(Class<T> type) {
        String name = type.getName();
        return registerBean(type, name, BeanScopes.SINGLETON, null);
    }

    /**
     * 动态注册Bean，并返回Bean对象
     */
    public static <T> T registerBean(Class<T> type, String name, BeanScopes scope, Map<String, String> injectionProperties) {
        T bean = null;
        try {
            bean = (T) getBean(name);
        } catch (Exception e1) {
        }
        if (scope == BeanScopes.SINGLETON && bean != null) return bean;
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(type);
        if (injectionProperties != null) {
            for (Entry<String, String> e : injectionProperties.entrySet()) {
                beanDefinitionBuilder.addPropertyReference(e.getKey(), e.getValue());
            }
        }
        if (scope != null) {
            beanDefinitionBuilder.setScope(scope.scope());
        }
        // 注册bean
        defaultListableBeanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());
        bean = (T) getBean(name);
        return bean;


    }

    /**
     * 移除已经注册的Bean
     */
    public static void unregisterBean(String name) {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        defaultListableBeanFactory.removeBeanDefinition(name);
    }


	/**
	 * 获得 以 prefix 开头的环境变量集合，并按 prefix 后的部分自然排序后的prop集合
	 * */
	public static TreeMap<String,Object> getEnvProperties(String prefix)
	{
		if(StringUtil.isBlank(prefix)) return null;
		prefix= StringUtil.removeLast(prefix,".");
		prefix+=".";
		TreeMap<String,Object> ps=new TreeMap<String,Object>();
		for (String key : configs.keySet()) {
			 if(key.startsWith(prefix))
			 {
				 ps.put(key, configs.get(key));
			 }
		}
		return ps;
	}

	/**
	 * 获得 以 prefix 开头的环境变量集合，并按 prefix 后的部分自然排序后的prop集合
	 * */
	public static Map<String,Object> getEnvProperties(String prefix,boolean simplify)
	{
		if(!simplify) {
			return getEnvProperties(prefix);
		}

		TreeMap<String,Object> ps=getEnvProperties(prefix);
		String key=null;
		TypedHashMap map=new TypedHashMap();
		for (Entry<String,Object> e : ps.entrySet()) {
			 key=e.getKey();
			 key=key.substring(prefix.length()+1);
			if(e.getValue() instanceof OriginTrackedValue) {
				 map.put(key,((OriginTrackedValue) e.getValue()).getValue());
			 } else {
				 map.put(key,e.getValue());
			 }
		}
		return map;
	}

	/**
	 * 获得环境变量(Boolean)
	 * */
	public static Boolean getBooleanEnvProperty(String name)
	{
		return DataParser.parseBoolean(getEnvProperty(name));
	}

	/**
	 * 获得环境变量(Boolean)
	 * */
	public static Integer getIntegerEnvProperty(String name)
	{
		return DataParser.parseInteger(getEnvProperty(name));
	}

	/**
	 * 获得环境变量(Date)
	 * */
	public static Date getDateEnvProperty(String name)
	{
		return DataParser.parseDate(getEnvProperty(name));
	}

	public static List<String> getStringListEnvProperty(String name)
	{
		return binder.bind(name, Bindable.listOf(String.class)).get();
	}

	/**
	 * 获得环境变量(字符串)
	 * */
	public static String getEnvProperty(String name)
	{
		String value=environment.getProperty(name);
//		if(value!=null)
//		{
//			value=StringUtil.remove(value, "\"");
//		}

		if(value==null) {
			return null;
		}
		return value;
	}


	public static String getBeanClassName(Object bean) {
		String clsName=bean.getClass().getName();
		int i=clsName.indexOf("$$");
		if(i>0) {
			clsName=clsName.substring(0,i);
		}
		return clsName;
	}


	public static String getActiveProfile() {
		String[] pfs=environment.getActiveProfiles();
		if(pfs==null || pfs.length==0) return "default";
		return pfs[0];
	}

	private static String processId=null;
	public static String getProcessId() {
		if(processId!=null) return processId;
		String name = ManagementFactory.getRuntimeMXBean().getName();
		processId = name.split("@")[0];
		return processId;
	}


    public static Object getApplicationName() {
		return getEnvProperty("spring.application.name");
	}
}
