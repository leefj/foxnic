package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.generator.builder.view.field.FieldInfo;

import java.util.*;

public class FormGroupConfig {

    public static class GroupLocation {

        /**
         * 栏次,从0还是编号
         * */
        private Integer columnIndex;
        /**
         * 栏次内的序号
         * */
        private Integer index;

        public GroupLocation(Integer columnIndex,Integer index) {
            this.columnIndex=columnIndex;
            this.index=index;
        }

        public Integer getColumnIndex() {
            return columnIndex;
        }

        public Integer getIndex() {
            return index;
        }
    }

    private String title=null;

    public String getType() {
        return type;
    }



    /**
     *  类型，normal,iframe,tab
     * */
    private String type="normal";

    /**
     * 列集合，列序号从0开始编号
     * */
    private Map<Integer,List<String>> columns;

    private String elId;

    public String getElId() {
        if(elId==null) {
            elId= "random-"+IDGenerator.getRandomDigits(4);
        }
        return elId;
    }

    public FormGroupConfig(List<FieldInfo> fields,String elId) {
        this.columns=new HashMap<Integer,List<String>>();
        List<String> list=new ArrayList<>();
        for (FieldInfo field : fields) {
            list.add(field.getColumn());
        }
        this.columns.put(0,list);
        this.elId=elId;
    }

    public FormGroupConfig(String title,Map<Integer,List<String>> columns,String elId) {
        this.title=title;
        this.columns=columns;
        this.type="normal";
        this.elId=elId;
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    private  List<Tab> tabs=null;

    public FormGroupConfig(Tab[] tab,String elId) {
        tabs= Arrays.asList(tab);
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setIndex(i);
        }
        this.type="tab";
        this.elId=elId;
    }

    private String iframeLoadJsFunctionName;

    public String getIframeLoadJsFunctionName() {
        return iframeLoadJsFunctionName;
    }

    public FormGroupConfig(String title,String jsFuncName,String elId) {
        this.title=title;
        this.iframeLoadJsFunctionName =jsFuncName;
        this.type="iframe";
        this.elId=elId;
    }


    public String getTitle() {
        return title;
    }

    public Map<Integer, List<String>> getColumns() {
        return columns;
    }

    /**
     * 按字段名获得栏次的编号
     * @return Point  x为栏次，y
     * */
    public GroupLocation getLocation(FieldInfo field) {
        if(columns==null) return null;
        for (Integer columnIndex : columns.keySet()) {
            List<String> column=columns.get(columnIndex);
            int index=column.indexOf(field.getColumn());
            if(index==-1) {
                index=column.indexOf(field.getVarName());
            }
            if(index!=-1) {
                return new GroupLocation(columnIndex,index);
            }
        }
        return null;
    }

}
