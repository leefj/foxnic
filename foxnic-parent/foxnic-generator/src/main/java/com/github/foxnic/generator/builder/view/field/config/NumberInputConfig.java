package com.github.foxnic.generator.builder.view.field.config;

public class NumberInputConfig {

    private Double step=1.0;
    private Double minValue=null;
    private Double maxValue=null;



    private int scale=0;

    private boolean allowNegative=true;


    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    private boolean integer = true;
    private boolean decimal = false;

    /**
     * 整数
     * */
    public void integer() {
        integer = true;
        decimal=false;
        if(scale<0) {
            scale=0;
        }
    }
    public boolean getIsInteger() {
        return integer;
    }

    /**
     * 整数+小数
     * */
    public void decimal() {
        decimal = true;
        integer=false;
        if(scale<0) {
            scale=2;
        }
    }
    public boolean getIsDecimal() {
        return decimal;
    }

    public boolean getAllowNegative() {
        return allowNegative;
    }

    /**
     * 是否允许负数
     * */
    public void setAllowNegative(boolean allowNegative) {
        this.allowNegative = allowNegative;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public Double getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    private Double defaultValue = null;


}
