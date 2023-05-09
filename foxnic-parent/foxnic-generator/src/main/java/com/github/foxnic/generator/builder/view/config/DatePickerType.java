package com.github.foxnic.generator.builder.view.config;

public enum DatePickerType {
    year("yyyy"),month("yyyy-M"),date("yyyy-MM-dd"),time("HH:mm:ss"),datetime("yyyy-MM-dd HH:mm:ss");


    private DatePickerType(String format) {
        this.format=format;
    }

    private String format;

    public String format() {
        return format;
    }

}
