package com.github.foxnic.generator.builder.view.field.option.list;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.generator.builder.view.config.FillByUnit;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;
import com.github.foxnic.generator.builder.view.option.ViewOptions;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.generator.util.ConfigCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldListOptions extends SubOptions {

    private ModuleContext context;

    public FieldListOptions(ModuleContext context,FieldInfo field, FieldOptions top) {
        super(field,top);
        this.context = context;
    }

    /**
     * 设置标签，默认从数据库注释获取
     * */
    public FieldListOptions label(String label) {
        this.field.setLabelInList(label);
        return this;
    }

    /**
     * 设置是否在列表中隐藏当前字段
     * */
    public FieldListOptions hidden(boolean hidden) {
        this.field.hideInList(hidden);
        return this;
    }

    /**
     * 设置在列表中隐藏当前字段
     * */
    public FieldListOptions hidden() {
        this.field.hideInList(true);
        return this;
    }


    /**
     * 使字段在列表中左对齐
     * */
    public  FieldListOptions alignLeft() {
         this.field.alignLeftInList();
        return this;
    }

    /**
     * 使字段在列表中右对齐
     * */
    public  FieldListOptions alignRight() {
        this.field.alignRightInList();
        return this;
    }

    /**
     * 使字段在列表中居中对齐
     * */
    public  FieldListOptions alignCenter() {
        this.field.alignCenterInList();
        return this;
    }

    /**
     * 设置是否可排序，默认可排序
     * */
    public FieldListOptions sort(boolean sort) {
        this.field.sortInList(sort);
        return this;
    }

    /**
     * 设置列锁定，默认不锁定
     * */
    public FieldListOptions fix(boolean fix) {
        this.field.fixInList(fix);
        return this;
    }

    /**
     * 指定列表单元格中的填充的数据<br/> 依次指定值所在的属性，形成路径，控制器代码不会生成 Join
     * */
    public FieldListOptions fillByProperty(String... propertyName) {
        this.field.setListFillByPropertyNames(propertyName);
        return this;
    }
    /**
     * 指定列表单元格中的填充的数据<br/> 依次指定值所在的属性，形成路径，控制器代码会生成Join
     * */
    public FieldListOptions fillBy(String... propertyName) {
        this.field.setListFillByPropertyNames(propertyName);
        //if(this.context.getFillByUnits()!=null) return this;
        Throwable th=new Throwable();
        StackTraceElement el=th.getStackTrace()[1];
        List<FillByUnit> fillByUnits= ConfigCollector.collectFills(el);
        this.context.setFillByUnits(fillByUnits);
        return this;
    }

    /**
     * 禁用列
     * */
    public FieldListOptions disable(boolean b){
        this.field.setDisableInList(b);
        return this;
    }

    /**
     * 禁用列
     * */
    public FieldListOptions disable(){
        this.field.setDisableInList(true);
        return this;
    }

    /**
     * 设置字段查询权限
     * */
    public FieldListOptions permission(String perm) {
        this.field.setListPermission(perm);
        return this;
    }

    /**
     * 强制显示默认情况下被 DBTreaty 规则排除的字段
     * */
    public FieldListOptions displayWhenDBTreaty(boolean b) {
        this.field.setDisplayWhenDBTreaty(b);
        return this;
    }

    /**
     * 指定样式Map
     * */
    public FieldListOptions useBadgeStyle(HashMap<String, String> badgeCssMap) {
        JSONObject json=new JSONObject();
        for (Map.Entry<String, String> entry : badgeCssMap.entrySet()) {
            json.put(entry.getKey(),entry.getValue());
        }
        this.field.setTableBadgeStyle(json);
        return this;
    }

    /**
     * 指定样式集合
     * */
    public FieldListOptions useBadgeStyle(String... badgeCssArray) {
        JSONArray jsonArray=new JSONArray();
        for (String s : badgeCssArray) {
            jsonArray.add(s);
        }
        this.field.setTableBadgeStyle(jsonArray);
        return this;
    }

    /**
     * 用默认主题指定样式
     * */
    public FieldListOptions useThemeBadgeStyle() {
        this.field.setTableBadgeStyle("#BY-THEME");
        return this;
    }
}
