package com.github.foxnic.api.constant;

public interface CodeTextEnum {

    String code();
    String name();
    String text();
    default String description() {
        return text();
    }

    public static CodeTextEnum parse(CodeTextEnum[] values,String code) {
        if(code==null) return null;
        code=code.trim();
        if(code.length()==0) return null;
        // 优先匹配代码
        for (CodeTextEnum value : values) {
            if(value.code().equals(code)) return value;
        }
        // 若代码未匹配则匹配 name
        for (CodeTextEnum value : values) {
            if(value.name().equals(code)) return value;
        }
        return null;
    }


}
