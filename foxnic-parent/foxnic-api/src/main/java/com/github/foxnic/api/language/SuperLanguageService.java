package com.github.foxnic.api.language;

public interface SuperLanguageService {

    public static final String DEFAULT_CONTEXT = "defaults";

    /**
     *使用默认语言进行转换
     * */
    String translate(String defaults, String code, String context);

    /**
     *使用默认语言进行转换
     * */
    String translate(String defaults);

}
