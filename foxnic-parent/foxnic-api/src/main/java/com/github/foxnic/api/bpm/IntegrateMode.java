package com.github.foxnic.api.bpm;

import com.github.foxnic.api.constant.CodeTextEnum;

/***
 * 流程引擎的集成方式
 * */
public enum IntegrateMode implements CodeTextEnum {

    NONE("不集成"),FRONT("前端集成"),BACK("后端集成");

    private String text;
    private IntegrateMode(String text)  {
        this.text=text;
    }

    public String code() {
        return this.name().toLowerCase();
    }

    public String text() {
        return text;
    }

    public static IntegrateMode parseByCode(String code) {
        for (IntegrateMode mode : IntegrateMode.values()) {
            if(mode.code().equals(code)) return mode;
        }
        return null;
    }

}
