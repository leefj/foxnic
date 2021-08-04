package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.sql.meta.DBField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchAreaConfig {

    private List<String[]> inputLayout=null;

    public void setInputLayout(Object[]... inputRows) {
        inputLayout=new ArrayList<>();
        Set<String> all=new HashSet<>();
        for (Object[] inputRow : inputRows) {
            String[] row=new String[inputRow.length];
            int i=0;
            int valid=0;
            for (Object o : inputRow) {
                if(o instanceof String) {
                    row[i]=(String)o;
                    valid++;
                } else if(o instanceof DBField) {
                    row[i]=((DBField)o).name();
                    valid++;
                }  else {
                   throw new RuntimeException("仅支持 DBField 与 String 类型");
                }
                if(all.contains(row[i])) {
                    throw new RuntimeException(row[i]+" 字段重复");
                }
                all.add(row[i]);
                i++;
            }
            if(valid>0) {
                inputLayout.add(row);
            }
        }

    }

    public List<String[]> getInputLayout() {
        return inputLayout;
    }


    private int inputWidth=140;


    public int getInputWidth() {
        return inputWidth;
    }

    public void setInputWidth(int w) {
        this.inputWidth=w;
    }

}
