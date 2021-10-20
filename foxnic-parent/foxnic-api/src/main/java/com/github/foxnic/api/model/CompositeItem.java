package com.github.foxnic.api.model;

public class CompositeItem {



    private CompositeParameter parameter;
    private String key;
    private String inputType;
    private String field;
    private Object value;
    private Boolean fuzzy;
    private String valuePrefix;
    private String valueSuffix;
    private Object begin;
    private Object end;
    private String label;
    private Object fillBy;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Boolean getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(Boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    public String getValuePrefix() {
        return valuePrefix;
    }

    public void setValuePrefix(String valuePrefix) {
        this.valuePrefix = valuePrefix;
    }

    public String getValueSuffix() {
        return valueSuffix;
    }

    public void setValueSuffix(String valueSuffix) {
        this.valueSuffix = valueSuffix;
    }

    public Object getBegin() {
        return begin;
    }

    public void setBegin(Object begin) {
        this.begin = begin;
    }

    public Object getEnd() {
        return end;
    }

    public void setEnd(Object end) {
        this.end = end;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getFillBy() {
        return fillBy;
    }

    public void setFillBy(Object fillBy) {
        this.fillBy = fillBy;
    }

    public CompositeParameter getParameter() {
        return parameter;
    }
    public void setParameter(CompositeParameter parameter) {
        this.parameter = parameter;
    }
}
