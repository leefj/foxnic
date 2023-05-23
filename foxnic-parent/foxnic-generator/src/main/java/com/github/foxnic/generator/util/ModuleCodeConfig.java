package com.github.foxnic.generator.util;

import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.entity.FieldsBuilder;
import com.github.foxnic.generator.builder.business.option.ControllerOptions;
import com.github.foxnic.generator.builder.business.option.ServiceOptions;
import com.github.foxnic.generator.builder.model.PoClassFile;
import com.github.foxnic.generator.builder.model.VoClassFile;
import com.github.foxnic.generator.builder.view.option.*;
import com.github.foxnic.generator.config.ModuleContext;
import com.github.foxnic.sql.meta.DBTable;

public abstract class ModuleCodeConfig<T extends DBTable> {

    protected T TABLE;
    protected ModuleContext context;
    protected String tablePrefix;


    public ModuleCodeConfig(T table, String tablePrefix) {
        this.TABLE=table;
        this.tablePrefix=tablePrefix;
    }

    protected void init() {
        this.context=createModuleContext();
    }


    public FieldsBuilder createFieldsBuilder() {
        return FieldsBuilder.build(this.context.getDAO(), this.TABLE);
    }

    protected abstract ModuleContext createModuleContext();

    public ModuleContext config(){

        System.out.println("正在配置 "+this.TABLE.name());

        //
        this.configModel(this.context.getPoClassFile(),this.context.getVoClassFile());
        //
        this.configView(this.context.view(),this.context.view().list(),this.context.view().form());
        //
        this.configFields(this.context.view());
        //
        this.configSearch(this.context.view(),this.context.view().search());
        //
        this.context.getListConfig().clearToolButtons();
        this.context.getListConfig().clearOpColumnMenus();
        this.context.getListConfig().clearOpColumnButtons();
        this.configList(this.context.view(),this.context.view().list());
        //
        this.context.getFormConfig().reset();
        this.configForm(this.context.view(),this.context.view().form(),this.context.view().formWindow());
        //
        this.context.controller().clearApis();
        this.configController(this.context.controller());
        //
        this.context.getServiceConfig().reset();
        this.configService(this.context.service());
        //
        this.context.getBpmConfig().reset();
        this.configBPM(this.context.bpm());

        this.configOverrides();
        //
        return  this.context;
    }

    public void configView(ViewOptions view, ListOptions list, FormOptions form) {}

    /**
     * 配置模型
     * */
    public void configModel(PoClassFile poType, VoClassFile voType) {}

    /**
     * 配置代码片段
     * */
    public void configCodeSegment() {}

    /**
     * 配置字段
     * */
    public void configFields(ViewOptions view) {};

    /**
     * 配置覆盖
     * */
    public abstract void configOverrides();

    /**
     * 配置服务
     * */
    public void configService(ServiceOptions service){}

    /**
     * 配置控制器
     * */
    public void configController(ControllerOptions controller){}

    /**
     * 配置列表
     * */
    public void configList(ViewOptions view,ListOptions list){}

    /**
     * 配置表单
     * */
    public void configForm(ViewOptions view, FormOptions form, FormWindowOptions formWindow){};

    /**
     * 配置表单
     * */
    public void configBPM(BpmOptions bpm){};


    /**
     * 配置搜索
     * */
    public void configSearch(ViewOptions view,SearchAreaOptions search){};


    public static void execute() {

        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        StackTraceElement top=stackTraceElements[stackTraceElements.length-1];
        Class gerType= ReflectUtil.forName(top.getClassName());
        try {
            ModuleCodeConfig config=(ModuleCodeConfig)gerType.newInstance();
            ModuleContext context=config.context;
            context.refreshTableMeta();
            config.config();
            context.buildAll();
        } catch (Exception e) {
            Logger.exception(e);
        }

    }



}
