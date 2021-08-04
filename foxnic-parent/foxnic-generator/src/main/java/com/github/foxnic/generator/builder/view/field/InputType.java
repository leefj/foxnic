package com.github.foxnic.generator.builder.view.field;

import com.github.foxnic.api.constant.CodeTextEnum;

public enum InputType implements CodeTextEnum {

    TEXT_INPUT("单行文本"),
    TEXT_AREA("多行文本"),
    UPLOAD("文件上传"),
    NUMBER_INPUT("数字输入框"),
    LOGIC_SWITCH("逻辑切换"),
    RADIO_BOX("单选框"),
    CHECK_BOX("复选框"),
    SELECT_BOX("下拉框"),
    DATE_BOX("日期选择框");

    private String text;

    private InputType(String text)  {
        this.text=text;
    }

    public String code() {
        return this.name().toLowerCase();
    }

    public String text() {
        return text;
    }
}
