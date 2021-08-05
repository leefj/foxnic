package com.github.foxnic.generator.builder.view.config;

import java.util.List;
import java.util.Map;

public class FormGroupConfig {

    private String title=null;

    private Map<Integer,List<String>> columns;

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

}
