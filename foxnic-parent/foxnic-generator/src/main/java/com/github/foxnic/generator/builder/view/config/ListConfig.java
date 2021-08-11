package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.List;

public class ListConfig {

    private List<String> defaultColumns=new ArrayList<>();


    public void setInputColumnLayout(Object[] inputs) {
        defaultColumns.clear();
        for (Object input : inputs) {
            if(input==null) continue;
            if(input instanceof  String) {
                if(StringUtil.isBlank(input)) continue;
                defaultColumns.add(input.toString());
            } else if(input instanceof DBField) {
                if(StringUtil.isBlank(input)) continue;
                defaultColumns.add(((DBField)input).name());
            } else {
                throw new RuntimeException("仅支持 DBField 与 String 类型");
            }
        }
    }

    public List<String> getDefaultColumns() {
        return defaultColumns;
    }



    private int operateColumnWidth=125;

    public void setOperateColumnWidth(int width) {
        this.operateColumnWidth=width;
    }

    public int getOperateColumnWidth() {
        return operateColumnWidth;
    }

}
