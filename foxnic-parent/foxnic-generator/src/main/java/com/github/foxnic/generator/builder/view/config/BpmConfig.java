package com.github.foxnic.generator.builder.view.config;

import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.sql.meta.DBField;

import java.util.*;

public class BpmConfig {

    /***
     * 流程引擎的集成方式
     * */
    private String integrateMode = "none";

    public String getIntegrateMode() {
        return integrateMode;
    }

    public void setIntegrateMode(String integrateMode) {
        this.integrateMode = integrateMode;
    }
}
