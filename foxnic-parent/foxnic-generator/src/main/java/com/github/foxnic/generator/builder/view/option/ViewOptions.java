package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBField;

import java.lang.reflect.Method;

public class ViewOptions {

    private ModuleContext context;
    //


    public ViewOptions(ModuleContext moduleContext) {
        this.context=moduleContext;
    }

    /**
     * 指定字段，开始配置字段在界面上的呈现<br/>
     * 默认已经存在数据库中的所有列，如果不存在就增加一个新的
     * */
    public FieldOptions field(DBField field) {
        try {
            Method method=this.context.getClass().getDeclaredMethod("field",DBField.class);
            method.setAccessible(true);
            FieldOptions obj=(FieldOptions)method.invoke(this.context,field);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 指定字段，开始配置字段在界面上的呈现<br/>
     * 默认已经存在数据库中的所有列，如果不存在就增加一个新的
     * */
    public FieldOptions field(String field) {
        try {
            Method method=this.context.getClass().getDeclaredMethod("field",String.class);
            method.setAccessible(true);
            FieldOptions obj=(FieldOptions)method.invoke(this.context,field);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 指定字段，开始配置字段在界面上的呈现
     * */
    public FormWindowOptions formWindow() {
        return new FormWindowOptions(this.context.getFormWindowConfig());
    }

    /**
     * 搜索区域配置
     * */
    public SearchAreaOptions search() {
        return new SearchAreaOptions(this.context.getSearchAreaConfig());
    }

    /**
     * 搜索区域配置
     * */
    public FormOptions form() {
        return new FormOptions(this.context.getFormConfig());
    }

    /**
     * 列表区域配置
     * */
    public ListOptions list() {
        return new ListOptions(this.context.getListConfig());
    }





}
