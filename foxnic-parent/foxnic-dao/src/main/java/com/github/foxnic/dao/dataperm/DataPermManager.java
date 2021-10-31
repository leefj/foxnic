package com.github.foxnic.dao.dataperm;


import com.github.foxnic.api.error.ErrorDesc;
import com.github.foxnic.api.transter.Result;
import com.github.foxnic.dao.dataperm.model.DataPermRule;
import com.github.foxnic.dao.dataperm.model.DataPermSubject;
import com.github.foxnic.dao.dataperm.model.DataPermSubjectVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataPermManager {

    //暂用本地缓存，不考虑集群模式
    private Map<String,DataPermRule> rules=new ConcurrentHashMap<>();
    private Map<String,DataPermSubject> subjects=new HashMap<>();
    private Map<String,DataPermSubjectVariable> varaibles=new HashMap<>();

    public Result apply(DataPermRule rule) {
        rules.put(rule.getCode(),rule);
        return ErrorDesc.success();
    }

    public DataPermRule get(String code) {
        return rules.get(code);
    }

    public void registSubject(String code, String name, DataPermSubject.SubjectGetter getter) {
        DataPermSubject subject=new DataPermSubject();
        subject.setCode(code);
        subject.setName(name);
        subject.setSubjectGetter(getter);
        this.subjects.put(subject.getCode(),subject);
    }

    public DataPermSubjectVariable getSubjectVariable(String variableName) {
        return varaibles.get(variableName);
    }

    public void registSubjectVariable(DataPermSubjectVariable variable) {
        DataPermSubject subject=subjects.get(variable.getSubjectCode());
        if(subject==null) {
            throw new IllegalArgumentException(variable.getName()+"，"+subject.getCode()+" 未找到对应的 Subject");
        }
        variable.setSubject(subject);
        varaibles.put("$"+variable.getSubject().getCode()+"."+variable.getVariable(),variable);
    }
}
