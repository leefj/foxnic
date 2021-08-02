package com.github.foxnic.generator.builder.view.field.config;

public class SearchConfig {

    private boolean displayAlone=false;

    public boolean getFuzzySearch() {
        return fuzzySearch;
    }

    /**
     * 设置是否为模糊搜索
     * */
    public void setFuzzySearch(boolean fuzzySearch) {
        this.fuzzySearch = fuzzySearch;
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

}
