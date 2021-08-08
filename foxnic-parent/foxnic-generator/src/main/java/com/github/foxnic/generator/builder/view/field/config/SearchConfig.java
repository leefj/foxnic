package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.generator.builder.view.config.SearchAreaConfig;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.InputType;

public class SearchConfig {

    private boolean displayAlone=false;

    public boolean getFuzzySearch() {
        return fuzzySearch;
    }

    private SearchAreaConfig searchAreaConfig;
    private FieldInfo field;

    public  SearchConfig(FieldInfo field, SearchAreaConfig searchAreaConfig) {
        this.searchAreaConfig=searchAreaConfig;
        this.field=field;
    }

    private String valuePrefix;
    private String valueSuffix;

    public String getValuePrefix() {
        return valuePrefix;
    }

    public String getValueSuffix() {
        return valueSuffix;
    }


    /**
     * 设置是否为模糊搜索
     * */
    public void setFuzzySearch(boolean fuzzySearch,String valuePrefix,String valueSuffix) {
        this.fuzzySearch = fuzzySearch;
        this.valuePrefix=valuePrefix;
        if(this.valuePrefix==null) this.valuePrefix="";
        this.valueSuffix=valueSuffix;
        if(this.valueSuffix==null) this.valueSuffix="";
        if(fuzzySearch){
            if(this.field.getType()!= InputType.TEXT_INPUT && this.field.getType()!= InputType.TEXT_AREA && this.field.getType()!= InputType.CHECK_BOX && this.field.getType()!= InputType.SELECT_BOX) {
                throw new IllegalArgumentException(this.field.getLabelInSearch()+"("+this.field.getColumn()+") 为 "+this.field.getType().name()+" 类型，不支持模糊搜索");
            }
        }
    }

    private boolean fuzzySearch=false;

    public boolean getDisplayAlone() {
        return displayAlone;
    }

    /**
     * 是否每个字段都独立呈现在搜索栏中
     * */
    public void displayAlone(boolean displayAlone) {
        this.displayAlone = displayAlone;
    }

    /**
     * 在搜索栏中的行号
     * */
    private int rowIndex=0;

    /**
     * 在搜索栏中行内的的序号
     * */
    private int columnIndex=0;

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    private Integer inputWidth=null;

    public int getInputWidth() {
        if(this.inputWidth!=null) {
            return inputWidth;
        }
        return  searchAreaConfig.getInputWidth();
    }

    public void setInputWidth(int inputWidth) {
        this.inputWidth = inputWidth;
    }

    private Boolean searchInRange=false;

    /**
     * 是否使用范围搜索
     * */
    public void setSearchInRange(boolean useRange) {
        this.searchInRange=useRange;
    }

    public Boolean getSearchInRange() {
        return this.searchInRange;
    }
}
