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
    public boolean getIsIdentity() {
        return identity;
    }





}
