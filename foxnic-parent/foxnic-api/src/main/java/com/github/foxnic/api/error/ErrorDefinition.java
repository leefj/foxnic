package com.github.foxnic.api.error;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class ErrorDefinition {

    private static Set<String> definitions=new HashSet<>();

    private static Set<ErrorDefinition> definitionBeans=new HashSet<>();

    public static Set<ErrorDefinition> getDefinitionBeans() {
        return definitionBeans;
    }

    private String prefix;
    private String title;


    public ErrorDefinition() {
        definitionBeans.add(this);
    }

    /**
     * 注册一个错误定义分类
     * */
    public static void register(ErrorDefinition definition) {
        if(definitions.contains(definition.getClass().getName())) return;
        definitions.add(definition.getClass().getName());
        definition.init();

    }

    @PostConstruct
    public abstract void  init();

    public String getPrefix() {
        return prefix;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private Map<String,String> consts=new HashMap<>();

    public String getConstsName(String code) {
        String name=consts.get(code);
        if(name!=null) return name;
        for (Field field : this.getClass().getDeclaredFields()) {
            if(!Modifier.isStatic(field.getModifiers()) ) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(null);
                consts.put(value.toString(), this.getClass().getSimpleName() + "." + field.getName());
            } catch (Exception e) {}
        }
        return consts.get(code);
    }
}
