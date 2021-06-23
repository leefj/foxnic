package com.github.foxnic.generator.builder.view.field.config;

public class ValidateConfig {

    private boolean required = false;
    private boolean phone = false;
    private boolean email = false;
    private boolean url = false;
    private boolean date = false;
    private boolean identity = false;


    /**
     * 必填项
     * */
    public ValidateConfig required() {
        required = true;
        return this;
    }
    public boolean isRequired() {
        return required;
    }
    public boolean getIsRequired() {
        return required;
    }


    /**
     * 手机号
     * */
    public ValidateConfig phone() {
        phone = true;
        return this;
    }
    public boolean isPhone() {
        return phone;
    }
    public boolean getIsPhone() {
        return phone;
    }

    /**
     * 邮箱
     * */
    public ValidateConfig email() {
        email = true;
        return this;
    }
    public boolean isEmail() {
        return email;
    }
    public boolean getIsEmail() {
        return email;
    }

    /**
     * URL地址
     * */
    public ValidateConfig url() {
        url = true;
        return this;
    }
    public boolean isUrl() {
        return url;
    }
    public boolean getIsUrl() {
        return url;
    }


    /**
     * 日期
     * */
    public ValidateConfig date() {
        date = true;
        return this;
    }
    public boolean isDate() {
        return date;
    }
    public boolean getIsDate() {
        return date;
    }

    /**
     * 身份证
     * */
    public ValidateConfig identity() {
        identity = true;
        return this;
    }
    public boolean isIdentity() {
        return identity;
    }
    public boolean getIsIdentity() {
        return identity;
    }



}
