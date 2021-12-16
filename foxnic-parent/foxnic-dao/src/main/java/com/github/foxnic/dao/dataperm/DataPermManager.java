package com.github.foxnic.dao.dataperm;


import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.dao.dataperm.model.DataPermRule;
import com.github.foxnic.dao.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataPermManager {

    private static  DataPermManager instance;

    public static DataPermManager getInstance() {
        return instance;
    }

    public static interface ContextGetter<T> {
        T get();
    }


    public DataPermManager() {
        instance=this;
    }

    /**
     * 全局环境获取器
     * */
    private Map<String,ContextGetter> globalContextGetters=new HashMap<>();

    public <T> void registerGlobalContextGetter(Class<T> retunrType, String name,ContextGetter contextGetter) {
        globalContextGetters.put(name,contextGetter);
    }

    public <T> void registerLocalContextGetter(Class<? extends Entity> poType, Class<T> retunrType, String name,ContextGetter contextGetter) {
        Map<String,ContextGetter> local=localContextGetters.get(poType);
        if(local==null) {
            local=new HashMap<>();
            localContextGetters.put(poType,local);
        }
        local.put(name,contextGetter);
    }

    public Map<String,Object> getGlobalContexts() {
        Map<String,Object> contexts=new HashMap<>();
        for (Map.Entry<String, ContextGetter> entry : globalContextGetters.entrySet()) {
            try {
                contexts.put(entry.getKey(),entry.getValue().get());
            } catch (Exception e) {
                contexts.put(entry.getKey(),"error : "+e.getMessage());
            }
        }
        return contexts;
    }

    public Map<String,Object> getLocalContexts(Class<? extends Entity> poType) {
        Map<String,Object> contexts=new HashMap<>();
        Map<String,ContextGetter> local=localContextGetters.get(poType);
        if(local==null) {
            return contexts;
        }
        for (Map.Entry<String, ContextGetter> entry : local.entrySet()) {
            try {
                contexts.put(entry.getKey(),entry.getValue().get());
            } catch (Exception e) {
                contexts.put(entry.getKey(),"error : "+e.getMessage());
            }
        }
        return contexts;
    }


    private Map<Class<? extends Entity>,Map<String,ContextGetter>> localContextGetters=new HashMap<>();

    //暂用本地缓存，不考虑集群模式
    private Map<String,DataPermRule> rules=new ConcurrentHashMap<>();

    /**
     * 应用规则
     * */
    public Result apply(DataPermRule rule) {
        rules.put(rule.getCode(),rule);
        return ErrorDesc.success();
    }

    /**
     * 取消规则
     * */
    public void cancel(String code) {
        rules.remove(code);
    }

    public DataPermRule get(String code) {
        return rules.get(code);
    }


}
