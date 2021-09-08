package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.api.constant.CodeTextEnum;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

public class OptionFieldConfig<T extends  OptionFieldConfig> extends FieldConfig {
    private CodeTextEnum dict;
    private Class<? extends CodeTextEnum> enumType;

    public OptionFieldConfig(DBField field) {
        super(field);
    }

    public String getDictCode() {
        if(dict==null) return null;
        return dict.code();
    }

    /**
     * 配置成字典模式，并设置字典代码
     * */
    public T dict(CodeTextEnum dict) {
        clear();
        this.dict = dict;
        return (T)this;
    }

    public String getEnumTypeName() {
        if(enumType==null) return null;
        return enumType.getName();
    }

    /**
     * 配置成枚举模式，并设置 CodeTextEnum 类型的枚举
     * */
    public T enumType(Class<? extends CodeTextEnum> enumType) {
        clear();
        this.enumType = enumType;
        return (T)this;
    }

    protected  void clear() {
        this.enumType = null;
        this.dict = null;
    }

    private String[] defaultValues=null;
    private Integer[] defaultIndexs=null;

    public String getDefaultIndexs() {
        if(defaultIndexs==null) return "";
        return  StringUtil.join(defaultIndexs,",");
    }

    public String getDefaultValues() {
        if(defaultValues==null) return "";
        return StringUtil.join(defaultValues,",");
    }

    public String getDefaultValue() {
        if(defaultValues==null) return null;
        Object defaultValue=defaultValues[0];
        if(defaultValue==null) return null;

        return defaultValue.toString();
    }

    public void setDefaultValue(Object... defaultValue) {
        String[] arr=new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++) {
            Object val=defaultValue[i];
            if(val instanceof String) {
                arr[i] =  (String) val;
            } else if(val instanceof CodeTextEnum) {
                arr[i] =  ((CodeTextEnum) val).code();
            } else {
                arr[i]=val.toString();
            }
        }
        this.defaultValues=arr;
        this.defaultIndexs=null;
    }

    public Integer getDefaultIndex() {
        if(defaultIndexs==null) return -1;
        return defaultIndexs[0];
    }

    public void setDefaultIndex(Integer... defaultIndex) {
        this.defaultIndexs = defaultIndex;
        this.defaultValues = null;
    }



}
