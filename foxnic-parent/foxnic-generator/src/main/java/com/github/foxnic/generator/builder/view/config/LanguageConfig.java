package com.github.foxnic.generator.builder.view.config;

public class LanguageConfig {

    public boolean isEnableContext() {
        return enableContext;
    }

    public void setEnableContext(boolean enableContext) {
        this.enableContext = enableContext;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    private boolean enableContext= false;
    private String context;
}
