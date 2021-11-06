package com.github.foxnic.dao.dataperm;


import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.dao.dataperm.model.DataPermRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataPermManager {

    //暂用本地缓存，不考虑集群模式
    private Map<String,DataPermRule> rules=new ConcurrentHashMap<>();

    public Result apply(DataPermRule rule) {
        rules.put(rule.getCode(),rule);
        return ErrorDesc.success();
    }

    public DataPermRule get(String code) {
        return rules.get(code);
    }

}