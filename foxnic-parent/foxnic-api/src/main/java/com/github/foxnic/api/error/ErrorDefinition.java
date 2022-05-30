package com.github.foxnic.api.error;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

public abstract class ErrorDefinition {

    private static Set<String> definitions=new HashSet<>();

    /**
     * 注册一个错误定义分类
     * */
    public static void regist(ErrorDefinition definition) {
        if(definitions.contains(definition.getClass().getName())) return;
        definitions.add(definition.getClass().getName());
        definition.init();
    }

    @PostConstruct
    public abstract void  init();
}
