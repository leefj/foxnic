package com.github.foxnic.api.bean;

import java.util.List;
import java.util.Map;

public class BeanProperty<E,T> {

    private Class<E> bean;
    private String name;
    private String label;
    private String desc;
    private Class type;
    private Class<T> componentType;
    private Class keyType;

    public BeanProperty(Class<E> bean,String name, Class type, String label, String desc, Class<T> componentType, Class keyType) {
        this.bean=bean;
        this.name=name;
        this.type=type;
        this.label=label;
        this.desc=desc;
        this.componentType=componentType;
        this.keyType=keyType;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getDesc() {
        return desc;
    }

    public Class getType() {
        return type;
    }

    public Class<T> getComponentType() {
        return componentType;
    }

    public Class getKeyType() {
        return keyType;
    }

    public boolean isList() {
        return List.class.equals(this.type);
    }

    public boolean isMap() {
        return Map.class.equals(this.type);
    }

    public boolean isSimple() {
        return  !isMap() && !isList();
    }

    public Class<E> getBean() {
        return bean;
    }

}
