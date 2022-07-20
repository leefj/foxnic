package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.*;

public class FormConfig {

    private List<FormGroupConfig> groups=new ArrayList<>();

    private Set<String> all=new HashSet<>();

    private String mode=null;


    public void setInputColumnLayout(Object[]... cols) {
        setInputColumnLayout(null,cols);
    }
    /**
     * 设置简单的分栏布局
     * */
    public void setInputColumnLayout(String elId, Object[]... cols) {


        if("group".equals(mode)) {
           throw new RuntimeException("不允许使用同时使用多组布局");
        }

        FormGroupConfig group=new FormGroupConfig(null,buildColumns(cols),elId);

        if(groups.size()==0) {
            groups.add(group);
        } else {
            groups.set(0,group);
        }
        mode="simple";
    }

    public void addGroup(String title,Object[]... cols) {
        addGroup(null,title,cols);
    }

    /**
     * 使用分组布局
     * */
    public void addGroup(String elId,String title,Object[]... cols) {
        if("simple".equals(mode)) {
            throw new RuntimeException("不允许使用同时使用多组布局");
        }
        if(StringUtil.isBlank(title)) title=null;
        FormGroupConfig group=new FormGroupConfig(title,buildColumns(cols),elId);
        groups.add(group);
        mode="group";
    }

    public void addPage(String title, String jsFunctionName) {
        addPage(null,title,jsFunctionName);
    }

    public void addPage(String elId,String title, String jsFunctionName) {
        FormGroupConfig group=new FormGroupConfig(title,jsFunctionName,elId);
        groups.add(group);
        mode="group";
    }

    public void addTab(Tab[] tab) {
        this.addTab(null,tab);
    }
    public void addTab(String elId,Tab[] tab) {
        FormGroupConfig group=new FormGroupConfig(tab,elId);
        groups.add(group);
        mode="group";
    }


    private Map<Integer,List<String>> buildColumns(Object[]... cols) {
        Map<Integer,List<String>> columns=new HashMap<>();
        int i=-1;
        String columnName=null;
        for (Object[] inputs : cols) {
            i++;
            List<String> list=new ArrayList<>();
            for (Object input : inputs) {
                if(input instanceof String) {
                    columnName=(String)input;
                } else if(input instanceof DBField) {
                    columnName=((DBField)input).name();
                }  else {
                    throw new RuntimeException("仅支持 DBField 与 String 类型");
                }
                if(all.contains(columnName)) {
                    throw new RuntimeException(columnName+" 字段重复");
                }
                all.add(columnName);
                list.add(columnName);
            }
            columns.put(i,list);
        }
        return columns;
    }



    public List<FormGroupConfig> getGroups() {
        return groups;
    }


    private Integer labelWidth=null;

    public Integer getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(Integer labelWidth) {
        this.labelWidth = labelWidth;
    }

    public List<JsVariable> getJsVariables() {
        return jsVariables;
    }

    private  List<JsVariable> jsVariables=new ArrayList<>();



    public void addJsVariable(String name, String value, String note) {
        jsVariables.add(new JsVariable(name,value,note));
    }

    private  boolean footerDisabled=false;

    public boolean getMarginDisabled() {
        return marginDisabled;
    }

    public void setMarginDisabled(boolean marginDisabled) {
        this.marginDisabled = marginDisabled;
    }

    private  boolean marginDisabled=false;

    public boolean getFooterDisabled() {
        return footerDisabled;
    }

    public void setFooterDisabled(boolean b) {
        this.footerDisabled=b;
    }

    private  String savingURL = null;

    public void setSavingURL(String url) {
        this.savingURL=url;
    }

    public String getSavingURL() {
        return savingURL;
    }

    private  Set jsList =new HashSet<>();

    /**
     * 添加 JS 模块
     * */
    public void addJs(List<String> js) {
        jsList.addAll(js);
    }

    public Set getJsList() {
        return jsList;
    }

    private  Set cssList =new HashSet<>();

    /**
     * 添加 JS 模块
     * */
    public void addCss(List<String> css) {
        cssList.addAll(css);
    }

    public Set getCssList() {
        return cssList;
    }

    private Boolean enableContextMenu=false;
    public void setEnableContextMenu(Boolean b) {
        this.enableContextMenu=b;
    }

    public Boolean getEnableContextMenu() {
        return this.enableContextMenu;
    }

}
