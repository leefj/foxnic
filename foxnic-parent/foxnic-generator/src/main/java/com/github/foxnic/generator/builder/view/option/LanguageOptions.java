package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.LanguageConfig;
import com.github.foxnic.generator.config.ModuleContext;

public class LanguageOptions {

    private LanguageConfig languageConfig;
    private ModuleContext context;
    public LanguageOptions(ModuleContext context, LanguageConfig languageConfig) {
        this.languageConfig=languageConfig;
        this.context=context;
    }

    public LanguageOptions enableContext() {
        return enableContext(this.context.getTableMeta().getTableName());
    }

    public LanguageOptions enableContext(String context) {
        this.languageConfig.setEnableContext(true);
        this.languageConfig.setContext(context);
        return this;
    }
}
