package com.github.foxnic.api.proxy;

public class InvokeSourceVar {

    private static ThreadLocal<InvokeSource> VAR =new ThreadLocal<>();

    public static void set(InvokeSource source){
        VAR.set(source);
    }

    public static InvokeSource get(){
        InvokeSource source=VAR.get();
        VAR.remove();
        return source;
    }


}
