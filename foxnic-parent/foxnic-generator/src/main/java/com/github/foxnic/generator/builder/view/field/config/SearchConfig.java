package com.github.foxnic.generator.builder.view.field.config;

import com.github.foxnic.api.query.MatchType;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.generator.builder.view.config.SearchAreaConfig;
import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.InputType;

public class SearchConfig {

//    private boolean displayAlone=false;

    public boolean getFuzzySearch() {
        return fuzzySearch;
    }

    private SearchAreaConfig searchAreaConfig;
    private FieldInfo field;

    private Boolean triggerOnSelect = false;
    private String matchType=MatchType.auto.code();

    private Boolean selectMuliti = false;


    public  SearchConfig(FieldInfo field, SearchAreaConfig searchAreaConfig) {
        this.searchAreaConfig=searchAreaConfig;
        this.field=field;
    }

    private String valuePrefix;
    private String valueSuffix;
    private Boolean splitValue = false;

    public String getValuePrefix() {
        return valuePrefix;
    }

    public String getValueSuffix() {
        return valueSuffix;
    }

    /**
     * 是否在模糊搜索时将输入值按空格拆分
     * */
    public Boolean getSplitValue() {
        return splitValue;
    }

    /**
     * 设置是否为模糊搜索
     * */
    public void setFuzzySearch(boolean fuzzySearch,String valuePrefix,String valueSuffix,Boolean splitValue) {
        this.fuzzySearch = fuzzySearch;
        this.valuePrefix=valuePrefix;
        if(this.valuePrefix==null) this.valuePrefix="";
        this.valueSuffix=valueSuffix;
        if(this.valueSuffix==null) this.valueSuffix="";
        this.splitValue=splitValue;
        if(fuzzySearch){
            if(this.field.getType()!= InputType.TEXT_INPUT && this.field.getType()!= InputType.TEXT_AREA && this.field.getType()!= InputType.CHECK_BOX && this.field.getType()!= InputType.SELECT_BOX) {
                throw new IllegalArgumentException(this.field.getLabelInSearch()+"("+this.field.getColumn()+") 为 "+this.field.getType().name()+" 类型，不支持模糊搜索");
            }
        }
    }

    private boolean fuzzySearch=false;

//    public boolean getDisplayAlone() {
//        return displayAlone;
//    }

//    /**
//     * 是否每个字段都独立呈现在搜索栏中
//     * */
//    public void displayAlone(boolean displayAlone) {
//        this.displayAlone = displayAlone;
//    }

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

    public Integer getLabelWidth() {
        if(labelWidth!=null) return labelWidth;
        return searchAreaConfig.getLabelWidth(this.getColumnIndex());
    }

    public void setLabelWidth(Integer labelWidth) {
        this.labelWidth = labelWidth;
    }

    private Integer labelWidth =null;

    public int getInputWidth() {
        if(this.inputWidth!=null) {
            return inputWidth;
        }
        return  searchAreaConfig.getInputWidth(this.getColumnIndex());
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


    private  String searchField;

    public String getSearchField() {
        //如果未明确指定搜索字段
        if(StringUtil.isBlank(searchField)) {
            //如果是下拉框
//            if(this.field.getType()==InputType.SELECT_BOX) {
//                if(this.fuzzySearch) {
//                    return this.field.getSelectField().getTextField();
//                } else {
//                    return this.field.getSelectField().getValueField();
//                }
//            }
        }
        return searchField;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public Boolean getTriggerOnSelect() {
        return triggerOnSelect;
    }

    public void setTriggerOnSelect(Boolean triggerOnSelect) {
        this.triggerOnSelect = triggerOnSelect;
    }

    public Boolean getSelectMuliti() {
        return selectMuliti;
    }

    public void setSelectMuliti(Boolean selectMuliti) {
        this.selectMuliti = selectMuliti;
    }
}
