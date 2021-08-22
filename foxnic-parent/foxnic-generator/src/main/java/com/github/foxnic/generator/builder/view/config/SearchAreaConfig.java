package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.sql.meta.DBField;

import java.util.*;

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


    public int getInputWidth(int columnIndex) {
        Integer w=inputWidthMap.get(columnIndex);
        if(w!=null) return w;
        return inputWidth;
    }

    public void setInputWidth(int w) {
        this.inputWidth=w;
    }

    private Map<Integer,Integer> labelWidthMap=new HashMap<>();
    private Map<Integer,Integer> inputWidthMap=new HashMap<>();

    public Integer getLabelWidth(Integer columnIndex) {
        return labelWidthMap.get(columnIndex);
    }

    public void setLabelWidth(int columnIndex,int width) {
        labelWidthMap.put(columnIndex,width);
    }

    public void setInputWidth(int columnIndex,int width) {
        inputWidthMap.put(columnIndex,width);
    }

    private int rowsDisplay=2;

    public int getRowsDisplay() {
        int size=1;
        if(inputLayout!=null) {
            size=inputLayout.size();
        }
        return rowsDisplay>size?size:rowsDisplay;
    }

    public void setRowsDisplay(int rowsDisplay) {
        if(rowsDisplay<1) rowsDisplay=1;
        this.rowsDisplay = rowsDisplay;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    private boolean disable=false;




}
