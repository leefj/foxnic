package com.github.foxnic.generator.builder.view.field.config;

public class SearchConfig {

    private boolean displayAlone=false;

    public boolean getDisplayAlone() {
        return displayAlone;
    }

    /**
     * 是否每个字段都独立呈现在搜索栏中
     * */
    public void displayAlone(boolean displayAlone) {
        this.displayAlone = displayAlone;
    }
}
