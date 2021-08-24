package com.github.foxnic.generator.builder.view.config;

public class JsVariable {

    private  String name;
    private String value;
    private String note;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getNote() {
        return note;
    }

    public JsVariable(String name, String value, String note) {
        this.name=name;
        this.value=value;
        this.note=note;
    }


}
