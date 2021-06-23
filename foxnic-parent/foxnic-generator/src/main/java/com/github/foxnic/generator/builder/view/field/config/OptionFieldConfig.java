package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.api.constant.CodeTextEnum;
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
     * 设置字典代码
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
     * 设置 CodeTextEnum 类型的枚举
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
}
