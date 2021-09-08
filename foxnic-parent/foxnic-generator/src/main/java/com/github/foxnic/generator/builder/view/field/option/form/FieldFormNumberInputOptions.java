package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormNumberInputOptions extends SubOptions {

    public FieldFormNumberInputOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    public FieldFormNumberInputOptions step(Double step) {
        this.field.numberField().setStep(step);
        return this;
    };

    public FieldFormNumberInputOptions range(Double min,Double max) {
        this.field.numberField().setMinValue(min);
        this.field.numberField().setMaxValue(max);
        return this;
    };


    /**
     * 整数
     * */
    public FieldFormNumberInputOptions integer() {
        this.field.numberField().integer();
        return this;
    }

    /**
     * 整数或小数
     * */
    public FieldFormNumberInputOptions decimal() {
        this.field.numberField().decimal();
        return this;
    }

    /**
     * 是否允许负数，默认允许
     * */
    public FieldFormNumberInputOptions allowNegative(boolean allow) {
        this.field.numberField().setAllowNegative(allow);
        return this;
    }

    /**
     *  小数位
     * */
    public FieldFormNumberInputOptions scale(int i) {
        this.field.numberField().setScale(i);
        return this;
    }


    /**
     * 设置默认值
     * */
    public FieldFormNumberInputOptions defaultValue(double value) {
        this.field.numberField().setDefaultValue(value);
        return this;
    }
}
