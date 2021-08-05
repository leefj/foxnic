package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.sql.meta.DBField;

import java.util.*;

public class FormConfig {

    private List<FormGroupConfig> groups;
    private FormGroupConfig columnOnlyGroup=null;

    public void setInputColumnLayout(int column,Object[] inputs) {
        Map<Integer,List<String>> columns=new HashMap<>();
        Set<String> all=new HashSet<>();
        int i=-1;
        int columnIndex=0;
        String columnName=null;
        for (Object input : inputs) {
            i++;
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
            columnIndex=i%column;
            List<String> list=columns.get(columnIndex);
            if(list==null) {
                list=new ArrayList<>();
                columns.put(columnIndex,list);
            }
            list.add(columnName);
        }
        columnOnlyGroup=new FormGroupConfig(null,columns);
    }

    /**
     * 返回布局模式
     * */
    public String getLayoutMode() {
        if(groups==null && columnOnlyGroup==null) {
            return "default";
        }
        else if(groups==null && columnOnlyGroup!=null) {
            if(columnOnlyGroup.getColumns().size()==1) {
                return "default";
            } else {
                return "column";
            }
        }
        else if(groups!=null && columnOnlyGroup==null) {
            return "group";
        } else {
            return "default";
        }
    }

    public List<FormGroupConfig> getGroups() {
        return groups;
    }

    public FormGroupConfig getColumnOnlyGroup() {
        return columnOnlyGroup;
    }

}
