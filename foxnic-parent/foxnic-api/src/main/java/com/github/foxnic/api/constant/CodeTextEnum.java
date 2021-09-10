package com.github.foxnic.api.constant;

public interface CodeTextEnum {

    String code();
    String name();
    String text();
    default String description() {
        return text();
    }

    public static CodeTextEnum parse(CodeTextEnum[] values,String code) {
        return null;
    }


}
