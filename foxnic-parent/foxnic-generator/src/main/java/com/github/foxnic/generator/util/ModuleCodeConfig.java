package com.github.foxnic.generator.util;

import com.github.foxnic.generator.builder.business.option.ServiceOptions;
import com.github.foxnic.generator.builder.model.PoClassFile;
import com.github.foxnic.generator.builder.model.VoClassFile;
import com.github.foxnic.generator.builder.view.option.FormOptions;
import com.github.foxnic.generator.builder.view.option.ListOptions;
import com.github.foxnic.generator.builder.view.option.SearchAreaOptions;
import com.github.foxnic.generator.builder.view.option.ViewOptions;
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


    protected abstract ModuleContext createModuleContext();

    public ModuleContext config(){

        System.out.println("正在配置 "+this.TABLE.name());

        this.context=createModuleContext();

        this.configCodeSegment();
        this.configModel(this.context.getPoClassFile(),this.context.getVoClassFile());
        this.configFields(this.context.view());
        this.configSearch(this.context.view(),this.context.view().search());
        this.configList(this.context.view(),this.context.view().list());
        this.configForm(this.context.view(),this.context.view().form());
        this.configService(this.context.service());
        this.configOverrides();
        //
        return  this.context;
    }

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
     * 配置列表
     * */
    public void configList(ViewOptions view,ListOptions list){}

    /**
     * 配置表单
     * */
    public void configForm(ViewOptions view,FormOptions form){};


    /**
     * 配置搜索
     * */
    public void configSearch(ViewOptions view,SearchAreaOptions search){};



}
