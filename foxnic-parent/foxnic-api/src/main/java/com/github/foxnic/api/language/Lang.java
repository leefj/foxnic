package com.github.foxnic.api.language;

public class Lang {

    private static SuperLanguageService INSTANCE;

    /**
     * 转换为用户指定的语言
     * @param defaults 默认词条
     * @param code 词条代码，如无指定空值
     * @param context 语境，默认时指定空值
     * @return 目标语言的词条
     * */
    public static String translate(String defaults, String code, String context) {
        if(INSTANCE==null) return defaults;
        return INSTANCE.translate(defaults,code,context);
    }

    /**
     * 转换为用户指定的语言
     * @param defaults 默认词条
     * @param context 语境，默认时指定空值
     * @return 目标语言的词条
     * */
    public static String translate(String defaults,String context) {
        if(INSTANCE==null) return defaults;
        return INSTANCE.translate(defaults,null,context);
    }

    /**
     * 转换为用户指定的语言
     * @param defaults 默认词条
     * @return 目标语言的词条
     * */
    public static String translate(String defaults) {
        if(INSTANCE==null) return defaults;
        return INSTANCE.translate(defaults);
    }


}
