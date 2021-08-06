package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.generator.builder.view.field.FieldInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 列集合，列序号从0开始编号
     * */
    private Map<Integer,List<String>> columns;

    public FormGroupConfig(List<FieldInfo> fields) {
        this.columns=new HashMap<Integer,List<String>>();
        List<String> list=new ArrayList<>();
        for (FieldInfo field : fields) {
            list.add(field.getColumn());
        }
        this.columns.put(0,list);
    }
    public FormGroupConfig(String title,Map<Integer,List<String>> columns) {
        this.title=title;
        this.columns=columns;
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
