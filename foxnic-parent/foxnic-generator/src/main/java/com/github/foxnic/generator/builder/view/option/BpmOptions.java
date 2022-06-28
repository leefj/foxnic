package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.api.bpm.IntegrateMode;
import com.github.foxnic.generator.builder.view.config.BpmConfig;
import com.github.foxnic.generator.builder.view.config.FormConfig;
import com.github.foxnic.generator.builder.view.config.Tab;
import com.github.foxnic.generator.config.ModuleContext;

public class BpmOptions {

    private BpmConfig config;
    private ModuleContext context;

    public BpmOptions(ModuleContext context, BpmConfig config) {
        this.context=context;
        this.config=config;
    }

    /**
     * 指示是否集成流程引擎，以及集成的方式
     * */
    public BpmOptions integrate(IntegrateMode mode) {
        this.config.setIntegrateMode(mode.code());
        return this;
    }


    /**
     * 设置表单代码
     */
    public BpmOptions form(String formCode) {
        this.config.setFormCode(formCode);
        return this;
    }
}
